package tulip.service.producerConsumer;

import tulip.app.appMessage.AppMessage;
import tulip.service.Config;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.MultiServerSocket;
import tulip.service.producerConsumer.messages.Message;

import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {

    private final String NAME;
    private final MultiServerSocket MULTI_SERVER_SOCKET;

    /** Map a name to a producer number */
    private Map<String, Integer> nameToProducerNumber = new ConcurrentHashMap<>();

    private final int BUFFER_SIZE = 10;
    private AppMessage[] buffer = new AppMessage[BUFFER_SIZE];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbcell = 0;
    private final int THRESHOLD = 5;
    private boolean tokenPresent = false;
    private int nbOfProducers = 0;
    private int nextProducer = 0;

    private boolean tokenStarted = false;
    private final Object lock = new Object();
    private final Object multiServerSocketLock = new Object();

    /**
     * Constructor
     * @param name The name of the consumer (unique identifier)
     * @param serverSocket A server socket to communicate with a producer
     */
    public Consumer(String name, ServerSocket serverSocket) {
        this.NAME = name;
        MULTI_SERVER_SOCKET = new MultiServerSocket(this,serverSocket);
        MULTI_SERVER_SOCKET.start();
        synchronized (multiServerSocketLock) { multiServerSocketLock.notifyAll(); }
    }

    /**
     * Consumes an AppMessage. Must be used in conjunction with the method boolean canConsume()
     * @return The AppMessage from the buffer or null if the buffer is empty
     */
    public AppMessage consume() {
        synchronized (lock) {
            if (nbmess > 0) {
                AppMessage appMessage = buffer[out];
                out = (out + 1) % BUFFER_SIZE;
                nbmess--;
                nbcell++;
                if (tokenPresent && nbcell > THRESHOLD) {
                    sendTokenTo(nextProducer, nbcell);
                    tokenPresent = false;
                    nbcell = 0;
                }

                // System.out.println("Consumer " + NAME + " consumes: " + appMessage.toJSON());
                return appMessage;
            }

            return null;
        }
    }

    /**
     * Sends an app message to a specific producer
     * NB: Since the producer-consumer system is unidirectional, this method does not use it. The app message is sent
     * regardless of the flow control.
     * @param name The name of the producer the app message is being sent to
     * @param appMessage The appMessage being sent
     * @throws IllegalStateException thrown if the producer to which the message is sent is not registered
     * */

    public void sendAppMessageTo(String name, AppMessage appMessage) throws IllegalStateException {

        if (!producerIsRegistered(name)) { throw new IllegalStateException(); }

        Integer producerNumber = nameToProducerNumber.get(name);
        Message message = new Message(NAME, Target.producer, ContentType.app, appMessage.toJSON());

        synchronized (multiServerSocketLock) {
            while (MULTI_SERVER_SOCKET == null) {
                try {
                    multiServerSocketLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(Config.TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
        System.out.println("Consumer " + NAME + " sends: " + appMessage.toJSON());

    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    public void uponReceipt(Message message, int producerNumber) {
        // System.out.println("Consumer " + NAME + " receives: " + message.toJSON());
        if (nbOfProducers > 0) {

            // Checks if the producer is not registered
            if (!producerIsRegistered(message.getSender())) {

                // If needed, maps the name of the sender to its producerNumber
                nameToProducerNumber.put(message.getSender(), producerNumber);
            }

            switch (message.getContentType()) {

                // If the message corresponds to a token
                case token:
                    if (message.getTarget().equals(Target.nextProducer)) {
                        passTokenToNextProducer(message);
                    } else {
                        int tokenValue = Integer.parseInt(message.getContent());
                        uponReceiptOfToken(tokenValue);
                    }
                    break;

                // If the message is an app message
                case app:
                    AppMessage appMessage = AppMessage.fromJSON(message.getContent());
                    uponReceiptOfAppMessage(appMessage);
                    break;
            }
        }
    }

    /**
     * This method is triggered when a message corresponding to the token is received.
     * @param val The value of the token when received
     * */
    private void uponReceiptOfToken(int val) {
        synchronized (lock) {
            nextProducer = (nextProducer + 1) % nbOfProducers;
            nbcell += val;
            if (nbcell > THRESHOLD) {
                sendTokenTo(nextProducer, nbcell);
                nbcell = 0;
            } else {
                tokenPresent = true;
            }
        }
    }

    /**
     * This method is triggered when an app message is received
     * @param appMessage The app message received
     * */
    private void uponReceiptOfAppMessage(AppMessage appMessage) {
        System.out.println("Consumer " + NAME + " receives: " + appMessage.toJSON());
        synchronized (lock) {
            buffer[in] = appMessage;
            in = (in + 1) % BUFFER_SIZE;
            nbmess++;
        }
    }

    /**
     * Checks if producer is registered
     * @param name The name of the producer
     * @return a boolean indicating whether the producer is registered
     */
    public boolean producerIsRegistered(String name) {
        return nameToProducerNumber.containsKey(name);
    }

    /** Adds a producer and starts the token system if needed */
    public void addProducer() {
        // System.out.println("Adds producer");
        synchronized (lock) {
            nbOfProducers++;
        }

        if (!tokenStarted) { startToken(); }

    }

    /** Starts the token system by sending the first token message */
    private void startToken() {
        tokenStarted = true;
        sendTokenTo(nextProducer, BUFFER_SIZE);
    }

    /**
     * Sends the token to a specific producer
     * @param producerNumber The number of the producer the token is being sent to
     * @param tokenValue The value of the token being sent
     * */
    private void sendTokenTo(int producerNumber, int tokenValue) {
        Message message = new Message(NAME, Target.producer, ContentType.token, Integer.toString(tokenValue));

        synchronized (multiServerSocketLock) {
            while (MULTI_SERVER_SOCKET == null) {
                try {
                    multiServerSocketLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(Config.TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
        // System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
    }

    /**
     * Sends the token to the next producer in the token ring.
     * @param message The message containing the token being sent.
     */
    private void passTokenToNextProducer(Message message) {
        nextProducer = (nextProducer + 1) % nbOfProducers;

        synchronized (multiServerSocketLock) {
            while (MULTI_SERVER_SOCKET == null) {
                try {
                    multiServerSocketLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(Config.TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MULTI_SERVER_SOCKET.sendMessageToClient(
                nextProducer,
                new Message(NAME, Target.producer, ContentType.token, message.getContent())
        );
        // System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
    }
}

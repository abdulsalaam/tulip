package tulip.service.producerConsumer;

import tulip.app.appMessage.AppMessage;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.MultiServerSocket;
import tulip.service.producerConsumer.messages.Message;

import java.net.ServerSocket;
import java.nio.BufferUnderflowException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {

    private final String NAME;
    private final MultiServerSocket MULTI_SERVER_SOCKET;

    /** Map a name to a producer number */
    private Map<String, Integer> nameToProducerNumber = new ConcurrentHashMap<>();

    private final int BUFFER_SIZE = 10;
    private Message[] buffer = new Message[BUFFER_SIZE];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbcell = 0;
    private final int THRESHOLD = 5;
    private boolean tokenPresent = false;
    private int nbOfProducers = 0;
    private int nextProducer = 0;

    private boolean tokenStarted = false;
    private final Object monitor = new Object();

    /**
     * Constructor
     * @param name The name of the consumer (unique identifier)
     * @param serverSocket A server socket to communicate with a producer
     */
    public Consumer(String name, ServerSocket serverSocket) {
        this.NAME = name;
        MULTI_SERVER_SOCKET = new MultiServerSocket(this,serverSocket);
        MULTI_SERVER_SOCKET.start();
    }

    /** Indicates whether there are messages in the buffer that can be consumed */
    public boolean canConsume() {
        synchronized (monitor) {
            return nbmess > 0;
        }
    }

    /**
     * Consumes an AppMessage. Must be used in conjunction with the method boolean canConsume()
     * @return The AppMessage from the buffer
     * @throws BufferUnderflowException thrown when there is no message to
     */
    public AppMessage consume() throws BufferUnderflowException {
        synchronized (monitor) {
            if (nbmess > 0) {
                //System.out.println("Consume");
                Message message = buffer[out];
                out = (out + 1) % BUFFER_SIZE;
                nbmess--;
                nbcell++;
                if (tokenPresent && nbcell > THRESHOLD) {
                    sendTokenTo(nextProducer, nbcell);
                    tokenPresent = false;
                    nbcell = 0;
                }

                return AppMessage.fromJSON(message.getContent());
            }

            throw new BufferUnderflowException();
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
        String rawAppMessage = appMessage.toJSON();
        Message message = new Message(NAME, Target.producer, ContentType.app, rawAppMessage);
       // System.out.println("Consumer " + NAME + " sends MESSAGE: " + message.toJSON());
        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    public void uponReceipt(Message message, int producerNumber) {
      //  System.out.println("Consumer " + NAME + " receives: " + message.toJSON());
        if (nbOfProducers > 0) {

            // Checks if the producer is not registered
            if (!producerIsRegistered(message.getSender())) {

                // If needed, map the name of the sender to its producerNumber
                nameToProducerNumber.put(message.getSender(), producerNumber);
            }

            // If the message corresponds to a token
            if (message.getContentType().equals(ContentType.token)) {

                if (message.getTarget().equals(Target.nextProducer)) {
                    passTokenToNextProducer(message);
                } else {
                    int tokenValue = Integer.parseInt(message.getContent());
                    uponReceiptOfToken(tokenValue);
                }

            // If the message is an app message
            } else if (message.getContentType().equals(ContentType.app)) {
                uponReceiptOfAppMessage(message);
            }
        }
    }

    /**
     * This method is triggered when an app message is received
     * @param message The app message received
     * */
    private void uponReceiptOfAppMessage(Message message) {
        synchronized (monitor) {
            //System.out.println(message.toJSON());
            buffer[in] = message;
            in = (in + 1) % BUFFER_SIZE;
            nbmess++;
        }
    }
    
    /** 
     * This method is triggered when a message corresponding to the token is received.
     * @param val The value of the token when received
     * */
    private void uponReceiptOfToken(int val) {
        synchronized (monitor) {
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
     * Sends the token to a specific producer
     * @param producerNumber The number of the producer the token is being sent to
     * @param tokenValue The value of the token being sent
     * */
    private void sendTokenTo(int producerNumber, int tokenValue) {
       // System.out.println("Producer number " + producerNumber);
        Message message = new Message(NAME, Target.producer, ContentType.token, Integer.toString(tokenValue));
       // System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
    }

    /** Adds a producer and starts the token system if needed */
    public void addProducer() {
        synchronized (monitor) {
           // System.out.println("Add producer");
            nbOfProducers++;

            if (!tokenStarted) {
                startToken();
            }
        }
    }

    /** Starts the token system by sending the first token message */
    private void startToken() {
        tokenStarted = true;
        sendTokenTo(nextProducer, BUFFER_SIZE);
    }

    /**
     * Sends the token to the next producer in the token ring.
     * @param message The message containing the token being sent.
     */
    private void passTokenToNextProducer(Message message) {
        synchronized (monitor) {
            nextProducer = (nextProducer + 1) % nbOfProducers;
            MULTI_SERVER_SOCKET.sendMessageToClient(
                    nextProducer,
                    new Message(NAME, Target.producer, ContentType.token, message.getContent())
            );
           // System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
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
}

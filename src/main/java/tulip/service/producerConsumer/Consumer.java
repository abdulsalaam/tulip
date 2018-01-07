package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.MultiServerSocket;
import tulip.service.producerConsumer.messages.Message;

import java.net.ServerSocket;

public class Consumer {

    private final String NAME;
    private final MultiServerSocket MULTI_SERVER_SOCKET;

    private final int BUFFER_SIZE = 10;
    private Message[] buffer = new Message[BUFFER_SIZE];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbcell = 0;
    private final int THRESHOLD = 5;
    private boolean tokenPresent = false;
    private int nbOfProducers = 0;
    private int next = 0;

    private boolean tokenStarted = false;
    private final Object monitor = new Object();

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

    /** Consumes a message. Must be used in conjunction with the method boolean canConsume() */
    public Message consume() {
        synchronized (monitor) {
            if (nbmess > 0) {
                System.out.println("Consumming");
                Message message = buffer[out];
                out = (out + 1) % BUFFER_SIZE;
                nbmess--;
                nbcell++;
                if (tokenPresent && nbcell > THRESHOLD) {
                    sendTokenTo(next, nbcell);
                    tokenPresent = false;
                    nbcell = 0;
                }

                return message;
            }

            System.out.println("Cannot consume");
            return null;
        }
    }

    /**
     * Sends an app message to a specific producer
     * NB: Since the producer-consumer system is unidirectional, this method does not use it. The app message is sent
     * regardless of the flow control.
     * @param producerNumber The number of the producer the app message is being sent to
     * @param appMessage The appMessage being sent
     * */
    private void sendAppMessageTo(int producerNumber, String appMessage) {
        System.out.println("Producer number " + producerNumber);
        Message message = new Message(Target.producer, ContentType.token, appMessage);
        System.out.println("Consumer " + NAME + " sends MESSAGE: " + message.toJSON());
        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    public void uponReceipt(Message message) {
        System.out.println("Consumer " + NAME + " receives: " + message.toJSON());
        if (nbOfProducers > 0) {

            if (message.getContentType().equals(ContentType.token)) {

                if (message.getTarget().equals(Target.nextProducer)) {
                    passTokenToNextProducer(message);
                } else {
                    int tokenValue = Integer.parseInt(message.getContent());
                    uponReceiptOfToken(tokenValue);
                }

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
            System.out.println(message.toJSON());
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
            next = (next + 1) % nbOfProducers;
            nbcell += val;
            if (nbcell > THRESHOLD) {
                sendTokenTo(next, nbcell);
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
        System.out.println("Producer number " + producerNumber);
        Message message = new Message(Target.producer, ContentType.token, Integer.toString(tokenValue));
        System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
    }

    /** Adds a producer and starts the token system if needed */
    public void addProducer() {
        synchronized (monitor) {
            System.out.println("Add producer");
            nbOfProducers++;

            if (!tokenStarted) {
                startToken();
            }
        }
    }

    /** Starts the token system by sending the first token message */
    private void startToken() {
        tokenStarted = true;
        sendTokenTo(next, BUFFER_SIZE);
    }

    /**
     * Sends the token to the next producer in the token ring.
     * @param message The message containing the token being sent.
     */
    private void passTokenToNextProducer(Message message) {
        synchronized (monitor) {
            next = (next + 1) % nbOfProducers;
            MULTI_SERVER_SOCKET.sendMessageToClient(
                    next,
                    new Message(Target.producer, ContentType.token, message.getContent())
            );
            System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
        }
    }
}

package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.ClientSocket;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;

public class Producer extends Thread {

    private final ClientSocket CLIENT_SOCKET;
    private final String NAME;

    private final int BUFFER_SIZE = 10;
    private Message[] buffer = new Message[BUFFER_SIZE];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbaut = 0;
    private int temp = 0;
    private final int THRESHOLD = 4;

    private final Object monitor = new Object();

    public Producer(String name) {
        this.NAME = name;
        this.CLIENT_SOCKET = new ClientSocket(this,"127.0.0.1", 4000);
        this.CLIENT_SOCKET.start();
        new Postman().start();
    }

    /**
     * When possible the postman will send the messages in the buffer
     */
    private class Postman extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (nbaut > 0) {
                        sendMessage(buffer[out]);
                        out = (out + 1) % BUFFER_SIZE;
                        nbaut--;
                        nbmess--;
                    }
                }
            }
        }
    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    public void uponReceipt(Message message) {
        synchronized (monitor) {
            System.out.println("Producer " + NAME + " receives: " + message.toJSON());
            if (message.getContentType().equals(ContentType.token)) {
                int tokenValue = Integer.parseInt(message.getContent());
                uponReceiptOfToken(tokenValue);
            }
        }
    }

    /** Indicates whether there is room in the buffer */
    public boolean canProduce() {
        synchronized (monitor) {
            return nbmess < BUFFER_SIZE;
        }
    }

    /**
     * Produces a message by adding it tho the buffer array. Must be used in conjunction with boolean canProduce()
     * @param message The message you want to add to the message buffer
     */
    public void produce(Message message) {
        synchronized (monitor) {
            System.out.println("Produire");
            if (canProduce()) {
                buffer[in] = message;
                in = (in + 1) % BUFFER_SIZE;
                nbmess++;
            } else {
                System.out.println("Buffer overflow");
            }
        }
    }

    /**
     * This method is triggered when a message corresponding to the token is received.
     * @param tokenValue The value of the token when received
     * */
    private void uponReceiptOfToken(int tokenValue) {
        synchronized (monitor) {
            temp = Math.min(nbmess - nbaut, tokenValue);
            int value = tokenValue;
            value -= temp;
            nbaut += temp;
            if (value > THRESHOLD) {
                sendTokenToProducer(value);
            } else {
                sendTokenToConsumer(value);
            }
        }
    }

    /**
     * Sends the token to the next producer
     * @param tokenValue The value of the token being sent
     */
    private void sendTokenToProducer(int tokenValue) {
        synchronized (monitor) {
            Message message = new Message(Target.nextProducer, ContentType.token, Integer.toString(tokenValue));
            CLIENT_SOCKET.sendMessage(message);
            System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
        }
    }

    /**
     * Sends the token to the consumer
     * @param tokenValue The value of the token being sent
     */
    private void sendTokenToConsumer(int tokenValue) {
        synchronized (monitor) {
            Message message = new Message(Target.consumer, ContentType.token, Integer.toString(tokenValue));
            CLIENT_SOCKET.sendMessage(message);
            System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
        }
    }

    /**
     * Sends a message
     * @param message The message being sent
     */
    private void sendMessage(Message message) {
        synchronized (monitor) {
            System.out.println("Producer " + NAME + " sends MESSAGE: " + message.toJSON());
            CLIENT_SOCKET.sendMessage(message);
        }
    }
}

package tulip.service.producerConsumer;

import tulip.app.appMessage.AppMessage;
import tulip.service.Config;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.ClientSocket;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;

import java.net.Socket;

public class Producer {

    private final String NAME;
    private final ClientSocket CLIENT_SOCKET;
    private final ProducerMessenger PRODUCER_MESSENGER;

    private final int BUFFER_SIZE = 10;
    private AppMessage[] buffer = new AppMessage[BUFFER_SIZE];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbaut = 0;
    private int temp = 0;
    private final int THRESHOLD = 4;

    private final Object lock = new Object();
    private final Object clientSocketLock = new Object();
    private final Object producerMessengerLock = new Object();

    /**
     * Constructor
     * @param name The name of the producer (unique identifier)
     * @param socket The socket used to communicate with the comsumer
     * @param producerMessenger An object implementing the producerMessenger interface
     */
    public Producer(String name, Socket socket, ProducerMessenger producerMessenger) {
        this.NAME = name;
        this.CLIENT_SOCKET = new ClientSocket(this,socket);
        this.CLIENT_SOCKET.start();
        synchronized (clientSocketLock) { clientSocketLock.notifyAll(); }
        this.PRODUCER_MESSENGER = producerMessenger;
        synchronized (producerMessengerLock) { producerMessengerLock.notifyAll(); }
        new Postman().start();
    }

    /**
     * Produces a message by adding it to the buffer array.
     * @param appMessage The app message to be sent
     * @return true if the message has been produced, false otherwise
     */
    public boolean produce(AppMessage appMessage) {
        synchronized (lock) {
            if (nbmess < BUFFER_SIZE) {
                buffer[in] = appMessage;
                in = (in + 1) % BUFFER_SIZE;
                nbmess++;
                System.out.println("Producer " + NAME + " produces: " + appMessage.toJSON());
                return true;
            }

            return false;
        }
    }

    /**
     * When possible the postman will send the messages in the buffer
     */
    private class Postman extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(Config.TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    if (nbaut > 0) {
                        sendAppMessage(buffer[out]);
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
        // System.out.println("Producer " + NAME + " receives: " + message.toJSON());

        switch (message.getContentType()) {
            // If the message corresponds to a token
            case token:
                int tokenValue = Integer.parseInt(message.getContent());
                uponReceiptOfToken(tokenValue);
                break;

            // If the message corresponds to an app message
            case app:
                AppMessage appMessage = AppMessage.fromJSON(message.getContent());

                synchronized (producerMessengerLock) {
                    while (PRODUCER_MESSENGER == null) {
                        try {
                            clientSocketLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                PRODUCER_MESSENGER.uponReceiptOfAppMessage(appMessage);
                break;
        }
    }

    /**
     * This method is triggered when a message corresponding to the token is received.
     * @param tokenValue The value of the token when received
     * */
    private void uponReceiptOfToken(int tokenValue) {
        synchronized (lock) {
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
        Message message = new Message(NAME, Target.nextProducer, ContentType.token, Integer.toString(tokenValue));

        synchronized (clientSocketLock) {
            while (CLIENT_SOCKET == null) {
                try {
                    clientSocketLock.wait();
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

        CLIENT_SOCKET.sendMessage(message);
        // System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
    }

    /**
     * Sends the token to the consumer
     * @param tokenValue The value of the token being sent
     */
    private void sendTokenToConsumer(int tokenValue) {
        Message message = new Message(NAME, Target.consumer, ContentType.token, Integer.toString(tokenValue));

        synchronized (clientSocketLock) {
            while (CLIENT_SOCKET == null) {
                try {
                    clientSocketLock.wait();
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

        CLIENT_SOCKET.sendMessage(message);
        // System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
    }

    /**
     * Sends an AppMessage over socket by putting it into a message before sending it
     * @param appMessage The message being sent
     */
    private void sendAppMessage(AppMessage appMessage) {
        Message message = new Message(NAME, Target.consumer, ContentType.app, appMessage.toJSON());

        synchronized (clientSocketLock) {
            while (CLIENT_SOCKET == null) {
                try {
                    clientSocketLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        CLIENT_SOCKET.sendMessage(message);
        System.out.println("Producer " + NAME + " sends APP MESSAGE: " + message.toJSON());
    }
}

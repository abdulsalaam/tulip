package tulip.service.sockets;

import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * A MultiServerSocket listens to a port and each time a ClientSocket wants to communicate on this port, it creates
 * a MultiServerSocketThread to handle the communication. Therefore, several MultiServerSocketThread correspond to a
 * unique MultiServerSocket.
 **/
public class MultiServerSocket extends Thread {

    private final Consumer CONSUMER;
    private final ServerSocket SERVER_SOCKET;

    /** The list of the MultiServerSocketThread corresponding to the socket clients connected */
    private List<MultiServerSocketThread> clients = new ArrayList<>();

    private final Object monitor = new Object();

    public MultiServerSocket(Consumer consumer, ServerSocket serverSocket) {
        this.CONSUMER = consumer;
        this.SERVER_SOCKET = serverSocket;
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try {
            while (true) {

                // When a new client socket initiates a connection with the MultiServerSocket
                // it creates a new multiServerSocketThread to handle the connection with the socket
                MultiServerSocketThread multiServerSocketThread =
                        new MultiServerSocketThread(this, SERVER_SOCKET.accept());

                // Starts the thread
                multiServerSocketThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

    /**
     * Sends a message to a specific client
     * @param clientNumber The position of the client on the list of the connected client
     * @param message The message object to send to the client
     */
    public void sendMessageToClient(int clientNumber, Message message) {
        synchronized (monitor) {
            while (clients == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clients.get(clientNumber).sendMessage(message);
        }
    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    void uponReceipt(Message message) {
        synchronized (monitor) {
            while (CONSUMER == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            CONSUMER.uponReceipt(message);
        }
    }

    /**
     * Each time a client server connects to the server socket, it adds the corresponding multiServerSocketThread to the
     * list of clients.
     * @param multiServerSocketThread The multiServerSocketThread corresponding to the client connecting
     */
    void addClient(MultiServerSocketThread multiServerSocketThread) {
        synchronized (monitor) {
            while (CONSUMER == null || clients == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clients.add(multiServerSocketThread);
            CONSUMER.addProducer();
        }
    }
}

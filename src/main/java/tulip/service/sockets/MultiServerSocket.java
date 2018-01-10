package tulip.service.sockets;

import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A MultiServerSocket listens to a port and each time a ClientSocket wants to communicate on this port, it creates
 * a MultiServerSocketThread to handle the communication. Therefore, several MultiServerSocketThread correspond to a
 * unique MultiServerSocket.
 **/
public class MultiServerSocket extends Thread {

    private final Consumer CONSUMER;
    private final ServerSocket SERVER_SOCKET;

    /** The list of the MultiServerSocketThread corresponding to the socket clients connected */
    private List<MultiServerSocketThread> clients;

    private final Object consumerMonitor = new Object();
    private final Object clientsMonitor = new Object();

    public MultiServerSocket(Consumer consumer, ServerSocket serverSocket) {
        this.CONSUMER = consumer;
        synchronized (consumerMonitor) { consumerMonitor.notifyAll(); }
        this.SERVER_SOCKET = serverSocket;
        this.clients = new ArrayList<>();
        synchronized (clientsMonitor) { clientsMonitor.notifyAll(); }
    }

    @Override
    public void run() {
        System.out.println("MultiServerSocket starting");
        try {
            while (true) {
                // When a new client socket initiates a connection with the MultiServerSocket,
                // it creates and starts a new multiServerSocketThread to handle the connection with the socket
                new MultiServerSocketThread(this, SERVER_SOCKET.accept()).start();
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
     * @throws NoSuchElementException If the clientNumber is not in the list
     */
    public void sendMessageToClient(int clientNumber, Message message) throws NoSuchElementException {
        synchronized (clientsMonitor) {
            while (clients == null) {
                try {
                    clientsMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            clients.get(clientNumber).sendMessage(message);
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Deals with the receipt of a message
     * @param message The message being received
     */
    void uponReceipt(Message message, int clientNumber) {
        synchronized (consumerMonitor) {
            while (CONSUMER == null) {
                try {
                    consumerMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        CONSUMER.uponReceipt(message, clientNumber);
    }

    /**
     * Each time a client server connects to the server socket, it adds the corresponding multiServerSocketThread to the
     * list of clients.
     * @param multiServerSocketThread The multiServerSocketThread corresponding to the client connecting
     */
    void addClient(MultiServerSocketThread multiServerSocketThread) {

        synchronized (clientsMonitor) {
            while (clients == null) {
                try {
                    clientsMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clients.add(multiServerSocketThread);
            multiServerSocketThread.setClientNumber(clients.indexOf(multiServerSocketThread));
        }

        synchronized (consumerMonitor) {
            while (CONSUMER == null) {
                try {
                    consumerMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        CONSUMER.addProducer();
    }
}

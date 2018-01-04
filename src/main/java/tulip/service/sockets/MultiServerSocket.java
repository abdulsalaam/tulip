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

    private Consumer CONSUMER;

    /**
     * The port the server socket listens to
     */
    private final int PORT;

    private List<MultiServerSocketThread> clients = new ArrayList<>();

    private final Object monitor = new Object();

    public MultiServerSocket(Consumer consumer, int port) {
        this.CONSUMER = consumer;
        this.PORT = port;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {

                // A new client socket initiates a connection with MultiServerSocket
                // Creates a new multiServerSocketThread with the socket
                MultiServerSocketThread multiServerSocketThread =
                        new MultiServerSocketThread(this, serverSocket.accept());

                // Starts the thread
                multiServerSocketThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

    /**
     * Send a message to a specific client
     * @param clientNumber
     * @param message
     */
    public void sendMessageToClient(int clientNumber, Message message) {
        clients.get(clientNumber).sendMessage(message);
    }

    void uponReceipt(Message message) {
        CONSUMER.sur_reception_de(message);
    }

    public void addClient(MultiServerSocketThread multiServerSocketThread) {
        clients.add(multiServerSocketThread);
        CONSUMER.addProducer();
    }
}

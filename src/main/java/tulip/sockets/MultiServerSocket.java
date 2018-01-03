package tulip.sockets;

import tulip.sockets.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A MultiServerSocket listens to a port and each time a ClientSocket wants to communicate on this port, it creates
 * a MultiServerSocketThread to handle the communication. Therefore, several MultiServerSocketThread correspond to a
 * unique MultiServerSocket.
 *
 * The MultiServerSocket is also used to orchestrate the token system.
 */
public class MultiServerSocket extends Thread {

    /**
     * The name that identifies the server socket on the network.
     * Please note that the MultiServerSocket and the MultiServerSocketThreads have the same name.
     * */
    private final String SERVER_SOCKET_NAME;

    /** The port the server socket listens to */
    private final int PORT;

    /** Maps the name of each registered client to its corresponding MultiServerSocketThread */
    private Map<String, MultiServerSocketThread> registeredClients = new ConcurrentHashMap<>();

    /** Indicates whether the token system has been started by sending a the token */
    private boolean tokenStarted = false;

    /** Iterator used in the token system to iterate through the server socket threads corresponding to the registered clients */
    private Iterator<Map.Entry<String, MultiServerSocketThread>> tokenIterator;

    private final Object monitor = new Object();

    public MultiServerSocket(String serverSocketName, int port) {
        SERVER_SOCKET_NAME = serverSocketName;
        PORT = port;
    }

    /* Producer - consumer */
    int N = 10;
    Message[] T = new Message[N];
    int in = 0;
    int out = 0;
    int nbmess = 0;
    int nbcell = 0;
    final int SEUIL = 6;
    boolean present = false;


    public Message consommer() {
        // Attendre (nbmess > 0)
        Message m = T[out];
        out = (out + 1) % N;
        nbmess--;
        nbcell++;
        if (present && nbcell > SEUIL) {
            passToken(nbcell);
            present = false;
            nbcell = 0;
        }

    }

    public void sur_reception_de() {

    }


    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Creates a new thread each time a new client socket initiates a connection with MultiServerSocket
                new MultiServerSocketThread(this, SERVER_SOCKET_NAME, serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

    /**
     * This methods is used when a client socket sends a registration request.
     * It enables to map the name of the socket client with the MultiServerSocketThread which is used
     * to communicate with it.
     * @param clientName
     * @param thread
     */
    public void registerConnectedClient(String clientName, MultiServerSocketThread thread) {
        synchronized (monitor) {
            registeredClients.put(clientName, thread);
            System.out.println(
                    "MultiServerSocket \"" + SERVER_SOCKET_NAME + "\": registers MultiServerSocketThread \"" + clientName
                            + "\" on thread " + thread.getId()
            );

            resetTokenIterator();
        }

        // Start the token system only once, when the first client registers
        if (!tokenStarted) {
            passToken(10);
            tokenStarted = true;
        }
    }

    /**
     * The MultiServerSocket passes the token to the next client on the token system.
     * This method is called by the MultiServerSocketThread which currently has the token.
     * @param tokenValue
     */
    public void passToken(int tokenValue) {
        synchronized (monitor) {

            // If the last client on the token ring is attained, the iterator is resetted in order to send the token
            // to the first client and make a loop
            if (!tokenIterator.hasNext()) {
                resetTokenIterator();
                tokenValue = 10;
            }

            Map.Entry<String, MultiServerSocketThread> entry = tokenIterator.next();
            entry.getValue().sendToken(tokenValue);
        }
    }

    /**
     * Resets the token iterator
     */
    public void resetTokenIterator() {
        tokenIterator = registeredClients.entrySet().iterator();
    }
}

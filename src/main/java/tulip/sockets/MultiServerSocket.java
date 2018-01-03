package tulip.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiServerSocket extends Thread {

    private final String SERVER_SOCKET_NAME;
    private final int PORT;

    private boolean tokenStarted = false;

    /** Maps the name of each connected client to its corresponding MultiServerSocketThread */
    private Map<String, MultiServerSocketThread> connectedClients = new ConcurrentHashMap<>();

    private Iterator<Map.Entry<String, MultiServerSocketThread>> tokenIterator;

    private final Object monitor = new Object();

    public MultiServerSocket(String serverSocketName, int port) {
        SERVER_SOCKET_NAME = serverSocketName;
        PORT = port;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new MultiServerSocketThread(this, SERVER_SOCKET_NAME, serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

    /**
     * This methods is used when a client socket sends a connection request.
     * It enables to map the name of the socket client with the MultiServerSocketThread which is used
     * to communicate with it.
     * @param clientName
     * @param thread
     */
    public void registerConnectedClient(String clientName, MultiServerSocketThread thread) {
        synchronized (monitor) {
            connectedClients.put(clientName, thread);
            System.out.println(
                    "MultiServerSocket \"" + SERVER_SOCKET_NAME + "\": registers MultiServerSocketThread \"" + clientName
                            + "\" on thread " + thread.getId()
            );

            resetTokenIterator();
        }

        if (!tokenStarted) {
            passToken(10);
            tokenStarted = true;
        }
    }

    public void passToken(int tokenValue) {
        synchronized (monitor) {

            if (!tokenIterator.hasNext()) {
                resetTokenIterator();
                tokenValue = 10;
            }

            Map.Entry<String, MultiServerSocketThread> entry = tokenIterator.next();
            entry.getValue().sendToken(tokenValue);
        }
    }


    public void resetTokenIterator() {
        tokenIterator = connectedClients.entrySet().iterator();
    }
}

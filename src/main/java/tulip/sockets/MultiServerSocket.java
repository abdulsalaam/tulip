package tulip.sockets;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiServerSocket extends Thread {

    private final String SERVER_SOCKET_NAME;
    private final int PORT;

    public MultiServerSocket(String serverSocketName, int port) {
        SERVER_SOCKET_NAME = serverSocketName;
        PORT = port;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new MultiServerSocketThread(SERVER_SOCKET_NAME, serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

}

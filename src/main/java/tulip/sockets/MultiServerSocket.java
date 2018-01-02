package tulip.sockets;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiServerSocket extends Thread {

    private final int PORT;

    public MultiServerSocket(int port) {
        PORT = port;
    }

    @Override
    public void run() {

        System.out.println("Launch MultiServerSocket");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new MultiServerSocketThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

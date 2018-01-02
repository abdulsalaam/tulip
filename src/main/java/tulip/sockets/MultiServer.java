package tulip.sockets;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiServer extends Thread {

    private final int PORT;

    public MultiServer(int port) {
        PORT = port;
    }

    @Override
    public void run() {

        System.out.println("Launch MultiServer");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new MultiServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

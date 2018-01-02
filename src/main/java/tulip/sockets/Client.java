package tulip.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {

    private final int PORT;
    private final Object monitor = new Object();

    private String[] buffer = new String[10];
    private int bufferPos = 0;


    public Client(int port) {
        PORT = port;
    }

    @Override
    public void run() {

        System.out.println("Launch Client");

        try (
                Socket socket = new Socket(InetAddress.getLocalHost(), 4000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            while (true) {

                sendMessages(out);

                String fromServer;
                if ((fromServer = in.readLine()) != null) {
                    System.out.println("Client receives: " + fromServer);

                    if (fromServer.equals("Bye")) {
                        System.out.println("Client quit");
                        break;
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String string) {
        synchronized (monitor) {
            buffer[bufferPos] = string;
            bufferPos++;
        }
    }

    private void sendMessages(PrintWriter out) {
        synchronized (monitor) {
            int index = 0;
            while (buffer[index] != null) {
                out.println(buffer[index]);
                System.out.println("Client sends: " + buffer[index]);
                buffer[index] = null;
                index++;
            }
            bufferPos = 0;
        }
    }
}

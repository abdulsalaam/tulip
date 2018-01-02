package tulip.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {

    private final InetAddress ADDRESS;
    private final int PORT;
    private final String NAME;
    private final Object monitor = new Object();

    public Client(InetAddress address, int port, String name) {
        this.ADDRESS = address;
        this.PORT = port;
        this.NAME = name;
    }

    @Override
    public void run() {

        System.out.println("Launch Client");

        try (
                Socket socket = new Socket(ADDRESS, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            // Send name to server
            out.println(NAME);

            // Wait for the welcome message from the server


            while (true) {

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
}

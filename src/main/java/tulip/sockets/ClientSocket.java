package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientSocket extends Thread {

    private final InetAddress ADDRESS;
    private final int PORT;
    private final String NAME;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;


    private final Object monitor = new Object();

    public ClientSocket(InetAddress address, int port, String name) {
        this.ADDRESS = address;
        this.PORT = port;
        this.NAME = name;
    }

    @Override
    public void run() {

        System.out.println("Launch ClientSocket");

        try {

            socket = new Socket(ADDRESS, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(Message message) {
        out.println(message.toJSON());
    }

    private void connect() {

        // Send a connection request to the server
        send(new Message(NAME, "Server", ContentType.connectionRequest, NAME));

        // Wait for the connection acknowledgement from the server
        try {
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                Message messageFromServer = Message.fromJSON(fromServer);

                // If the message received is a connection acknowledgment
                if (messageFromServer.getContentType().equals(ContentType.connectionAcknowledgement)) {
                    System.out.println("Connection successfully established");
                } else {
                    System.out.println("Connection failed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

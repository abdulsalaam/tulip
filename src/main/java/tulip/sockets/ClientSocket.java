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
    private final String CLIENT_SOCKET_NAME;
    private String serverSocketName;
    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final Object monitor = new Object();

    public ClientSocket(InetAddress address, int port, String clientSocketName) {
        this.ADDRESS = address;
        this.PORT = port;
        this.CLIENT_SOCKET_NAME = clientSocketName;
    }

    @Override
    public void run() {

        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" starting");

        try {

            socket = new Socket(ADDRESS, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Sends a connection request to the server
            send(new Message(CLIENT_SOCKET_NAME, "", ContentType.connectionRequest, CLIENT_SOCKET_NAME));

            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    Message msgFromServer = Message.fromJSON(fromServer);
                    System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" receives: " + fromServer);

                    if (!isConnected) {

                        // If the message received is a connection acknowledgment
                        if (msgFromServer.getContentType().equals(ContentType.connectionAcknowledgement)) {
                            registerServerSocket(msgFromServer.getContent());
                            System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\": connection successfully established");
                        } else {
                            System.out.println("Connection failed");
                        }

                    } else {
                        // do something
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("ClientSocket stopping" );
    }

    private void send(Message message) {
        String json = message.toJSON();
        out.println(json);
        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" sends: " + json);
    }

    private void registerServerSocket(String serverSocketName) {
        this.serverSocketName = serverSocketName;
        this.isConnected = true;
        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\": registers server socket \"" + serverSocketName + "\"");
    }
}

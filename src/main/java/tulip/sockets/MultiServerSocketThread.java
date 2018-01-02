package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiServerSocketThread extends Thread {

    private final String SERVER_SOCKET_NAME;
    private String clientSocketName;
    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiServerSocketThread(String serverSocketName, Socket socket) {
        this.SERVER_SOCKET_NAME = serverSocketName;
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" starting");

        try {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" receives: " + fromClient);
                Message msgFromClient = Message.fromJSON(fromClient);

                if (!isConnected) {

                    // If the server detects a connection request
                    if (msgFromClient.getContentType().equals(ContentType.connectionRequest)) {
                        registerClientSocket(msgFromClient.getContent());
                        // Sends a connection acknowledgment
                        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.connectionAcknowledgement, SERVER_SOCKET_NAME));
                    }
                } else {
                    // Do something
                }

            }

        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" stopping");
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
    }

    private void send(Message message) {
        String json = message.toJSON();
        out.println(json);
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" sends: " + json);
    }

    private void registerClientSocket(String clientSocketName) {
        this.clientSocketName = clientSocketName;
        this.isConnected = true;
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\": register client socket \"" + clientSocketName + "\"");

    }

}

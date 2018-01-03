package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiServerSocketThread extends Thread {

    private final MultiServerSocket MULTI_SERVER_SOCKET;
    private final String SERVER_SOCKET_NAME;
    private String clientSocketName;
    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiServerSocketThread(MultiServerSocket multiServerSocket, String serverSocketName, Socket socket) {
        this.MULTI_SERVER_SOCKET = multiServerSocket;
        this.SERVER_SOCKET_NAME = serverSocketName;
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" starting on thread " + this.getId());

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
                    } else {
                        System.out.println("Error: the client is not connected. Send a connection request first.");
                    }

                } else {

                    if (msgFromClient.getContentType().equals(ContentType.token)) {
                        int tokenValue = Integer.parseInt(msgFromClient.getContent());
                        MULTI_SERVER_SOCKET.passToken(tokenValue);
                    }

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
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\": registers client socket \"" + clientSocketName + "\"");
        this.MULTI_SERVER_SOCKET.registerConnectedClient(clientSocketName, this);
    }

    void sendToken(int tokenValue) {
        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.token, Integer.toString(tokenValue)));
    }
}

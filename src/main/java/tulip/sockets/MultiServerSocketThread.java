package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A MultiServerSocketThread handles a connection with a client socket on a specific port.
 * Each MultiServerSocketThread corresponds to a MultiServerSocket.
 */
public class MultiServerSocketThread extends Thread {

    /** The MultiServerSocket behind this MultiServerSocketThread */
    private final MultiServerSocket MULTI_SERVER_SOCKET;

    /**
     * The name that identifies the server socket on the network.
     * Please note that the MultiServerSocket and the MultiServerSocketThreads have the same name.
     * */
    private final String SERVER_SOCKET_NAME;

    /** The name that identifies the client socket the MultiServerSocketThread is communicating with */
    private String clientSocketName;

    /**
     * Indicates whether the client is registered ie. whether the client has sent a registration request to the server
     * socket
     * */
    private boolean isRegisterd = false;

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

                if (!isRegisterd) {

                    // If the server detects a registration request
                    if (msgFromClient.getContentType().equals(ContentType.registrationRequest)) {
                        registerClientSocket(msgFromClient.getContent());
                        // Sends a registration acknowledgment
                        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.registrationAcknowledgement, SERVER_SOCKET_NAME));
                    } else {
                        System.out.println("Error: the client is not registred. Send a registration request first.");
                    }

                } else {

                    // Pass the token without modifying the token value
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

    /**
     * Sends a message to the client socket through the socket output stream.
     * Takes care of converting the message to JSON first.
     * @param message The message you want to send
     */
    private void send(Message message) {
        String json = message.toJSON();
        out.println(json);
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" sends: " + json);
    }

    /**
     * When a client socket registers (ie. when a registration request is received), this methods is used to take
     * into account the registration.
     * @param clientSocketName The name of the client socket being registered
     */
    private void registerClientSocket(String clientSocketName) {
        this.clientSocketName = clientSocketName;
        this.isRegisterd = true;
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\": registers client socket \"" + clientSocketName + "\"");
        this.MULTI_SERVER_SOCKET.registerConnectedClient(clientSocketName, this);
    }

    void sendToken(int tokenValue) {
        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.token, Integer.toString(tokenValue)));
    }
}

package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A client socket communicates with a MultiServerSocketThread and follows a communication protocol
 */
public class ClientSocket extends Thread {

    /** The address of the host the client socket connects to */
    private final String HOST;

    /** The port the client socket connects to */
    private final int PORT;

    /** The name that identifies the client socket on the network */
    private final String CLIENT_SOCKET_NAME;

    /** The name that identifies the server socket the client socket is communicating with */
    private String serverSocketName;

    /**
     * Indicates whether the client socket is registered i.e. whether it has sent a registration request to the server
     * socket and received a registration acknowledgment in response.
     * */
    private boolean isRegistered = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final Object monitor = new Object();


    // Equitable producer/consumer algorithm related variables
    /** List of produced messages */
    List<Message> producedMessages = new ArrayList<>();
    /** Number of messages produced (equivalent to producedMessages length) */
    private int nbMess;
    /** Number of authorisations to send a message */
    private int nbAut;
    /** Next site to consider */
    private int successor;
    /** Total amount of places allocated to this producer */
    private int allocatedPlaces;
    /** Input index used for producedMessages */
    private int inputIndex = 0;
    /** Output index used for producedMessages */
    private int outputIndex = 0;

    public ClientSocket(String host, int port, String clientSocketName) {
        this.HOST = host;
        this.PORT = port;
        this.CLIENT_SOCKET_NAME = clientSocketName;
    }

    @Override
    public void run() {

        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" starting");

        try {

            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Sends a registration request to the server
            send(new Message(CLIENT_SOCKET_NAME, "", ContentType.registrationRequest, CLIENT_SOCKET_NAME));

            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    Message msgFromServer = Message.fromJSON(fromServer);
                    System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" receives: " + fromServer);

                    if (!isRegistered) {

                        // If the message received is a registration acknowledgment
                        if (msgFromServer.getContentType().equals(ContentType.registrationAcknowledgement)) {
                            registerServerSocket(msgFromServer.getContent());
                            System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\": successful registration");

                        // If the message received is a token, sends back the token without doing anything
                        } else if (msgFromServer.getContentType().equals(ContentType.token)) {
                            send(new Message(CLIENT_SOCKET_NAME, "", ContentType.token, msgFromServer.getContent()));
                        } else {
                            System.out.println("Connection failed");
                        }

                    } else {

                        if (msgFromServer.getContentType().equals(ContentType.token)) {

                            int tokenValue = Integer.parseInt(msgFromServer.getContent());
                            tokenValue--;

                            // Send back the token
                            send(new Message(CLIENT_SOCKET_NAME, "", ContentType.token, Integer.toString(tokenValue)));
                        }

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

    /**
     * Sends a message to the server socket through the socket output stream.
     * Takes care of converting the message to JSON first.
     * @param message The message you want to send
     */
    private void send(Message message) {
        String json = message.toJSON();
        out.println(json);
        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\" sends: " + json);
    }

    /**
     * When the client socket is registred (i.e. when a registration acknowledgment is received), this
     * methods is used to take into account the registration.
     * @param serverSocketName The name of the server socket on which the client socket registered
     */
    private void registerServerSocket(String serverSocketName) {
        this.serverSocketName = serverSocketName;
        this.isRegistered = true;
        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\": registers server socket \"" + serverSocketName + "\"");
    }

    /**
     * Producing method to be called before sending a message
     * @param m is the message to be sent
     * @throws InterruptedException associated with wait()
     */
    public void produce(Message m) throws InterruptedException {
        if(!(nbMess < allocatedPlaces)) {
            wait();
        }
        producedMessages.set(inputIndex, m);
        inputIndex = (inputIndex + 1) % allocatedPlaces;
        nbMess++;
    }

    public void onReception(){}
    public void factor(){}
}

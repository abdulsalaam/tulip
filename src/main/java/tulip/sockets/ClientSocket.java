package tulip.sockets;

import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
     * Indicates whether the client socket is registered ie. whether it has sent a registration request to the server
     * socket and received a registration acknowledgment in response.
     * */
    private boolean isRegistered = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /* Consumer-producer */

    int Ni = 10;
    Message[] Ti = new Message[Ni];
    int ini = 0;
    int outi = 0;
    int nbmessi = 0;
    int nbauti = 0;
    int tempi;
    final int SEUILi = 5;


    public void produire(Message message) {
        // Attendre (nbmessi < Ni)
        Ti[ini] = message;
        ini = (ini + 1) % Ni;
        nbmessi++;
    }

    public void sur_reception_de(int val) {
        tempi = Math.min(nbmessi - nbauti, val);
        val -= tempi;
        nbauti += tempi;
        if (val > SEUILi) {
            send(new Message(CLIENT_SOCKET_NAME, "NextProducer", ContentType.token, Integer.toString(val)));
        } else {
            send(new Message(CLIENT_SOCKET_NAME, "Consumer", ContentType.token, Integer.toString(val)));
        }
    }

    public class Facteur extends Thread {

        @Override
        public void run() {
            while (true) {
                if (nbauti > 0) {
                    send(Ti[outi]);
                    outi = (outi + 1) % Ni;
                    nbauti--;
                    nbmessi--;
                }
            }
        }
    }

    private final Object monitor = new Object();

    public ClientSocket(String host, int port, String clientSocketName) {
        this.HOST = host;
        this.PORT = port;
        this.CLIENT_SOCKET_NAME = clientSocketName;
    }

    @Override
    public void run() {

        new Facteur().start();

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

                            // Producteur-consommateur : sur_reception_de
                            int val = Integer.parseInt(msgFromServer.getContent());
                            sur_reception_de(val);

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
     * When the client socket is registred (ie. when a registration acknowledgment is received), this
     * methods is used to take into account the registration.
     * @param serverSocketName The name of the server socket on which the client socket registered
     */
    private void registerServerSocket(String serverSocketName) {
        this.serverSocketName = serverSocketName;
        this.isRegistered = true;
        System.out.println("ClientSocket \"" + CLIENT_SOCKET_NAME + "\": registers server socket \"" + serverSocketName + "\"");
    }
}

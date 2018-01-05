package tulip.service.sockets;

import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A client socket communicates with a MultiServerSocketThread and follows a communication protocol
 */
public class ClientSocket extends Thread {

    private final Producer PRODUCER;

    /** The address of the host the client socket connects to */
    private final String HOST;

    /** The port the client socket connects to */
    private final int PORT;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final Object monitor = new Object();

    public ClientSocket(Producer producer, String host, int port) {
        this.PRODUCER = producer;
        this.HOST = host;
        this.PORT = port;
    }

    @Override
    public void run() {
        System.out.println("ClientSocket starting" );

        try {

            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    uponReceipt(fromServer);
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
     * Sends a message to the server
     * @param message The message you want to send to the server
     */
    public void sendMessage(Message message) {
        String rawMessage = message.toJSON();
        out.println(rawMessage);
    }

    /**
     * Deals with the receipt of a line of text
     * @param rawMessage The message in the form a line of text
     */
    private void uponReceipt(String rawMessage) {
        Message message = Message.fromJSON(rawMessage);
        PRODUCER.uponReceipt(message);
    }
}

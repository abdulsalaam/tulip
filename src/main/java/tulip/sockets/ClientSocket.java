package tulip.sockets;

import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A client socket communicates with a MultiServerSocketThread and follows a communication protocol.
 */
public class ClientSocket extends Thread {

    private final Producer PRODUCER;
    private final Socket SOCKET;
    private PrintWriter out;
    private BufferedReader in;

    public ClientSocket(Producer producer, Socket socket) {
        this.PRODUCER = producer;
        this.SOCKET = socket;
    }

    private final Object outLock = new Object();

    @Override
    public void run() {
        System.out.println("ClientSocket starting" );

        try {
            out = new PrintWriter(SOCKET.getOutputStream(), true);
            synchronized (outLock) { outLock.notifyAll(); }
            in = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));

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
        synchronized (outLock) {
            while (out == null) {
                try {
                    outLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            out.println(rawMessage);
        }
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

package tulip.service.sockets;

import tulip.service.producerConsumer.messages.Message;

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
    private MultiServerSocket MULTI_SERVER_SOCKET;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiServerSocketThread(MultiServerSocket multiServerSocket, Socket socket) {
        this.MULTI_SERVER_SOCKET = multiServerSocket;
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocketThread starting");

        try {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            MULTI_SERVER_SOCKET.addClient(this);

            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                uponReceipt(fromClient);
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
    }

    public void sendMessage(Message message) {
        String rawMessage = message.toJSON();
        out.println(rawMessage);
    }

    private void uponReceipt(String rawMessage) {
        Message message = Message.fromJSON(rawMessage);
        MULTI_SERVER_SOCKET.uponReceipt(message);
    }
}

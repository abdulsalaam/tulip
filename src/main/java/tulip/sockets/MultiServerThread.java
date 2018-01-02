package tulip.sockets;

import java.io.*;
import java.net.Socket;

public class MultiServerThread extends Thread {

    private Socket socket;

    public MultiServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("Launch MultiServerThread");

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Server receive: " + fromClient);

                if (fromClient.equals("Bye")) {
                    System.out.println("Client quit");
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}

package tulip.sockets;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}

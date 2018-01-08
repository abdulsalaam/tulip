package tulip.service.sockets;

import org.junit.Test;
import static org.junit.Assert.*;

import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class MessageThroughSocketsTest {

    CountDownLatch latch = new CountDownLatch(2);

    Message messageSent = new Message("Sofia", Target.producer, ContentType.app,"{ \"order\" : \"hello\" }");

    @Test
    public void messageThroughSocketsTest() {

        final String HOST = "127.0.0.1";
        final int PORT = 6000;

        class Server extends Thread {

            ServerSocket serverSocket;
            Socket clientSocket;
            PrintWriter out;
            BufferedReader in;

            @Override
            public void run() {
                try {
                    System.out.println("Server starting" );

                    serverSocket = new ServerSocket(PORT);
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String fromClient;
                    while ((fromClient = in.readLine()) != null) {
                        System.out.println("Server receives: " + fromClient);
                        assertTrue(Message.fromJSON(fromClient).equals(messageSent));
                        if (Message.fromJSON(fromClient).equals(messageSent)) {
                            System.out.println("The messages match");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                    try {
                        in.close();
                        clientSocket.close();
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Server quitting" );
                latch.countDown();
            }

        }

        class Client extends Thread {

            Socket socket;
            PrintWriter out;
            BufferedReader in;

            @Override
            public void run() {
                System.out.println("Client starting" );

                try {
                    socket = new Socket(HOST, PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.write(messageSent.toJSON());
                    System.out.println("Client sends: " + messageSent.toJSON());

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

                System.out.println("Client quitting" );
                latch.countDown();
            }
        }

        new Server().start();
        new Client().start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
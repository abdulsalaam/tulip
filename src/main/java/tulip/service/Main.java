package tulip.service;

import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {

        final String HOST = "127.0.0.1";
        final int PORT = 4000;

        try {

            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket1 = new Socket(HOST, PORT);
            Socket socket2 = new Socket(HOST, PORT);

            Consumer consumer = new Consumer("0", serverSocket);
            Producer producer1 = new Producer("1", socket1);
            Producer producer2 = new Producer("2", socket2);

            int counter1 = 0;
            int counter2 = 0;
            while (true) {

                if (producer1.canProduce()) {
                    producer1.produce(new Message(Target.consumer, ContentType.app, "Echo " + counter1));
                    counter1++;
                }

                if (producer2.canProduce()) {
                    producer2.produce(
                            new Message(Target.consumer, ContentType.app, "Echo " + counter2)
                    );
                    counter2++;
                }

                if (consumer.canConsume()) {
                    consumer.consume();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

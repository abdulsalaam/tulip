package tulip.service;

import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {

        final String HOST = "127.0.0.1";
        final int PORT = 4000;

        ProducerMessenger producerMessenger = new ProducerMessenger() {
            @Override
            public void uponReceiptOfAppMessage(AppMessage appMessage) {
                System.out.println("uponReceiptOfAppMessage called");
            }
        };

        try {

            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket1 = new Socket(HOST, PORT);
            Socket socket2 = new Socket(HOST, PORT);

            Consumer consumer = new Consumer("Christopher", serverSocket);
            Producer producer1 = new Producer("Peter", socket1, producerMessenger);
            Producer producer2 = new Producer("Harrisson", socket2, producerMessenger);

            int counter1 = 0;
            int counter2 = 0;
            while (true) {

                producer1.produce(
                        new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Echo " + counter1)
                );
                counter1++;

                producer2.produce(
                        new AppMessage("Harrisson", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Echo " + counter2)
                );
                counter2++;

                consumer.consume();
                if ( ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                    try {
                        consumer.sendAppMessageTo(
                                "Harrisson",
                                new AppMessage("Christopher", ActorType.stockExchange, "Harrisson", ActorType.broker, AppMessageContentType.order, "Echo " + counter2)
                        );
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

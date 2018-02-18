package tulip.service.producerConsumer;

import org.junit.Test;
import tulip.app.common.model.appMessage.ActorType;
import tulip.app.common.model.appMessage.AppMessage;
import tulip.app.common.model.appMessage.AppMessageContentType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

public class producerConsumerTest {

    @Test
    public void producerConsumerTest() {

        final String HOST = "127.0.0.1";
        final int PORT = 4000;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket1 = new Socket(HOST, PORT);
            Socket socket2 = new Socket(HOST, PORT);

            ProducerMessenger producerMessenger = new ProducerMessenger() {
                @Override
                public void uponReceiptOfAppMessage(AppMessage appMessage) {}
            };

            Consumer consumer = new Consumer("C", serverSocket);
            Producer producer1 = new Producer("A", socket1, producerMessenger);
            Producer producer2 = new Producer("B", socket2, producerMessenger);

            AppMessage appMessageSent = new AppMessage("Peter", ActorType.client, "", ActorType.broker,
                    AppMessageContentType.order, "Application message 0");


            producer1.produce(appMessageSent);

            while (true) {
                AppMessage appMessageReceived = consumer.consume();
                if (appMessageReceived != null) {
                    assertTrue(appMessageReceived.equals(appMessageSent));
                    break;
                }
            }

            AppMessage[] appMessagesSent = {
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 1"),
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 2"),
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 3"),
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 4")
            };

            producer1.produce(appMessagesSent[0]);
            producer2.produce(appMessagesSent[1]);
            producer2.produce(appMessagesSent[2]);
            producer1.produce(appMessagesSent[3]);

            AppMessage[] appMessagesReceived = new AppMessage[4];
            int index = 0;

            while (true) {
                AppMessage appMessage = consumer.consume();
                if (appMessage != null) {
                    appMessagesReceived[index++] = appMessage;
                }

                if (index == 4) {
                    break;
                }
            }

            int k = 0;
            for (AppMessage appMessSent: appMessagesSent) {
                System.out.println("App message sent: " + appMessSent.toJSON());
                for (AppMessage appMessReceived: appMessagesReceived) {
                    System.out.println("App message received: " + appMessReceived.toJSON());
                    if (appMessSent.equals(appMessReceived)) {
                        k++;
                    }
                }
            }

            assertTrue(k == 4);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
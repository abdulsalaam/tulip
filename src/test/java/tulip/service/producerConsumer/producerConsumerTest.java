package tulip.service.producerConsumer;

import org.junit.Test;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.util.concurrent.CountDownLatch;

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

            CountDownLatch latch = new CountDownLatch(1);

            AppMessage uponReceiptOfAppMessageTest =
                    new AppMessage("Quentin", ActorType.stockExchange, "Peter", ActorType.broker, AppMessageContentType.order, "Consumer message ");


                    ProducerMessenger producerMessenger = new ProducerMessenger() {
                @Override
                public void uponReceiptOfAppMessage(AppMessage appMessage) {
                    assertTrue(appMessage.equals(uponReceiptOfAppMessageTest));
                    System.out.println("uponReceiptOfAppMessage succeeded");
                    latch.countDown();
                }
            };

            Consumer consumer = new Consumer("Quentin", serverSocket);
            Producer peter = new Producer("Peter", socket1, producerMessenger);
            Producer david = new Producer("David", socket2, producerMessenger);

            AppMessage appMessageSent = new AppMessage("Peter", ActorType.client, "", ActorType.broker,
                    AppMessageContentType.order, "Application message 0");


            peter.produce(appMessageSent);

            while (true) {
                AppMessage appMessageReceived = consumer.consume();
                if (appMessageReceived != null) {
                    assertTrue(appMessageReceived.equals(appMessageSent));
                    break;
                }
            }

            AppMessage[] appMessagesSent = {
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 1"),
                    new AppMessage("David", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 2"),
                    new AppMessage("David", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 3"),
                    new AppMessage("Peter", ActorType.client, "", ActorType.broker, AppMessageContentType.order, "Application message 4")
            };

            peter.produce(appMessagesSent[0]);
            david.produce(appMessagesSent[1]);
            david.produce(appMessagesSent[2]);
            peter.produce(appMessagesSent[3]);

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

            while (true) {
                try {
                    consumer.sendAppMessageTo("Peter", uponReceiptOfAppMessageTest);
                    break;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
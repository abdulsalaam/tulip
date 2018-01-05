package tulip.service.producerConsumer;

import org.junit.Test;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ConsumerProducerTest {

    @Test
    public void consumerProducerTest() {

        Consumer consumer = new Consumer("0");
        Producer producer1 = new Producer("1");
        Producer producer2 = new Producer("2");

        Message m1 = new Message(Target.consumer, ContentType.app, "Message 1");
        producer1.produire(m1);

        while (true) {
            if (consumer.peutConsommer()) {
                assertTrue(consumer.consommer().equals(m1));
                break;
            }
        }

        Message m2 = new Message(Target.consumer, ContentType.app, "Message 2");
        Message m3 = new Message(Target.consumer, ContentType.app, "Message 3");
        Message m4 = new Message(Target.consumer, ContentType.app, "Message 4");
        Message m5 = new Message(Target.consumer, ContentType.app, "Message 5");

        Message[] messagesSent = {m2, m3, m4, m5};

        producer1.produire(m2);
        producer2.produire(m3);
        producer2.produire(m4);
        producer1.produire(m5);

        Message[] messagesReceived = new Message[4];
        int index = 0;

        while (true) {
            if (consumer.peutConsommer()) {
                messagesReceived[index++] = consumer.consommer();
            }

            if (index == 4) {
                break;
            }
        }

        int k = 0;
        for (Message mSent: messagesSent) {
            System.out.println("Message sent: " + mSent.toJSON());
            for (Message mReceived: messagesReceived) {
                System.out.println("Message received: " + mReceived.toJSON());
                if (mSent.equals(mReceived)) {
                    k++;
                }
            }
        }

        assertTrue(k == 4);
    }
}
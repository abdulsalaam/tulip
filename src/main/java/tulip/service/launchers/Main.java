package tulip.service.launchers;

import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

public class Main {

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {

        Consumer consumer = new Consumer("0");
        Producer producer1 = new Producer("1");
        Producer producer2 = new Producer("2");

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

    }

}

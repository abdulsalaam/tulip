package tulip.service.launchers;

import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

public class Main {

    public static void main(String[] args) {
        try {
            Consumer consumer = new Consumer("0");
            Producer producer1 = new Producer("1");
            Producer producer2 = new Producer("2");

                if (producer1.canProduce()) {
                    producer1.produire(new Message(Target.consumer, ContentType.app, "Echo " + 1));
                }

                if (consumer.peutConsommer()) {
                    System.out.println("Consume " + consumer.consommer());
                }

//                if (producer2.canProduce()) {
//                    producer2.produire(
//                            new Message(Target.consumer, ContentType.app, "Echo " + 2)
//                    );
//                }
//
//                if (consumer.peutConsommer()) {
//                    System.out.println("Consume " + consumer.consommer());
//                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

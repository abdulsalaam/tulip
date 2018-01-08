package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.Message;

public interface ProducerMessenger {

    /**
     * This method is called whenever an app message is received by a producer.
     * NB: Since the producer-consumer system is a unidirectional system, the message received via this method does
     * not take advantage of the flow control.
     * @param message
     */
    public void uponReceiptOfAppMessage(Message message);

}

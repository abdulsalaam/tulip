package tulip.service.producerConsumer;

import tulip.app.appMessage.AppMessage;

/**
 * This method must be implemented by the objects using the class Producer, in order for the producer to be able
 * to perform task on message receipt.
 */
public interface ProducerMessenger {

    /**
     * This method is called whenever an app message is received by a producer.
     * NB: Since the producer-consumer system is a unidirectional system, the message received via this method do
     * not take advantage of the flow control.
     * @param appMessage The appMessage received
     */
    public void uponReceiptOfAppMessage(AppMessage appMessage);

}

package tulip.service.producerConsumer.messages;

/**
 * Describes the type of contents that can be sent on the service layer with messages.
 * token describes a message that is use by the producer-consumer system.
 * app describes a message that corresponds to the application layer.
 */
public enum ContentType {
    token,
    app
}

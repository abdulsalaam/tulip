package tulip.app.common.model.appMessage;

/**
 * The different types of app messages
 */
public enum AppMessageContentType {
    registrationRequest,
    registrationAcknowledgment,
    registrationNotification,
    marketStateRequest,
    marketStateReply,
    order,
    orderProcessed,
    endOfDayNotification
}

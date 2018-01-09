package tulip.app.appMessage;

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

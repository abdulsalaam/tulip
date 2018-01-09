package tulip.app.appMessage;

/**
 * The different types of app messages
 */
public enum AppMessageContentType {
    registrationRequest,
    registrationAcknowledgment,
    marketStateRequest,
    marketStateReply,
    order,
    orderProcessed,
    endOfDayNotification
}

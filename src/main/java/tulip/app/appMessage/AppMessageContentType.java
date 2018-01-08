package tulip.app.appMessage;

public enum AppMessageContentType {
    registrationRequest,
    registrationAcknowledgment,
    marketStateRequest,
    marketStateReply,
    purchaseOrder,
    sellOrder,
    purchaseOrderProcessed,
    sellOrderProcessed,
    endOfDayNotification
}

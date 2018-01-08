package tulip.sockets.messages;

/**
 * Describes the different types of contents that can be sent on the service layer (i.e. over socket communication)
 */
public enum ContentType {
    registrationRequest,
    registrationAcknowledgement,
    token,
    order
}

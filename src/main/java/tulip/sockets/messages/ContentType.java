package tulip.sockets.messages;

/**
 * Describes the different types of contents that can be sent on the service layer (ie. over socket communication)
 */
public enum ContentType {
    registrationClientRequest,
    registrationClientAcknowledgement,
    
    registrationBrokerRequest,
    registrationBrokerAcknowledgement,
    
    token,
    
    order, 
    orderAcknowledgment, 
    orderImpossible, 
    agreementAcknowledgment, 
    purchasedWithFloatingStocksAcknowledgment,
    
    stateMarket,
    stateMarketAcknowledgment,
    
    updateStockPrice, 
    
    deconnect, 
    deconnectAcknowledgment
}

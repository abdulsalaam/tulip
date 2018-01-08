package tulip.app.broker.model;

import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.client.model.Client;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Broker implements ProducerMessenger {

    /** Name of the broker */
    private String name;
    /** Tells whether the broker is registered to the stock exchange or not */
    private boolean isRegistered;
    /** Amount of cash available to the broker */
    private double cash;
    /** Commission rate applied by the broker on each transaction */
    private double commissionRate;
    /** Clients list of the broker */
    private List<String> clients = new ArrayList<>();
    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private List<Order> pendingOrders = new ArrayList<>();
    /** Counts the selling orders proceeded by the broker */
    private int sellOrderCounter;
    /** Counts the purchase orders proceeded by the broker */
    private int purchaseOrderCounter;

    private Map<String, Double> marketState = new HashMap<>();

    private List<String> clientsRequestingRegistration = new ArrayList<>();

    private List<String> clientsRegistered = new ArrayList<>();

    ServerSocket serverSocket;
    Consumer brokerConsumer = new Consumer("broker", serverSocket);
    Socket socket;
    Producer brokerProducer = new Producer("broker", socket, this);


    /**
     * Constructor
     * @param name is the name of the broker
     */
    public Broker(String name) {
        this.name = name;
        this.isRegistered = false;
        this.cash = 0;
        this.commissionRate = 0.1;
        this.clients = new ArrayList<>();
        this.pendingOrders = new ArrayList<>();
    }

    /**
     * Sends registering request to stock exchange
     */
    public void registerToStockExchange() {
        if (brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.registrationRequest, this.name
            ));
        }
    }

    /**
     * Registers a client
     */
    public void registerClient(String clientName){
        //String clientToRegister = clientsRequestingRegistration.get(0);

        if(brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, clientName, ActorType.client, AppMessageContentType.registrationAcknowledgment, ""
            ));
        }
    }

    /**
     * Sends request to stock exchange in order to retrieve
     * market state information
     */
    public void requestMarketState(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.marketStateRequest, ""
            ));
        }
    }

    /**
     * Checks whether a client is registered or not
     * @return true if the client is registered, false otherwise
     */
    public boolean checkClientRegistered(String clientName){
        for(String s : clientsRegistered) {
            if(s.equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Proceeds a sell order
     * @param client is the name of the ordering client
     * @param nbOfStocks is the number of stocks the client wants to sell
     * @param minSellingPrice is the minimum price to which the client is willing to sell
     */
    public void placeSellOrder(String company, String client, int nbOfStocks, double minSellingPrice){
        pendingOrders.add(
                new Order(++sellOrderCounter, OrderType.purchase, company, client, name, minSellingPrice, nbOfStocks)
        );
    }

    /**
     * Proceeds a purchase order
     * @param client is the name of the ordering client
     * @param nbOfStocks is the number of stocks requested by the client
     * @param maxPurchasingPrice is the maximum price to which the client is willing to buy
     */
    public void placePurchaseOrder(String company, String client, int nbOfStocks, double maxPurchasingPrice) {
        pendingOrders.add(
                new Order(++purchaseOrderCounter, OrderType.purchase, company, client, name, maxPurchasingPrice, nbOfStocks)
        );
    }

    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    public void notifyOfTransaction(String clientName){
        if(brokerProducer.canProduce()){
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, clientName, ActorType.client, AppMessageContentType.purchaseOrder, ""

            ));
        }
    }


    // Improvement : put getActualAmount in Order class
    /**
     * After proceeding an order, the broker calculates its commission and updates
     * his cach accordingly
     * @param order the order from which the commission is calculated
     */
    public void calculateCommission(Order order){
        double commission = order.getActualAmount() * commissionRate;
        this.cash += commission;
    }


    /**
     * Sends to the client the updated stock values
     */
    public void notifyClientsOfPriceChanges(){

    }

    /**
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    public void closeTheDay(){

    }

    @Override
    public void uponReceiptOfAppMessage(AppMessage message) {
        switch (message.getAppMessageContentType()){

            case sellOrder:
                Order order = new Order(0, null, null, null, null, 0, 0);
                order.fromJSON(message.toJSON());
                break;

            case purchaseOrder:
                // Add to pending orders
                break;

            case marketStateReply:
                // Add all elements of the map to this map
                break;

            case marketStateRequest:
                if (brokerProducer.canProduce()) {
                    brokerProducer.produce(new AppMessage(
                            this.name, ActorType.broker, message.getSender(), ActorType.client, AppMessageContentType.marketStateReply, marketState.toString()

                    ));
                }
                break;

            case registrationAcknowledgment:
                this.isRegistered = true;
                break;

            case registrationRequest:
                registerClient(message.getSender());
                break;

            case endOfDayNotification:
                // Add to list



        }
    }
}
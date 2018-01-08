package tulip.app.broker.model;

import tulip.app.MarketState;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

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
    /** (Registered) clients list of the broker */
    private List<String> clients = new ArrayList<>();
    /** (Not yet registered) prospects of the broker requesting registration */
    private List<String> clientsRequestingRegistration = new ArrayList<>();
    /** Clients of the broker who closed the day */
    private List<String> closedClients = new ArrayList<>();
    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private List<Order> pendingOrders = new ArrayList<>();
    /** Counts the selling orders proceeded by the broker */
    private int sellOrderCounter;
    /** Counts the purchase orders proceeded by the broker */
    private int purchaseOrderCounter;
    /** Current market state (list of companies and associated prices */
    private MarketState marketState = new MarketState();



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
        for(String s : clients) {
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
        if(checkClientRegistered(client)) {
            Order sellOrder = new Order(++sellOrderCounter, OrderType.purchase, company, client, name, minSellingPrice, nbOfStocks);
            pendingOrders.add(sellOrder);
            calculateCommission(sellOrder);
        }
    }

    /**
     * Proceeds a purchase order
     * @param client is the name of the ordering client
     * @param nbOfStocks is the number of stocks requested by the client
     * @param maxPurchasingPrice is the maximum price to which the client is willing to buy
     */
    public void placePurchaseOrder(String company, String client, int nbOfStocks, double maxPurchasingPrice) {
        Order purchaselOrder = new Order(++sellOrderCounter, OrderType.purchase, company, client, name, maxPurchasingPrice, nbOfStocks);
        pendingOrders.add(purchaselOrder);
        calculateCommission(purchaselOrder);
    }

    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfTransaction(String clientName){
        if(brokerProducer.canProduce()){
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, clientName, ActorType.client, AppMessageContentType.purchaseOrder, ""

            ));
        }
    }

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
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    public void closeTheDay(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.endOfDayNotification, ""
            ));
        }
    }

    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {
        switch (appMessage.getAppMessageContentType()){

            case sellOrder:
                Order sellOrder = Order.fromJSON(appMessage.getContent());
                pendingOrders.add(sellOrder);
                break;

            case purchaseOrder:
                Order purchaseOrder = Order.fromJSON(appMessage.getContent());
                pendingOrders.add(purchaseOrder);
                break;

            case marketStateReply:
                marketState = MarketState.fromJSON(appMessage.getContent());
                break;

            case marketStateRequest:
                if (brokerProducer.canProduce()) {
                    brokerProducer.produce(new AppMessage(
                            this.name, ActorType.broker, appMessage.getSender(), ActorType.client, AppMessageContentType.marketStateReply, marketState.toJSON()
                    ));
                }
                break;

            case registrationAcknowledgment:
                this.isRegistered = true;
                break;

            case registrationRequest:
                registerClient(appMessage.getSender());
                break;

            case endOfDayNotification:
                closedClients.add(appMessage.getContent());
                break;

            case sellOrderProcessed:
                Order ProcessedPurchaseOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(ProcessedPurchaseOrder.getClient());
                break;

            case purchaseOrderProcessed:
                Order ProcessedSellOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(ProcessedSellOrder.getClient());
                break;
        }
    }
}
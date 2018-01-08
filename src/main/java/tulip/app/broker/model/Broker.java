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
            while(!isRegistered) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Registers a client
     */
    private void registerClient(String clientName) {
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
    public void requestMarketState() {
        if(brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.marketStateRequest, ""
            ));
        }
    }

    /**
     * Checks whether a client is registered or not
     * @return true if the client is registered, false otherwise
     */
    private boolean checkClientRegistered(String clientName) {
        for(String s : clients) {
            if(s.equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Proceeds a sell order
     */
    public void placeSellOrder() {
        String sellOrderJson = pendingOrders.get(0).toJSON();
        if(brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.sellOrder, sellOrderJson
            ));
        }
        pendingOrders.remove(0);
    }

    /**
     * Proceeds a purchase order
     */
    public void placePurchaseOrder() {
        String purchaseOrderJson = pendingOrders.get(0).toJSON();
        if(brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.sellOrder, purchaseOrderJson
            ));
        }
        pendingOrders.remove(0);

    }

    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfTransaction(String clientName) {
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
    public void calculateCommission(Order order) {
        double commission = order.getActualAmount() * commissionRate;
        this.cash += commission;
    }

    /**
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    public void closeTheDay(){
        if(brokerProducer.canProduce()) {
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
                calculateCommission(ProcessedPurchaseOrder);
                break;

            case purchaseOrderProcessed:
                Order ProcessedSellOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(ProcessedSellOrder.getClient());
                calculateCommission(ProcessedSellOrder);
                break;
        }
    }
}
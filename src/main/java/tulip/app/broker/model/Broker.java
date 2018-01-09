package tulip.app.broker.model;

import tulip.app.MarketState;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.exceptions.RegistrationException;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

import java.io.IOException;
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
    /** Consumer role of the broker */
    Consumer brokerConsumer;
    /** Producer role of the broker */
    Producer brokerProducer;

    /**
     * Constructor
     * @param name is the name of the broker
     */
    public Broker(String name, ServerSocket serverSocket, Socket socket) {
        this.name = name;
        this.isRegistered = false;
        this.cash = 0;
        this.commissionRate = 0.1;
        this.clients = new ArrayList<>();
        this.pendingOrders = new ArrayList<>();
        brokerConsumer = new Consumer(this.name, serverSocket);
        brokerProducer = new Producer(this.name, socket, this);
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
    private void registerClient(String clientName)
            throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

        if (brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, clientName, ActorType.client, AppMessageContentType.registrationAcknowledgment, ""
            ));
        }
    }

    /**
     * Sends request to stock exchange in order to retrieve
     * market state information
     */
    public void requestMarketState() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

        if (brokerProducer.canProduce()) {
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
            if (s.equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Proceeds an order
     */
    public void placeOrder()
        throws RegistrationException {

            if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

        String orderJson = pendingOrders.get(0).toJSON();
        if(brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.order, orderJson
            ));
        }
        pendingOrders.remove(0);
    }


    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfTransaction(String clientName) {
        if (brokerProducer.canProduce()){
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, clientName, ActorType.client, AppMessageContentType.order, ""

            ));
        }
    }

    /**
     * After proceeding an order, the broker calculates its commission and updates
     * his cach accordingly
     * @param order the order from which the commission is calculated
     */
    private void calculateCommission(Order order) {
        double commission = order.getActualAmount() * commissionRate;
        this.cash += commission;
    }

    /**
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    private void closeTheDay(){
        if (brokerProducer.canProduce()) {
            brokerProducer.produce(new AppMessage(
                    this.name, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.endOfDayNotification, ""
            ));
        }
    }

    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {
        switch (appMessage.getAppMessageContentType()) {

            case order:
                if(checkClientRegistered(appMessage.getSender())) {
                    Order order = Order.fromJSON(appMessage.getContent());
                    pendingOrders.add(order);
                }
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

            case orderProcessed:
                Order processedOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(processedOrder.getClient());
                calculateCommission(processedOrder);
                break;

        }
    }

    public List<Order> getPendingOrders() {
        pendingOrders.add(new Order(23, OrderType.purchase, "Basecamp", "Titus", this.name, 120, 350));
        pendingOrders.add(new Order(23, OrderType.purchase, "Alphabet", "Bobo", this.name, 180, 950));
        pendingOrders.add(new Order(23, OrderType.purchase, "Sony", "Leonardo", this.name, 100, 390));
        return pendingOrders;
    }

    public MarketState getMarketState() {
        marketState.put("Basecamp", 250.0);
        marketState.put("Tesla", 596.70);
        marketState.put("Facebook", 450.0);
        marketState.put("Alphabet", 270.0);
        marketState.put("Apple", 430.0);
        marketState.put("Spotify", 220.0);
        marketState.put("LVMH", 550.0);
        marketState.put("Ecosia", 120.0);
        marketState.put("Biocop", 140.0);
        marketState.put("Veolia", 245.8);
        marketState.put("Samsung", 240.0);
        return marketState;
    }
}
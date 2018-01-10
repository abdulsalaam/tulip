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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Broker extends Thread implements ProducerMessenger {

    /** Name of the broker */
    private final String NAME;

    /** Tells whether the broker is registered to the stock exchange or not */
    private boolean isRegistered = false;

    /** Amount of cash available to the broker */
    private double cash = 0;

    /** Commission rate applied by the broker on each transaction */
    private final double COMMISSION_RATE = 0.1;

    /** (Registered) clients list of the broker */
    private List<String> clients = new ArrayList<>();

    /** (Not yet registered) prospects of the broker requesting registration */
    private List<String> clientsRequestingRegistration = new ArrayList<>();

    /** Clients of the broker who closed the day */
    private List<String> closedClients = new ArrayList<>();

    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private List<Order> pendingOrders = new ArrayList<>();

    /** Counts the selling orders proceeded by the broker */
    private int sellOrderCounter = 0;

    /** Counts the purchase orders proceeded by the broker */
    private int purchaseOrderCounter = 0;

    /** Current market state (list of companies and associated prices */
    private MarketState marketState = new MarketState();

    /** Consumer role of the broker */
    private Consumer consumer;

    /** Producer role of the broker */
    private Producer producer;

    /**
     * Constructor
     * @param name is the name of the broker
     */
    public Broker(String name, ServerSocket serverSocket, Socket socket) {
        this.NAME = name;
        this.consumer = new Consumer(this.NAME, serverSocket);
        this.producer = new Producer(this.NAME, socket, this);
    }

    /**
     * Consumes messages while he can, and treats the ones received from the client,
     * following the producer-consumer algorithm used
     */
    @Override
    public void run() {

        registerToStockExchange();

        while (true) {
            if (consumer.canConsume()) {
                AppMessage appMessage = consumer.consume();
                switch (appMessage.getAppMessageContentType()) {

                    case order:
                        if(checkClientRegistered(appMessage.getSender())) {
                            Order order = Order.fromJSON(appMessage.getContent());
                            pendingOrders.add(order);
                        }
                        break;

                    case marketStateRequest:
                        if (producer.canProduce()) {
                            producer.produce(new AppMessage(
                                    this.NAME, ActorType.broker, appMessage.getSender(), ActorType.client, AppMessageContentType.marketStateReply, marketState.toJSON()
                            ));
                        }
                        break;

                    case registrationRequest:
                        registerClient(appMessage.getSender());
                        break;

                    case endOfDayNotification:
                        closedClients.add(appMessage.getContent());
                        break;

                }
            }
        }
    }
    /**
     * Registers the broker to the stock exchange
     */
    public void registerToStockExchange() {

        // Loop until the broker is registered
        while (!isRegistered) {
            System.out.println("Broker " + NAME + " is trying to register");

            // Sends registration request
            if (producer.canProduce()) {
                producer.produce(new AppMessage(
                        this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.registrationRequest, this.NAME
                ));
            }

            // Sleeps
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Consumes message
            if (consumer.canConsume()) {
                AppMessage appMessage = consumer.consume();
                if (appMessage.getAppMessageContentType().equals(AppMessageContentType.registrationAcknowledgment)) {
                    isRegistered = true;
                }
            }
        }

    }

    /**
     * Registers a client
     */
    private void registerClient(String clientName) throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

        // todo: check if client is not already registered and registered client
        if(!(clients.contains(clientName))) {
            clients.add(clientName);
            if (producer.canProduce()) {
                producer.produce(new AppMessage(
                        this.NAME, ActorType.broker, clientName, ActorType.client, AppMessageContentType.registrationAcknowledgment, ""
                ));
                producer.produce(new AppMessage(
                        this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.registrationNotification, clientName
                ));
            }
        }
    }

    /**
     * Sends request to stock exchange in order to retrieve
     * market state information
     */
    public void requestMarketState() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

        if (producer.canProduce()) {
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.marketStateRequest, ""
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
        if(producer.canProduce()) {
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.order, orderJson
            ));
        }
        pendingOrders.remove(0);
    }


    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfTransaction(String clientName) {
        if (producer.canProduce()){
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, clientName, ActorType.client, AppMessageContentType.order, ""

            ));
        }
    }

    /**
     * After proceeding an order, the broker calculates its commission and updates
     * his cach accordingly
     * @param order the order from which the commission is calculated
     */
    private void calculateCommission(Order order) {
        double commission = order.getActualAmount() * COMMISSION_RATE;
        this.cash += commission;
    }

    /**
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    private void closeTheDay(){
        if (producer.canProduce()) {
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.endOfDayNotification, ""
            ));
        }
    }

    /**
     * Treats acquaintances received from stock exchange
     */
    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {

        switch (appMessage.getAppMessageContentType()) {

            case marketStateReply:
                marketState = MarketState.fromJSON(appMessage.getContent());
                break;

            case registrationAcknowledgment:
                this.isRegistered = true;
                break;

            case orderProcessed:
                Order processedOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(processedOrder.getClient());
                calculateCommission(processedOrder);
                break;

        }
    }

    public List<Order> getPendingOrders() {
        pendingOrders.add(new Order(23, OrderType.purchase, "Basecamp", "Titus", this.NAME, 120, 350));
        pendingOrders.add(new Order(23, OrderType.purchase, "Alphabet", "Bobo", this.NAME, 180, 950));
        pendingOrders.add(new Order(23, OrderType.purchase, "Sony", "Leonardo", this.NAME, 100, 390));
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
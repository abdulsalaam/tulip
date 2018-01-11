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

    /** Registered clients of the broker */
    private List<String> clients = new ArrayList<>();

    /** Clients of the broker who closed the day */
    private List<String> closedClients = new ArrayList<>();

    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private List<Order> pendingOrders = new ArrayList<>();

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
            AppMessage appMessage = consumer.consume();
            if (appMessage != null) {

                switch (appMessage.getAppMessageContentType()) {

                    case order:
                        if(checkClientRegistered(appMessage.getSender())) {
                            Order order = Order.fromJSON(appMessage.getContent());
                            pendingOrders.add(order);
                        }
                        break;

                    case marketStateRequest:
                        consumer.sendAppMessageTo(appMessage.getSender(),
                                new AppMessage(this.NAME, ActorType.broker, appMessage.getSender(), ActorType.client, AppMessageContentType.marketStateReply, marketState.toJSON()
                                ));

                        break;

                    case registrationRequest:
                        registerClient(appMessage.getSender());
                        break;

                    case endOfDayNotification:
                        closedClients.add(appMessage.getContent());
                        if (closedClients.size() == clients.size() && closedClients.size() != 0) {
                            closeTheDay();
                        }
                        break;

                }
            }
        }
    }

    /**
     * Treats app messages received from stock exchange
     */
    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {

        switch (appMessage.getAppMessageContentType()) {

            case registrationAcknowledgment:
                if (!isRegistered) {
                    this.isRegistered = true;
                    System.out.println("Broker " + NAME + " is now registered");
                }
                break;

            case marketStateReply:
                marketState = MarketState.fromJSON(appMessage.getContent());
                break;

            case orderProcessed:
                Order processedOrder = Order.fromJSON(appMessage.getContent());
                notifyOfTransaction(processedOrder.getClient());
                calculateCommission(processedOrder);
                break;

        }
    }

    /**
     * Registers the broker to the stock exchange
     */
    public void registerToStockExchange() {

        // Loop until the broker is registered
        while (!isRegistered) {

            // Sends registration request
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.registrationRequest, this.NAME
            ));

            System.out.println("Broker " + NAME + " is trying to register");

            // Sleeps
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Registers a client. If the broker is not yet registered, this method does nothing
     * @param clientName The name of the client to register
     */
    private void registerClient(String clientName) {

        if (isRegistered) {

            // If needed adds the client to the client list
            if (!clients.contains(clientName)) { clients.add(clientName); }

            // Sends a registration acknowledgement to the client
            consumer.sendAppMessageTo(
                    clientName,
                    new AppMessage(
                            this.NAME, ActorType.broker, clientName, ActorType.client,
                            AppMessageContentType.registrationAcknowledgment, ""
                    )
            );

            // Indicates to the stock exchange that the broker has a new client
            producer.produce(
                    new AppMessage(
                            this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange,
                            AppMessageContentType.registrationNotification, clientName
                    ));

        } else {
            System.out.println("Impossible to register the client, the broker is not yet registered");
        }
    }

    /**
     * Sends request to stock exchange in order to retrieve
     * market state information
     */
    public void requestMarketState() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The broker is not registered"); }

            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.marketStateRequest, ""
            ));

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
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.order, orderJson
            ));

        pendingOrders.remove(0);
    }


    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfTransaction(String clientName) {
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, clientName, ActorType.client, AppMessageContentType.order, ""

            ));

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
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.endOfDayNotification, ""
            ));
    }

    public List<Order> getPendingOrders() {
        return pendingOrders;
    }

    public MarketState getMarketState() {
        return marketState;
    }
}
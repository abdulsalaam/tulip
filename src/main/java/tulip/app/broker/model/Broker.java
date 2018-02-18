package tulip.app.broker.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import tulip.app.common.model.MarketState;
import tulip.app.common.model.appMessage.ActorType;
import tulip.app.common.model.appMessage.AppMessage;
import tulip.app.common.model.appMessage.AppMessageContentType;
import tulip.app.common.model.exceptions.RegistrationException;
import tulip.app.common.model.order.Order;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Broker implements Runnable, ProducerMessenger {

    /** Name of the broker */
    private final String NAME;

    /** Tells whether the broker is registered to the stock exchange or not */
    private BooleanProperty isRegistered = new SimpleBooleanProperty(false);

    /** Amount of cash available to the broker */
    private double cash = 0;

    /** Commission rate applied by the broker on each transaction */
    private final double COMMISSION_RATE = 0.1;

    /** Registered clients of the broker */
    private List<String> clients = new ArrayList<>();

    /** Clients of the broker who closed the day */
    private List<String> closedClients = new ArrayList<>();

    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private Queue<Order> pendingOrders = new LinkedList<>();

    /** Current market state (list of companies and associated prices */
    private MarketState marketState = new MarketState();

    /** Consumer role of the broker */
    private Consumer consumer;

    /** Producer role of the broker */
    private Producer producer;

    private final Object marketStateLock = new Object();

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
     * Consumes messages while he can, and processes the ones received from the client,
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
                        getMarketState();
                        consumer.sendAppMessageTo(appMessage.getSender(),
                                new AppMessage(this.NAME, ActorType.broker, appMessage.getSender(),
                                        ActorType.client, AppMessageContentType.marketStateReply, marketState.toJSON()
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
     * Processes app messages received from the stock exchange
     */
    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {

        switch (appMessage.getAppMessageContentType()) {

            case registrationAcknowledgment:
                if (!getIsRegistered()) {
                    setIsRegistered(true);
                    System.out.println("Broker " + NAME + " is now registered");
                }
                break;

            case marketStateReply:
                marketState = MarketState.fromJSON(appMessage.getContent());
                synchronized (marketStateLock) {
                    marketStateLock.notifyAll();
                }
                break;

            case orderProcessed:
                Order processedOrder = Order.fromJSON(appMessage.getContent());
                notifyOfClientTransaction(processedOrder.getClient(), processedOrder);
                calculateCommission(processedOrder);
                break;

        }
    }

    /**
     * Registers the broker to the stock exchange
     */
    public void registerToStockExchange() {

        // Loop until the broker is registered
        while (!getIsRegistered()) {

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

        if (getIsRegistered()) {

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
    public void requestMarketState() {
        if (getIsRegistered()) {
            producer.produce(new AppMessage(
                    this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange, AppMessageContentType.marketStateRequest, ""
            ));
        } else {
            System.err.println("The broker is not registered");
        }
    }

    /**
     * Checks whether a client is registered or not
     * @return true if the client is registered, false otherwise
     */
    private boolean checkClientRegistered(String clientName) {
        for (String s : clients) {
            if (s.equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Places an order
     */
    public void placeOrder() throws RegistrationException, IndexOutOfBoundsException {

        if (!getIsRegistered()) { throw new RegistrationException("The broker is not registered"); }

        Order order = pendingOrders.poll();
        if (order == null) { throw new IndexOutOfBoundsException(); }

        producer.produce(new AppMessage(
                this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange,
                AppMessageContentType.order, order.toJSON()
        ));
    }


    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    private void notifyOfClientTransaction(String clientName, Order order) {
        consumer.sendAppMessageTo(
                clientName,
                new AppMessage(
                        this.NAME, ActorType.broker, clientName, ActorType.client, AppMessageContentType.order, order.toJSON()

                )
        );
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
                this.NAME, ActorType.broker, "stockExchange", ActorType.stockExchange,
                AppMessageContentType.endOfDayNotification, ""
        ));
    }

    public List<Order> getPendingOrders() {
        @SuppressWarnings("unchecked")
        List<Order> list = (List<Order>) pendingOrders;
        return Collections.unmodifiableList(list);
    }

    public HashMap<String, Double> getMarketState() {
        marketState = null;
        synchronized (marketStateLock) {
            while (marketState == null) {
                requestMarketState();
                try {
                    marketStateLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return marketState;
    }

    public List<String> getClients() {
        return Collections.unmodifiableList(clients);
    }

    public String getName() {
        return NAME;
    }

    public final boolean getIsRegistered() {
        return isRegistered.get();
    }

    public final void setIsRegistered(boolean value) {
        isRegistered.set(value);
    }

    public final BooleanProperty isRegisteredProperty() {
        return isRegistered;
    }
}
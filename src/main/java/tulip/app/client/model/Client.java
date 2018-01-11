package tulip.app.client.model;

import tulip.app.MarketState;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.client.view.ClientUI;
import tulip.app.exceptions.IllegalOrderException;
import tulip.app.exceptions.RegistrationException;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * This class represents a client
 */
public class Client implements Runnable, ProducerMessenger {

    /** The name of the client (unique identifier) */
    private final String NAME;

    /**
     * The portfolio of the client ie. a map that associates the name of a company with the number of stocks owned by the
     * client.
     * */
    private Portfolio portfolio = new Portfolio();

    /** The name of the broker of this client (unique identifier) */
    private String broker;

    /** Indicates whether the client is registered to a broker */
    private boolean isRegistered = false;

    /** The amount of cash of the client */
    private double cash;

    /** The list of the sell orders placed by the client and not treated yet */
    private List<Order> pendingSellOrders = new ArrayList<>();

    /** The list of the sell purchased placed by the client and not treated yet */
    private List<Order> pendingPurchaseOrders = new ArrayList<>();

    /** The commission rate of the broker */
    private final double COMMISSION_RATE = 0.1;

    /** The number of sell orders made so far. Used to generate an id for each sell order */
    private int sellOrderCounter = 0;

    /** The number of purchase order. Used to generate an id for each sell order */
    private int purchaseOrderCounter = 0;

    /** The current market state (list of companies and associated prices) */
    private MarketState marketState = null;

    /** The producer corresponding to the client */
    private Producer producer;

    private final Object marketStateLock = new Object();

    /**
     * The constructor of the client
     * @param name The name of the client (unique identifier)
     * @param cash The amount of cash the client has
     * @param socket A socket used to communicate with a broker
     */
    public Client(String name, double cash, Socket socket) {
        this.cash = cash;
        this.NAME = name;
        this.producer = new Producer(name, socket, this);
    }

    @Override
    public void run() {
        registerToBroker();
    }

    /**
     * This method is called upon receipt of a message by the producer
     * @param appMessage The message being received
     */
    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {

        switch (appMessage.getAppMessageContentType()) {

            case registrationAcknowledgment:
                if (!isRegistered) {
                    this.isRegistered = true;
                    this.broker = appMessage.getSender();
                    System.out.println("Client " + NAME + " is now to registered");
                }
                break;

            case marketStateReply:
                this.marketState = MarketState.fromJSON(appMessage.getContent());
                synchronized (marketStateLock) {
                    marketStateLock.notifyAll();
                }
                break;

            case orderProcessed:
                Order order = Order.fromJSON(appMessage.getContent());

                if (order.getOrderType().equals(OrderType.purchase)) {
                    processPurchaseOrder(order);
                } else {
                    processSellOrder(order);
                }
                break;
        }
    }

    /**
     * Registers the client to the broker
     */
    private void registerToBroker() {
        // Loop until the client is registered
        while (!isRegistered) {

            // Sends registration message if possible
            producer.produce(
                    new AppMessage(NAME, ActorType.client, "", ActorType.broker,
                            AppMessageContentType.registrationRequest, NAME)
            );
            System.out.println("Client " + NAME + " is trying to register");

            // Sleeps
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a request of the market state to the broker
     */
    public void requestMarketState() {

        if (isRegistered) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.marketStateRequest, "")
            );
        } else {
            System.out.println("The client is not registered");
        }

    }

    /**
     * Places a sell order.
     * @param company The company concerned by the sell order
     * @param nbOfStocks The number of stock of the company you want to sell
     * @param minSellingPrice The minimum price PER stock you are willing to obtain
     * @throws RegistrationException thrown when the client is not registered
     * @throws IllegalOrderException thrown when the client do not have enough stocks available to perform the operation
     */
    public void placeSellOrder(String company, int nbOfStocks, double minSellingPrice)
            throws RegistrationException, IllegalOrderException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (!sellOrderIsLegal(company, nbOfStocks)) { throw new IllegalOrderException("Illegal sell order"); }

        Order sellOrder =
                new Order(++sellOrderCounter, OrderType.sell, company, NAME, broker, minSellingPrice, nbOfStocks);

            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.order, sellOrder.toJSON())
            );


        pendingSellOrders.add(sellOrder);
    }

    /**
     * Places a purchase order.
     * @param company The company concerned by the purchase order
     * @param nbOfStocks The number of stock of the company you want to purchase
     * @param maxPurchasingPrice The maximum price PER stock you are willing to pay
     * @throws RegistrationException thrown when the client is not registered
     * @throws IllegalOrderException thrown when the client do not have enough cash available to perform the operation
     */
    public void placePurchaseOrder(String company, int nbOfStocks, double maxPurchasingPrice)
            throws RegistrationException, IllegalOrderException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (!(purchaseOrderIsLegal(nbOfStocks, maxPurchasingPrice))) { throw new IllegalOrderException("Illegal pruchase order"); }

        Order purchaseOrder =
                new Order(++purchaseOrderCounter, OrderType.purchase, company, NAME, broker, maxPurchasingPrice, nbOfStocks);

            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.order, purchaseOrder.toJSON())
            );


        pendingPurchaseOrders.add(purchaseOrder);
    }

    /**
     * Checks if a sell order would be legal given the client situation
     * @param company The company of the sell order
     * @param nbOfStocks The number of stocks in the sell order
     * @return A boolean indicating whether or not the sell order would be legal
     */
    private boolean sellOrderIsLegal(String company, int nbOfStocks) {

        // Is the number of stocks in the order inferior or equal to the number of stocks available for sale ?
        return nbOfStocks <= portfolio.getNbOfStocks(company) - stocksInSellOrders(company);
    }

    /**
     * Checks if a purchase order would be legal given the client situation
     * @param nbOfStocks The number of stocks in the purchase order
     * @param maxPurchasingPrice The maximum price the client is willing to pay per action in the purchase order
     * @return A boolean indicating whether or not the purchase order would be legal
     */
    private boolean purchaseOrderIsLegal(int nbOfStocks, double maxPurchasingPrice) {

        // Is the amount of the order (broker's commission included) inferior or equal to the amount of money
        // available ?
        return  maxPurchasingPrice * nbOfStocks * (1 + COMMISSION_RATE) <= cash - amountOfPurchaseOrders() ;
    }

    /**
     * Returns the number of stocks waiting in sell orders for a given company
     * @param company The company for which you want to know the number of stocks waiting to be sold
     * @return The number of stocks in pendingOrders for a given company
     */
    private int stocksInSellOrders(String company) {
        int stocksInSellOrders = 0;
        for (Order sellOrder : pendingSellOrders) {
            if (sellOrder.getCompany().equals(company)) {
                stocksInSellOrders += sellOrder.getDesiredNbOfStocks();
            }
        }
        return stocksInSellOrders;
    }

    /**
     * Computes the total amount of all the pending purchase orders by taking into account the desired amount of each
     * each order and the commission of the broker
     * @return The total amount of all the purchase orders
     */
    private double amountOfPurchaseOrders() {

        int amountOfPurchaseOrders = 0;

        // Computes the total amount without commission
        for (Order purchaseOrder : pendingPurchaseOrders) {
            amountOfPurchaseOrders += purchaseOrder.getDesiredAmount();
        }

        // Adds the broker's commission
        amountOfPurchaseOrders *= (1 + COMMISSION_RATE);

        return amountOfPurchaseOrders;
    }

    /**
     * Called upon receipt of a processed sell order to take into account the processed sell order.
     * @param order The sell order received
     */
    private void processSellOrder(Order order) {

        // Update cash
        cash += order.getActualAmount() * (1 - COMMISSION_RATE);

        // Update portfolio
        portfolio.removeStocks(order.getCompany(), order.getActualNbOfStocks());

        // Remove the order from the list of the pending sell orders
        pendingSellOrders.removeIf(o -> o.getId() == order.getId());
    }

    /**
     * Called upon receipt of a processed purchase order to take into account the processed purchase order.
     * @param order
     */
    private void processPurchaseOrder(Order order) {

        // Update cash
        cash -= order.getActualAmount() * (1 + COMMISSION_RATE);

        // Update portfolio
        portfolio.addStocks(order.getCompany(), order.getActualNbOfStocks());

        // Remove the order from the list of the pending purchase orders
        pendingPurchaseOrders.removeIf(o -> o.getId() == order.getId());
    }

    /**
     * Indicates that the client is done with placing order for the day.
     * @throws RegistrationException thrown when the client is not registered
     */
    public void closeTheDay() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.endOfDayNotification, ""
                    ));

    }

    public MarketState getMarketState() {
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

    public List<Order> getPendingPurchaseOrders() {
        return pendingPurchaseOrders;
    }
    public List<Order> getPendingSellOrders() {
        return pendingSellOrders;
    }

    public String getNAME() {
        return NAME;
    }
}

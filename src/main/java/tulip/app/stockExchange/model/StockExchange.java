package tulip.app.stockExchange.model;

import tulip.app.common.model.MarketState;
import tulip.app.common.model.appMessage.ActorType;
import tulip.app.common.model.appMessage.AppMessage;
import tulip.app.common.model.appMessage.AppMessageContentType;
import tulip.app.common.model.order.Order;
import tulip.app.common.model.order.OrderType;
import tulip.service.producerConsumer.Consumer;

import java.net.ServerSocket;
import java.util.*;

public class StockExchange implements Runnable {

    /** The name of the stock exchange */
    private final String NAME = "Stock Exchange";

    /**
     * A map of all the companies listed on the stock exchange.
     * The name of the company is mapped with the corresponding Company object.
     * */
    private Map<String, Company> companies = new HashMap<>();

    /** A map which links a broker (identified by his name) to the name of his clients */
    private Map<String, List<String>> brokersClients = new HashMap<>();

    /** A list which indicate wich broker have closed the day */
    private List<String> closedBrokers = new ArrayList<>();

    /** Used for the consumer role of the stockExchange */
    private Consumer consumer;

    /**
     * Constructor of the StockExchange
     * @param serverSocket The server socket use to communicate with the brokers
     */
    public StockExchange(ServerSocket serverSocket) {
        this.consumer = new Consumer(NAME, serverSocket);

        try {
            // Adds companies
            addCompany("Apple", 500, 70);
            addCompany("Alphabet", 1000, 40);
            addCompany("Tesla", 800, 48);
            addCompany("Amazon", 900, 50);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        while (true) {
            AppMessage appMessage = consumer.consume();
            if (appMessage != null) {

                switch (appMessage.getAppMessageContentType()) {

                    case registrationRequest:
                        String brokerName = appMessage.getSender();
                        registerBroker(brokerName);
                        sendRegistrationAcknowledgment(brokerName);
                        break;

                    case marketStateRequest:
                        sendMarketState(appMessage.getSender());
                        break;

                    case order:
                        Order order = Order.fromJSON(appMessage.getContent());
                        placeOrder(order);
                        break;

                    case endOfDayNotification:
                        closeBroker(appMessage.getRecipient());
                        break;

                    case registrationNotification:
                        addClientToBroker(appMessage.getSender(), appMessage.getContent());

                }

            }

            if (isClosed()) {
                updateStockPrices();
                processTransactions();
                break;
            }
        }
    }

    /**
     * Registers a broker on the stock exchange
     * @param brokerName The name of the broker being registered
     */
    private void registerBroker(String brokerName) {
        // If the client is not already registered
        if (!brokersClients.containsKey(brokerName)) {
            brokersClients.put(brokerName, new ArrayList<>());
            System.out.println(brokerName + " is now registered");
        }
    }

    /**
     * Sends a registration acknoledgement to a broker
     * @param brokerName The broker to whom the registration acknowledgement is sent
     */
    private void sendRegistrationAcknowledgment(String brokerName) {
        consumer.sendAppMessageTo(
                brokerName,
                new AppMessage(NAME, ActorType.stockExchange, brokerName, ActorType.broker,
                        AppMessageContentType.registrationAcknowledgment, "")
        );
        System.out.println("Sends registration acknowledgment to " + brokerName);
    }

    /**
     * Indicates whether a broker is registered to the stock exchange
     * @param brokerName The name of the broker you want to check
     * */
    private boolean brokerIsRegistered(String brokerName) {
        return brokersClients.containsKey(brokerName);
    }

    /**
     * For a given client, adds a client to the client list
     * @param brokerName The name of the client for which you want to increase the client counter
     */
    private void addClientToBroker(String brokerName, String clientName) {
        if (brokerIsRegistered(brokerName)) {

            // Adds the client to the list if the client is not already in the list
            List<String> clientList = brokersClients.get(brokerName);
            if (!clientList.contains(clientName)) {
                clientList.add(clientName);
                System.out.println( "The client " + clientName + " has been added to the client list of " + brokerName);
            }
        }
    }

    /**
     * Adds a company to companies
     * @param name The name of the company
     * @param nbEmittedStocks The number of emitted stocks for this company
     * @param initialStockPrice The initial stock price
     * @throws IllegalArgumentException If the company is already in the list
     */
    public void addCompany(String name, int nbEmittedStocks, double initialStockPrice) throws IllegalArgumentException {
        if (companies.containsKey(name)) { throw new IllegalArgumentException(); }
        companies.put(name, new Company(name, nbEmittedStocks, initialStockPrice));
    }

    /**
     * Sends the state of the market to a given broker
     * @param brokerName The broker to whom the MarketState must be sent
     */
    private void sendMarketState(String brokerName) {
        MarketState marketState = getMarketState();
        consumer.sendAppMessageTo(
                brokerName,
                new AppMessage(NAME, ActorType.stockExchange, brokerName, ActorType.broker,
                        AppMessageContentType.marketStateReply, marketState.toJSON())
        );
    }

    /**
     * Transmits a processed order to the broker responsible for this order
     * @param order The order being sent
     */
    private void sendsProcessedOrder(Order order) {
        consumer.sendAppMessageTo(
                order.getBroker(),
                new AppMessage(NAME, ActorType.stockExchange, order.getBroker(), ActorType.broker,
                        AppMessageContentType.orderProcessed, order.toJSON())
        );
    }

    /**
     * Upon receipt of an order, this method is called to add the order of the list of pending orders of the concerned
     * company
     * @param order The order received
     */
    private void placeOrder(Order order) {

        // Gets the company corresponding to the order
        Company company = companies.get(order.getCompany());

        if (order.getOrderType().equals(OrderType.purchase)) {
            company.addPurchaseOrder(order);
        } else {
            company.addSellOrder(order);
        }
    }

    /**
     * Updates the stock prices of all the companies
     */
    private void updateStockPrices() {
        System.out.println("Update stock prices");
        for (Map.Entry<String, Company> entry : companies.entrySet()) {
            Company company = entry.getValue();
            double deltaPrice =
                    (company.nbOfStocksForPurchase() - (company.nbOfStocksForSale() + company.getNbFloatingStocks()) )
                            / company.getNB_EMITTED_STOCKS();
            double newStockPrice = company.getStockPrice() * (1 + deltaPrice);
            company.updateStockPrice(newStockPrice);
            System.out.println(entry.getKey() + " price is now: " + newStockPrice);
        }
    }

    /**
     * Processes all the pending orders for all the companies and sends the processed orders to the brokers
     */
    private void processTransactions() {

        System.out.println("Processing transactions");

        // Iterates over each company
        for (Company c : companies.values()) {

            // Iterates over purchase orders
            while (!c.getPendingPurchaseOrders().isEmpty()) {

                // Retrieves and removes the head of the pendingPurchaseOrder queue
                Order purchaseOrder = c.getPendingPurchaseOrders().poll();

                // If the desired price is superior or equal to the market price
                if (purchaseOrder.getDesiredPrice() >= c.getStockPrice()) {

                    // Sells the floating stocks
                    if (c.getNbFloatingStocks() > 0) {
                        int floatingStocksSold = Math.min(purchaseOrder.getDesiredNbOfStocks(), c.getNbFloatingStocks());
                        c.sellFloatingStocks(floatingStocksSold);
                        purchaseOrder.setActualNbOfStocks(floatingStocksSold);
                    }

                    // While there are still stocks to be purchased, iterates over sell orders
                    while (purchaseOrder.getActualNbOfStocks() < purchaseOrder.getDesiredNbOfStocks() &&
                            !c.getPendingSellOrders().isEmpty()) {

                        // Retrieves, but does not remove, the head of the queue
                        Order sellOrder = c.getPendingSellOrders().peek();

                        // If the market price is inferior to the minimum selling price
                        if (c.getStockPrice() < sellOrder.getDesiredPrice()) {

                            // Remove the head of the queue
                            c.getPendingSellOrders().remove();

                            // Sets the sell order as processed
                            sellOrder.processOrder(new Date(), c.getStockPrice());

                            // Sends the sell order
                            sendsProcessedOrder(sellOrder);

                        } else {

                            int stocksSold = Math.min(
                                    purchaseOrder.getDesiredNbOfStocks() - purchaseOrder.getActualNbOfStocks(),
                                    sellOrder.getDesiredNbOfStocks() - sellOrder.getActualNbOfStocks()
                            );

                            // Updates the actual number of stocks
                            purchaseOrder.setActualNbOfStocks(
                                purchaseOrder.getActualNbOfStocks() + stocksSold
                            );
                            sellOrder.setActualNbOfStocks(
                                    sellOrder.getActualNbOfStocks() + stocksSold
                            );

                            // For this sell order, if all the stocks for sale have been sold
                            if (sellOrder.getDesiredNbOfStocks() == sellOrder.getActualNbOfStocks()) {
                                c.getPendingSellOrders().remove();
                                sellOrder.processOrder(new Date(), c.getStockPrice());
                                sendsProcessedOrder(sellOrder);
                            }
                        }
                    }
                }

                // Sets the purchase order as processed
                purchaseOrder.processOrder(new Date(), c.getStockPrice());

                // Sends the order to the broker
                sendsProcessedOrder(purchaseOrder);
            }

            // Processes and sends the remaining sell orders
            while (!c.getPendingSellOrders().isEmpty()) {
                Order sellOrder = c.getPendingSellOrders().poll();
                sellOrder.processOrder(new Date(), c.getStockPrice());
                sendsProcessedOrder(sellOrder);
            }
        }
    }

    /**
     * Gives a snapshot of the market state at a given moment.
     * @return A MarketState object which is an object that inherit from HashMap<String, Double>.
     *         It associates the name of a company with a stock price.
     */
    public MarketState getMarketState() {
        MarketState marketState = new MarketState();
        for (Map.Entry<String, Company> entry : companies.entrySet()) {
            marketState.put(entry.getKey(), entry.getValue().getStockPrice());
        }
        return marketState;
    }

    /**
     * Adds the broker to the closedBrokers list
     * @param brokerName The name of the broker
     */
    private void closeBroker(String brokerName) {
        closedBrokers.add(brokerName);
    }

    /**
     * Indicates whether all the brokers have closed the day
     */
    private boolean isClosed() {
        if (closedBrokers.size() == 0) { return false; }

        return closedBrokers.size() == brokersClients.size();
    }

    public List<Order> getCurrentDemand() {
        List<Order> purchaseOrders = new ArrayList<>();
        for (Company company : companies.values()) {
            purchaseOrders.addAll(company.getPendingPurchaseOrders());
        }
        return purchaseOrders;
    }

    public List<Order> getCurrentSupply() {
        List<Order> sellOrders = new ArrayList<>();
        for (Company company : companies.values()) {
            sellOrders.addAll(company.getPendingSellOrders());
        }
        return sellOrders;
    }

    public List<String> getBrokers() {
        List<String> brokers = new ArrayList<>();
        for (Map.Entry<String, List<String> > broker : brokersClients.entrySet()) {
            brokers.add(broker.getKey());
        }
        return brokers;
    }

}

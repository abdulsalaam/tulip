package tulip.app.client.model;

import tulip.app.MarketState;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.exceptions.IllegalOrderException;
import tulip.app.exceptions.RegistrationException;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements ProducerMessenger {

    private final String NAME;

    private Portfolio portfolio = new Portfolio();

    private String broker;

    private boolean isRegistered = false;

    private double cash;

    private List<Order> pendingSellOrders = new ArrayList<>();

    private List<Order> pendingPurchaseOrders = new ArrayList<>();

    private List<Order> archivedOrders = new ArrayList<>();

    private final double COMMISSION_RATE = 0.1;

    private int sellOrderCounter = 0;

    private int purchaseOrderCounter = 0;

    /** Current market state (list of companies and associated prices */
    private MarketState marketState = new MarketState();

    private Producer producer;

    public Client(String name, double cash, Socket socket) {
        this.cash = cash;
        this.NAME = name;
        this.producer = new Producer(name, socket, this);
    }

    @Override
    public void uponReceiptOfAppMessage(AppMessage appMessage) {

        switch (appMessage.getAppMessageContentType()) {

            case registrationAcknowledgment:
                this.isRegistered = true;
                this.broker = appMessage.getContent();
                break;

            case marketStateReply:
                this.marketState = MarketState.fromJSON(appMessage.getContent());
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

    private void registerToBroker() {

        if (producer.canProduce()) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, "", ActorType.broker,
                            AppMessageContentType.registrationRequest, NAME)
            );
        }
    }

    public void requestMarketState() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (producer.canProduce()) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.marketStateRequest, "")
            );
        }
    }

    public void placeSellOrder(String company, int nbOfStocks, double minSellingPrice)
            throws RegistrationException, IllegalOrderException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (!sellOrderIsLegal(company, nbOfStocks)) { throw new IllegalOrderException("Illegal sell order"); }

        Order sellOrder =
                new Order(++sellOrderCounter, OrderType.sell, company, NAME, broker, minSellingPrice, nbOfStocks);

        if (producer.canProduce()) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.order, sellOrder.toJSON())
            );
        }

        pendingSellOrders.add(sellOrder);

    }

    public void placePurchaseOrder(String company, int nbOfStocks, double maxPurchasingPrice)
            throws RegistrationException, IllegalOrderException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (purchaseOrderIsLegal(nbOfStocks, maxPurchasingPrice)) { throw new IllegalOrderException("Illegal pruchase order"); }

        Order purchaseOrder =
                new Order(++purchaseOrderCounter, OrderType.purchase, company, NAME, broker, maxPurchasingPrice, nbOfStocks);

        if (producer.canProduce()) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.order, purchaseOrder.toJSON())
            );
        }

        pendingSellOrders.add(purchaseOrder);
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

    private void processSellOrder(Order order) {

        // Update cash
        cash += order.getActualAmount() * (1 - COMMISSION_RATE);

        // Update portfolio
        portfolio.removeStocks(order.getCompany(), order.getActualNbOfStocks());

        // Remove the order from the list of the pending sell orders
        pendingSellOrders.removeIf(o -> o.getId() == order.getId());

        archivedOrders.add(order);

    }

    private void processPurchaseOrder(Order order) {

        // Update cash
        cash -= order.getActualAmount() * (1 + COMMISSION_RATE);

        // Update portfolio
        portfolio.addStocks(order.getCompany(), order.getActualNbOfStocks());

        // Remove the order from the list of the pending purchase orders
        pendingPurchaseOrders.removeIf(o -> o.getId() == order.getId());

        archivedOrders.add(order);

    }

    public void closeTheDay() throws RegistrationException {

        if (!isRegistered) { throw new RegistrationException("The client is not registered"); }

        if (producer.canProduce()) {
            producer.produce(
                    new AppMessage(NAME, ActorType.client, broker, ActorType.broker,
                            AppMessageContentType.endOfDayNotification, ""
                    ));
        }
    }

    public MarketState getMarketState() {
        marketState.put("Basecamp", 250.0);
        marketState.put("Tesla", 596.70);
        marketState.put("Facebook", 450.0);
        marketState.put("Alphabet", 270.0);
        marketState.put("Apple", 430.0);
        marketState.put("Spotify", 220.0);
        marketState.put("LVMH", 550.0);
        return marketState;
    }
}

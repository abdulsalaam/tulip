package tulip.manageClient;

import tulip.manageOrder.PurchaseOrder;
import tulip.manageOrder.SellOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Client {

    private String name;

    private Portfolio portfolio;

    private String broker;

    private boolean isRegistered;

    private double cash;

    private List<SellOrder> pendingSellOrders;

    private List<PurchaseOrder> pendingPurchaseOrders;

    private final double COMMISSION_RATE = 0.1;

    private int sellOrderCounter;

    private int purchaseOrderCounter;

    public Client(String name) {
        this.name = name;
        this.portfolio = new Portfolio();
        this.isRegistered = false;
        this.cash = 0;
        this.pendingSellOrders = new ArrayList<>();
        this.pendingPurchaseOrders = new ArrayList<>();
    }

    private void registerToBroker() {

        // todo: Send message to broker

        this.isRegistered = true;
        // this.broker =

    }

    private void requestMarketState() {}

    private void placeSellOrder(String company, int nbOfStocks, double minSellingPrice) {
        if (sellOrderIsLegal(company, nbOfStocks)) {
            pendingSellOrders.add(
                    new SellOrder(++sellOrderCounter, company, name, broker, new Date(), nbOfStocks, minSellingPrice)
            );

            // todo: transmit order to broker
        } else {
            System.out.println("Illegal sell order");
        }
    }

    private void placePurchaseOrder(String company, int nbOfStocks, double maxPurchasingPrice) {
        if (purchaseOrderIsLegal(nbOfStocks, maxPurchasingPrice)) {
            pendingPurchaseOrders.add(
                new PurchaseOrder(++purchaseOrderCounter, company, name, broker, new Date(), nbOfStocks, maxPurchasingPrice)
            );

            // todo: transmit order to broker
        } else {
            System.out.println("Illegal purchase order");
        }

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
        for (SellOrder sellOrder : pendingSellOrders) {
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
        for (PurchaseOrder purchaseOrder : pendingPurchaseOrders) {
            amountOfPurchaseOrders += purchaseOrder.getDesiredAmount();
        }

        // Adds the broker's commission
        amountOfPurchaseOrders *= (1 + COMMISSION_RATE);

        return amountOfPurchaseOrders;
    }

    private void processSellOrder(int id, int actualNbOfStock, Date processingDate, double actualSellingPrice, double commission) {

        // todo: to improve
        Iterator<SellOrder> iterator = pendingSellOrders.iterator();
        while (iterator.hasNext()) {
            SellOrder sellOrder = iterator.next();
            if (sellOrder.getId() == id) {
                sellOrder.processOrder(actualNbOfStock, processingDate, actualSellingPrice);
                iterator.remove();

                // Update cash
                cash += sellOrder.getActualAmount() * (1 - commission);
            }
        }

    }

    private void processPurchaseOrder(int id, int actualNbOfStock, Date processingDate, double actualPurchasingPrice, double commission) {

        // todo: to improve
        Iterator<PurchaseOrder> iterator = pendingPurchaseOrders.iterator();
        while (iterator.hasNext()) {
            PurchaseOrder purchaseOrder = iterator.next();
            if (purchaseOrder.getId() == id) {
                purchaseOrder.processOrder(actualNbOfStock, processingDate, actualPurchasingPrice);
                iterator.remove();

                // Update cash
                cash -= purchaseOrder.getActualAmount() * (1 + commission);
            }
        }

    }

    private void closeTheDay() {}
}

package tulip.app.stockExchange;

import tulip.app.order.Order;

import java.util.LinkedList;
import java.util.Queue;

public class Company {

    /** The name of the company */
    private final String NAME;

    /** The number of emitted stocks */
    private final int NB_EMITTED_STOCKS;

    /** The number of floating stocks (initially the number of emitted stocks) */
    private int nbFloatingStocks ;

    /** The current stock price for this company */
    private double stockPrice ;

    /** Queue of pending purchase orders */
    private Queue<Order> pendingPurchaseOrders = new LinkedList<>();

    /** Queue of pending sell orders */
    private Queue<Order> pendingSellOrders = new LinkedList<>();

    /**
     * Constructor
     * @param name The name of the company
     * @param nbEmittedStocks The number of emitted stocks for this company
     * @param initialStockPrice The initial stock price
     */
    Company(String name, int nbEmittedStocks, double initialStockPrice) {
        this.NAME = name;
        this.NB_EMITTED_STOCKS = this.nbFloatingStocks = nbEmittedStocks;
        this.stockPrice = initialStockPrice;
    }

    /**
     * Returns the amount of the market capitalisation of the company
     * @return The amount of the market capitalisation
     * */
    double getMarketCap() {
        return NB_EMITTED_STOCKS * stockPrice;
    }

    /**
     * Computes the total number of stocks available for purchase for this company
     * @return The number of stocks available for purchase
     */
    int nbOfStocksForPurchase() {
        int nbOfStocksForPurchase = 0;
        for (Order purchaseOrder: pendingPurchaseOrders) {
            nbOfStocksForPurchase += purchaseOrder.getDesiredNbOfStocks();
        }
        return nbOfStocksForPurchase;
    }

    /**
     * Computes the total number of stocks for sale for this company
     * @return The number of stocks for sale
     */
    int nbOfStocksForSale() {
        int nbOfStocksForSale = 0;
        for (Order sellOrder: pendingSellOrders) {
            nbOfStocksForSale += sellOrder.getDesiredNbOfStocks();
        }
        return nbOfStocksForSale;
    }

    /**
     * Used to update the stock price of a company
     * @param stockPrice The new stock price
     */
    void updateStockPrice(double stockPrice) {
        this.stockPrice = stockPrice;
    }


    /**
     * Decreases the number of floating stocks when floating stocks are sold
     * @param nbOfStocksToSell The number of floating stocks being sold
     */
    void sellFloatingStocks(int nbOfStocksToSell) {
        nbFloatingStocks -= nbOfStocksToSell;
    }

    void addPurchaseOrder(Order purchaseOrder) {
        pendingPurchaseOrders.add(purchaseOrder);
    }

    void addSellOrder(Order sellOrder) {
        pendingSellOrders.add(sellOrder);
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public int getNB_EMITTED_STOCKS() {
        return NB_EMITTED_STOCKS;
    }

    public Queue<Order> getPendingPurchaseOrders() {
        return pendingPurchaseOrders;
    }

    public Queue<Order> getPendingSellOrders() {
        return pendingSellOrders;
    }

    public int getNbFloatingStocks() {
        return nbFloatingStocks;
    }
}

package tulip.app.stockExchange;

public class Company {

    /** The name of the company */
    private final String NAME;

    /** The number of emitted stocks */
    private final int NB_EMITTED_STOCKS;

    /** The number of floating stocks (initially the number of emitted stocks) */
    private int nbFloatingStocks ;

    /** The current stock price for this company */
    private double stockPrice ;

    /**
     * Constructor
     * @param name The name of the company
     * @param nbEmittedStocks The number of emitted stock for this company
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
     * Used to update the stock price of a company
     * @param stockPrice The new stock price
     */
    void updateStockPrice(double stockPrice) {
        this.stockPrice = stockPrice;
    }
}

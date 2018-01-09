package tulip.app.client.model;

import java.util.HashMap;

/**
 * Represents a portfolio. Map the name of a company with the number of actions of this company owned by the client.
 */
public class Portfolio extends HashMap<String, Integer> {

    /**
     * Increases the count of stocks for a given company
     * @param company The company concerned
     * @param nbOfStocksToAdd The number of stock to add
     */
    void addStocks(String company, int nbOfStocksToAdd) {
        Integer value = get(company);
        int currentNumberOfStocks = (value == null) ? 0 : value;
        put(company, currentNumberOfStocks + nbOfStocksToAdd);
    }

    /**
     * Decreases the count of stocks for a given company
     * @param company The company concerned
     * @param nbOfStocksToRemove The number of stocks to remove
     */
    void removeStocks(String company, int nbOfStocksToRemove) {
        Integer value = get(company);
        int currentNumberOfStocks = (value == null) ? 0 : value;
        put(company, currentNumberOfStocks - nbOfStocksToRemove);
    }

    /**
     * Returns the number of stocks owned for a given company
     * @param company The company for which you want to get the number of stocks owned
     * @return The number of stocks owned
     */
    int getNbOfStocks(String company) {
        Integer value = get(company);
        return (value == null) ? 0 : value;
    }

}


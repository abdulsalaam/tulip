package tulip.app.client.model;

import java.util.HashMap;

public class Portfolio extends HashMap<String, Integer> {

    void addStocks(String company, int nbOfStocksToAdd) {
        put(company, get(company) + nbOfStocksToAdd);
    }

    void removeStocks(String company, int nbOfStocksToRemove) {
        put(company, get(company) - nbOfStocksToRemove);
    }

    /**
     * Returns the number of stocks owned for a given company
     * @param company The company for which you want to get the number of stocks owned
     * @return The number of stocks owned
     */
    int getNbOfStocks(String company) { return get(company); }

}


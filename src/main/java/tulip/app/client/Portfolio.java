package tulip.app.client;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {

        private Map<String, Integer> stocksForEachCompany = new HashMap<>();

        void addStock(String company) {}

        void removeStock(String company) {}

        /**
         * Returns the number of stocks owned for a given company
         * @param company The company for which you want to get the number of stocks owned
         * @return The number of stocks owned
         */
        int getNbOfStocks(String company) {
            return stocksForEachCompany.get(company);
        }

}


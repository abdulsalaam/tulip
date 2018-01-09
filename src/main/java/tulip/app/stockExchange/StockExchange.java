package tulip.app.stockExchange;

import tulip.app.MarketState;
import tulip.app.order.Order;
import tulip.service.producerConsumer.Consumer;

import java.util.HashMap;
import java.util.Map;

public class StockExchange {

    /**
     * A map of all the companies listed on the stock exchange.
     * The name of the company is mapped with the corresponding Company object.
     * */
    private Map<String, Company> companies = new HashMap<>();

    /** A map which links a broker (identified by his name) to his number of client */
    Map<String, Integer> brokersAndNbOfClients = new HashMap<>();

    /** Used for the consumer role of the stockExchange */
    private Consumer consumer;

    void openStockExchange() {}

    void closeStockEchange() {}

    /**
     * Registers a broker on the stock exchange
     * @param brokerName The name of the broker being registered
     */
    void registerBroker(String brokerName) {
        brokersAndNbOfClients.put(brokerName, 0);
    }

    /**
     * Increases the counter of clients by one on the brokersAndNbOfClients map, for a given client
     * @param brokerName The name of the client for which you want to increase the client counter
     */
    void addClientToBroker(String brokerName) {
        brokersAndNbOfClients.put(brokerName, brokersAndNbOfClients.get(brokerName) + 1);
    }

    /** Indicates whether a broker is registered to the stock exchange */
    boolean brokerIsRegistered() {

    }

    /**
     * Updates the stock price of all the companies
     */
    void updateStockPrices() {}

    void placeSellOrder(Order sellOrder) {}

    void placePurchaseOrder(Order purchaseOrder) {}

    void processTransactions() {}

    /**
     * Gives a snapshot of the market state at a given moment.
     * @return A MarketState object which is an object that inherit from HashMap<String, Double>.
     *         It associates the name of a company with a stock price.
     */
    MarketState getMarketState() {
        MarketState marketState = new MarketState();
        for (Map.Entry<String, Company> entry : companies.entrySet()) {
            marketState.put(entry.getKey(), entry.getValue().getStockPrice());
        }
        return marketState;
    }
}

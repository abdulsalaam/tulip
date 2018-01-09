package tulip.app.stockExchange;

import tulip.app.MarketState;
import tulip.app.appMessage.ActorType;
import tulip.app.appMessage.AppMessage;
import tulip.app.appMessage.AppMessageContentType;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.service.producerConsumer.Consumer;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class StockExchange extends Thread {

    private final String NAME = "Stock Exchange";

    /**
     * A map of all the companies listed on the stock exchange.
     * The name of the company is mapped with the corresponding Company object.
     * */
    private Map<String, Company> companies = new HashMap<>();

    /** A map which links a broker (identified by his name) to his number of client */
    private Map<String, Integer> brokersAndNbOfClients = new HashMap<>();

    /** Used for the consumer role of the stockExchange */
    private Consumer consumer;

    /**
     * Constructor of the StockExchange
     * @param serverSocket
     */
    public StockExchange(ServerSocket serverSocket) {
        this.consumer = new Consumer(NAME, serverSocket);
    }

    @Override
    public void run() {

        while (true) {
            if (consumer.canConsume()) {
                AppMessage appMessage = consumer.consume();

                switch (appMessage.getAppMessageContentType()) {

                    case registrationRequest:
                        String brokerName = appMessage.getContent();
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


                }
            }
        }
    }

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
     * Sends a registration acknoledgement to a broker
     * @param brokerName The broker to whom the registration acknowledgement is sent
     */
    void sendRegistrationAcknowledgment(String brokerName) {
        consumer.sendAppMessageTo(
                brokerName,
                new AppMessage(NAME, ActorType.stockExchange, brokerName, ActorType.broker,
                        AppMessageContentType.registrationAcknowledgment, "")
        );
    }

    /**
     * Sends the state of the market to a given broker
     * @param brokerName The broker to whom the MarketState must be sent
     */
    void sendMarketState(String brokerName) {
        MarketState marketState = getMarketState();
        consumer.sendAppMessageTo(
                brokerName,
                new AppMessage(NAME, ActorType.stockExchange, brokerName, ActorType.broker,
                        AppMessageContentType.marketStateReply, marketState.toJSON())
        );
    }

    /**
     * Increases the counter of clients by one on the brokersAndNbOfClients map, for a given client
     * @param brokerName The name of the client for which you want to increase the client counter
     */
    void addClientToBroker(String brokerName) {
        brokersAndNbOfClients.put(brokerName, brokersAndNbOfClients.get(brokerName) + 1);
    }

    /** Indicates whether a broker is registered to the stock exchange */
    boolean brokerIsRegistered(String brokerName) {
        return brokersAndNbOfClients.containsKey(brokerName);
    }

    void placeOrder(Order order) {

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
    void updateStockPrices() {
        for (Company company: companies.values()) {
            double deltaPrice =
                    (company.nbOfStocksForPurchase() - company.nbOfStocksForSale()) / company.getNB_EMITTED_STOCKS();
            double newStockPrice = company.getStockPrice() * (1 + deltaPrice);
            company.updateStockPrice(newStockPrice);
        }
    }

    /**
     * Processes all the pending orders for all the companies
     */
    void processTransactions() {

    }

    void notifyBrokerOfTransaction() {}

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

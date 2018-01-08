package tulip.app.broker.model;

import tulip.app.client.model.Client;
import tulip.app.order.Order;
import tulip.app.order.PurchaseOrder;
import tulip.app.order.SellOrder;
import tulip.service.producerConsumer.Consumer;
import tulip.service.producerConsumer.Producer;
import tulip.service.producerConsumer.ProducerMessenger;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;
import tulip.service.producerConsumer.messages.Target;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Broker implements ProducerMessenger {

    /** Name of the broker */
    private String name;
    /** Tells whether the broker is registered to the stock exchange or not */
    private boolean isRegistered;
    /** Amount of cash available to the broker */
    private double cash;
    /** Commission rate applied by the broker on each transaction */
    private double commissionRate;
    /** Clients list of the broker */
    private List<String> clients = new ArrayList<>();
    /** Orders (which can be both purchases and sellings) not yet proceeded */
    private List<Order> pendingOrders = new ArrayList<>();
    /** Counts the selling orders proceeded by the broker */
    private int sellOrderCounter;
    /** Counts the purchase orders proceeded by the broker */
    private int purchaseOrderCounter;

    private List<String> clientsRequestingRegistration = new ArrayList<>();

    private List<String> clientsRegistered = new ArrayList<>();

    ServerSocket serverSocket;
    Consumer brokerConsumer = new Consumer("broker", serverSocket);
    Socket socket;
    Producer brokerProducer = new Producer("broker", socket, this);


    /**
     * Constructor
     * @param name is the name of the broker
     */
    public Broker(String name) {
        this.name = name;
        this.isRegistered = false;
        this.cash = 0;
        this.commissionRate = 0.1;
        this.clients = new ArrayList<>();
        this.pendingOrders = new ArrayList<>();
    }

    /**
     * Sends registering request to stock exchange
     */
    public void registerToStockExchange() {
        while(!isRegistered) {
            if (brokerProducer.canProduce()) {
                brokerProducer.produce("Registration request");
            }
        }
    }

    /**
     * Registers a client
     */
    public void registerClient(){
        clientsRegistered.add(clientsRequestingRegistration.get(0));
        if(brokerProducer.canProduce()) {
            brokerProducer.produce("Registration acquittance");
        }
    }

    /**
     * Sends request to stock exchange in order to retrieve
     * market state information
     */
    public Map<String, Double> requestMarketState(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce("Registration request");
        }
        return new HashMap<String, Double>();
    }

    /**
     * Checks whether a client is registered or notf
     * @return true if the client is registered, false otherwise
     */
    public boolean checkClientRegistered(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce("Registration request");
        }
        return false;
    }

    /**
     * Proceeds a sell order
     * @param client is the name of the ordering client
     * @param nbOfStocks is the number of stocks the client wants to sell
     * @param minSellingPrice is the minimum price to which the client is willing to sell
     */
    public void placeSellOrder(String company, String client, int nbOfStocks, double minSellingPrice){
        pendingOrders.add(
                new SellOrder(++sellOrderCounter,company, client, name, new Date(), nbOfStocks, minSellingPrice)
        );
    }

    /**
     * Proceeds a purchase order
     * @param client is the name of the ordering client
     * @param nbOfStocks is the number of stocks requested by the client
     * @param maxPurchasingPrice is the maximum price to which the client is willing to buy
     */
    public void placePurchaseOrder(String company, String client, int nbOfStocks, double maxPurchasingPrice) {
        pendingOrders.add(
                new PurchaseOrder(++purchaseOrderCounter, company, client, name, new Date(), nbOfStocks, maxPurchasingPrice)
        );
    }

    /**
     * To delete ?
     */
    public void notifyOfTransaction(){}

    /**
     * After an agreement has been made, notifies the client that the order
     * has been proceeded.
     */
    public void notifyClientOfTransaction(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce("Registration request");
        }
    }

    // Improvement : put getActualAmount in Order class
    /**
     * After proceeding a Purchase order, the broker calculates its commission and updates
     * his cach accordingly
     * @param order the order from which the commission is calculated
     */
    public void calculateCommission(PurchaseOrder order){
        double commission = order.getActualAmount() * commissionRate;
        this.cash += commission;
    }
    /**
     * After proceeding a Sell order, the broker calculates its commission and updates
     * his cach accordingly
     * @param order the order from which the commission is calculated
     */
    public void calculateCommission(SellOrder order){
        double commission = order.getActualAmount() * commissionRate;
        this.cash += commission;
    }

    /**
     * Sends to the client the updated stock values
     */
    public void notifyClientsOfPriceChanges(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce("Registration request");
        }
    }

    /**
     * When all his clients close the day, the closer closes it as well
     * and informs the stock exchange
     */
    public void closeTheDay(){
        if(brokerProducer.canProduce()){
            brokerProducer.produce("Registration request");
        }
    }

    @Override
    public void uponReceiptOfAppMessage(Message message) {
        message.getContent();
    }
}

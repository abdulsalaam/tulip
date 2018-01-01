package tulip.manageBroker;

import tulip.manageOrder.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Broker {

    private String name;
    private boolean isRegistered;
    private double cash;
    private double commissionRate;
    private List<String> clients;
    private Map<String, Order> pendingOrders;

    public Broker(String name) {
        this.name = name;
        this.isRegistered = false;
        this.cash = 0;
        this.commissionRate = 0.1;
        this.clients = new ArrayList<>();
        this.pendingOrders = new HashMap<>();
    }

    public void registerToStockExchange() {}

    public void registerClient(){}

    public void requestMarketState(){}

    public void checkClientRegistered(){}

    public void placeSellOrder(String client, String company, int nbStock, double minPrice){}

    public void placePurchaseOrder(String client, String company, int nbStock, double maxPrice){}

    public void notifyOfTransaction(){}

    public void notifyClientOfTransaction(){}

    public void calculateCommission(Order order){}

    public void notifyClientsOfPriceChanges(){}

    public void closeTheDay(){}
    
}

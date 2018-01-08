package tulip.app.client.model;



import tulip.app.order.Order;

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

    private List<Order> pendingSellOrders;

    private List<Order> pendingPurchaseOrders;

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


}

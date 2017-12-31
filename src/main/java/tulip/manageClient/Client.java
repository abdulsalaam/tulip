package tulip.manageClient;

import tulip.manageOrder.PurchaseOrder;
import tulip.manageOrder.SellOrder;

import java.util.ArrayList;
import java.util.List;

public class Client {

    private String name;

    private Portfolio portfolio;

    private String broker;

    private boolean isRegistered;

    private double cash;

    private List<SellOrder> pendantSellOrders;

    private List<PurchaseOrder> pendantPurchaseOrders;


    public Client(String name) {
        this.name = name;
        this.portfolio = new Portfolio();
        this.isRegistered = false;
        this.cash = 0;
        this.pendantSellOrders = new ArrayList<>();
        this.pendantPurchaseOrders = new ArrayList<>();
    }

    void registerToBroker(String brokerName) {}

    void requestMarketState() {}

    void placeSellOrder(String company, double minSellingPrice, int nbOfStocks) {}

    void placePurchaseOrder(String company, double maxPurchasingPrice, int nbOfStocks) {}

    void SellOrderIsLegal() {}

    void PurchaseOrderIsLegal() {}

    void updateCash() {}

    void archiveOrder() {}

    void closeTheDay() {}
}

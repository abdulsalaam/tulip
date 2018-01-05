package tulip.app.order;

import java.util.Date;

/**
 * Represents a purchase order
 * @author Thibaud Martinez
 */
public class PurchaseOrder extends Order {

    /** The maximum price the client is willing to pay for each stock */
    private double maxPurchasingPrice;

    /** The purchase price per stock actually paid to acquire the stocks */
    private double actualPurchasingPrice;

    /**
     * Constructs a PurchaseOrder
     * @param id
     * @param client
     * @param broker
     * @param emissionDate
     * @param desiredNbOfStock
     * @param maxPurchasingPrice
     */
    public PurchaseOrder(int id, String client, String broker, Date emissionDate, int desiredNbOfStock, double maxPurchasingPrice) {
        super(id, client, broker, emissionDate, desiredNbOfStock);
        this.maxPurchasingPrice = maxPurchasingPrice;
    }

    /**
     * Processes the purchase order
     * @param actualNbOfStocks
     * @param processingDate
     * @param actualPurchasingPrice
     */
    public void processOrder(int actualNbOfStocks, Date processingDate, double actualPurchasingPrice) {
        super.processOrder(actualNbOfStocks, processingDate);
        this.actualPurchasingPrice = actualPurchasingPrice;
    }

    @Override
    public String toString() {
        return "{ " + super.toString() + ", maxPurchasingPrice: " + maxPurchasingPrice + ", actualPurchasingPrice: "
                + actualPurchasingPrice + " }";
    }
}

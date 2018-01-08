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
     */
    public PurchaseOrder(int id, String company, String client, String broker, Date emissionDate, int desiredNbOfStock,
                         double maxPurchasingPrice) {
        super(id, company, client, broker, emissionDate, desiredNbOfStock);
        this.maxPurchasingPrice = maxPurchasingPrice;
    }

    /**
     * Processes the purchase order
     */
    public void processOrder(int actualNbOfStocks, Date processingDate, double actualPurchasingPrice) {
        super.processOrder(actualNbOfStocks, processingDate);
        this.actualPurchasingPrice = actualPurchasingPrice;
    }

    /**
     * Computes the amount of the order by taking into account the maximum purchasing price and the desired number of
     * stocks
     * @return The desired amount of the order
     */
    public double getDesiredAmount() {
        return maxPurchasingPrice * getDesiredNbOfStocks();
    }

    /**
     * Computes the amount of the order by taking into account the actual purchasing price and the actual number of
     * stocks
     * @return The actual amount of the order
     */
    public double getActualAmount() {
        return actualPurchasingPrice * getActualNbOfStocks();
    }

    @Override
    public String toString() {
        return "{ " + super.toString() + ", maxPurchasingPrice: " + maxPurchasingPrice + ", actualPurchasingPrice: "
                + actualPurchasingPrice + " }";
    }
}
package tulip.app.order;

import java.util.Date;

/**
 * Represents a sell order
 * @author Thibaud Martinez
 */
public class SellOrder extends Order {

    /** The minimum price the client is willing to obtain for each stock */
    private double minSellingPrice;

    /** The price per stock actually paid when selling the stocks */
    private double actualSellingPrice;

    /**
     * Constructs a PurchaseOrder
     * @param id
     * @param client
     * @param broker
     * @param emissionDate
     * @param desiredNbOfStock
     * @param minSellingPrice
     */
    public SellOrder(int id, String client, String broker, Date emissionDate, int desiredNbOfStock, double minSellingPrice) {
        super(id, client, broker, emissionDate, desiredNbOfStock);
        this.minSellingPrice = minSellingPrice;
    }

    /**
     * Processes the sell order
     * @param actualNbOfStocks
     * @param processingDate
     * @param actualSellingPrice
     */
    public void processOrder(int actualNbOfStocks, Date processingDate, double actualSellingPrice) {
        super.processOrder(actualNbOfStocks, processingDate);
        this.actualSellingPrice = actualSellingPrice;
    }

    @Override
    public String toString() {
        return "{ " + super.toString() + ", minSellingPrice: " + minSellingPrice + ", actualSellingPrice: "
                + actualSellingPrice + " }";
    }

}

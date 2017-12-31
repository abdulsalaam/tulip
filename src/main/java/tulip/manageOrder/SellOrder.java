package tulip.manageOrder;

import java.util.Date;

/**
 * Represent a sell order
 * @author Thibaud Martinez
 */
public class SellOrder extends Order {

    /** The minimum price the client is willing to obtain for each stock */
    private double minSellingPrice;

    /** The price per stock actually paid when selling the stocks */
    private double actualSellingPrice;

    /**
     * Constructs a PurchaseOrder
     */
    public SellOrder(int id, String client, String broker, Date emissionDate, int desiredNbOfStock, double minSellingPrice) {
        super(id, client, broker, emissionDate, desiredNbOfStock);
        this.minSellingPrice = minSellingPrice;
    }

    /**
     * Processes the sell order
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

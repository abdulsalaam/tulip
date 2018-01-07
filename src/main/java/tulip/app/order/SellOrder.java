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
     * Constructs a sellOrder
     */
    public SellOrder(int id, String company, String client, String broker, Date emissionDate, int desiredNbOfStock,
                     double minSellingPrice) {
        super(id, company, client, broker, emissionDate, desiredNbOfStock);
        this.minSellingPrice = minSellingPrice;
    }

    /**
     * Processes the sell order
     */
    public void processOrder(int actualNbOfStocks, Date processingDate, double actualSellingPrice) {
        super.processOrder(actualNbOfStocks, processingDate);
        this.actualSellingPrice = actualSellingPrice;
    }

    public double getDesiredAmount() {
        return minSellingPrice * getDesiredNbOfStocks();
    }

    public double getActualAmount() {
        return actualSellingPrice * getActualNbOfStocks();
    }

    @Override
    public String toString() {
        return "{ " + super.toString() + ", minSellingPrice: " + minSellingPrice + ", actualSellingPrice: "
                + actualSellingPrice + " }";
    }

}
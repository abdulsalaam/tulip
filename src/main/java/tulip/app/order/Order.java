package tulip.app.order;

import java.util.Date;

/**
 * Represent an order.
 * The client places an order, the order is sent by the broker of the client to the stock exchange, then, the
 * stock exchange processes the order.
 * @author Thibaud Martinez
 */
public abstract class Order {

    /** A unique identifier of the order */
    private int id;

    /** The name of the client who placed the order */
    private String client;

    /** The name of the broker responsible for sending the order to the stock exchange */
    private String broker;

    /** The date on which the order was placed by the client */
    private Date emissionDate;

    /** The state of the order */
    private OrderState state;

    /** The date on which the order was processed by the stock exchange */
    private Date processingDate;

    /** The number of stocks concerned by the order ie. the number of stocks that the client wants to purchase or sell */
    private int desiredNbOfStock;

    /** The number of stocks actually purchased or sold once the order has been processed */
    private int actualNbOfStocks;

    /**
     * Constructor
     */
    Order(int id, String client, String broker, Date emissionDate, int desiredNbOfStocks) {
        this.id = id;
        this.client = client;
        this.state = OrderState.pending;
        this.broker = broker;
        this.emissionDate = emissionDate;
        this.desiredNbOfStock = desiredNbOfStocks;
    }

    /**
     * Processes the order ie. fills the processing date, changes the order state to processed and indicates the number
     * of stocks purchased or sold.
     * @param actualNbOfStocks
     * @param processingDate
     */
    void processOrder(int actualNbOfStocks, Date processingDate) {
        this.processingDate = processingDate;
        this.state = OrderState.processed;
        this.actualNbOfStocks = actualNbOfStocks;
    }

    @Override
    public String toString() {
        return "id: " + id+ ", client: " + client + ", broker: " + broker + ", emissionDate: " + emissionDate
                + ", state: " + state + ", processingDate: " + processingDate + ", desiredNbOfStock: " + desiredNbOfStock
                + ", actualNbOfStocks: " + actualNbOfStocks;
    }
}

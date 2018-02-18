package tulip.app.common.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

/**
 * Represents an order.
 * The client places an order, the order is sent by the broker of the client to the stock exchange, then, the
 * stock exchange processes the order.
 */
public class Order implements Serializable {

    /** ObjectMapper used to serialize and deserialize */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** A unique identifier of the order */
    @JsonProperty("id")
    private int id;

    /** The type of the order: purchase or sell */
    @JsonProperty("orderType")
    private OrderType orderType;

    /** The company corresponding to the stocks being sold or purchased */
    @JsonProperty("company")
    private String company;

    /** The name of the client who placed the order */
    @JsonProperty("client")
    private String client;

    /** The name of the broker responsible for sending the order to the stock exchange */
    @JsonProperty("broker")
    private String broker;

    /** The date on which the order was placed by the client */
    @JsonProperty("emissionDate")
    private Date emissionDate;

    /** The state of the order */
    @JsonProperty("state")
    private OrderState state;

    /** The date on which the order was processed by the stock exchange */
    @JsonProperty("processingDate")
    private Date processingDate;

    /**
     * The maximum price the client is willing to pay for each stock in the case of a purchase order and
     * the minimum price the client is willing to obtain for each stock in the case of a sell order.
     * */
    @JsonProperty("desiredPrice")
    private double desiredPrice;

    /** The price per stock actually paid to acquire the stocks or The price per stock actually obtained  when selling the stocks. */
    @JsonProperty("actualPrice")
    private double actualPrice;

    /** The number of stocks concerned by the order ie. the number of stocks that the client wants to purchase or sell */
    @JsonProperty("desiredNbOfStocks")
    private int desiredNbOfStocks;

    /** The number of stocks actually purchased or sold once the order has been processed */
    @JsonProperty("actualNbOfStocks")
    private int actualNbOfStocks;

    /**
     * Constructor
     */
    public Order(@JsonProperty("id") int id,
                 @JsonProperty("orderType") OrderType orderType,
                 @JsonProperty("company") String company,
                 @JsonProperty("client") String client,
                 @JsonProperty("broker") String broker,
                 @JsonProperty("desiredPrice") double desiredPrice,
                 @JsonProperty("desiredNbOfStocks") int desiredNbOfStocks) {
        this.id = id;
        this.orderType = orderType;
        this.company = company;
        this.client = client;
        this.state = OrderState.pending;
        this.broker = broker;
        this.emissionDate = new Date();
        this.desiredPrice = desiredPrice;
        this.desiredNbOfStocks = desiredNbOfStocks;
    }

    /**
     * Processes the order ie. fills the processing date, changes the order state to processed, and fills the actualPrice.
     */
    public void processOrder(Date processingDate, double actualPrice) {
        this.processingDate = processingDate;
        this.actualPrice = actualPrice;
        this.state = OrderState.processed;
    }

    @JsonIgnore
    public double getDesiredAmount() {
        return desiredPrice * desiredNbOfStocks;
    }

    @JsonIgnore
    public double getActualAmount() {
        return actualPrice * actualNbOfStocks;
    }

    /** Returns the JSON representation of the order */
    public String toJSON() {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /** Constructs an Order object from a JSON string */
    public static Order fromJSON(String json) {
        Order order = null;

        try {
            order = mapper.readValue(json, Order.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return order;
    }

    @Override
    public String toString() {
        return "id: " + id + ", company: " + company + ", client: " + client + ", broker: " + broker +
                ", emissionDate: " + emissionDate + ", state: " + state + ", processingDate: " + processingDate +
                ", desiredNbOfStocks: " + desiredNbOfStocks + ", actualNbOfStocks: " + actualNbOfStocks;
    }

    public int getId() {
        return id;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public String getCompany() {
        return company;
    }

    public String getClient() {
        return client;
    }

    public String getBroker() {
        return broker;
    }
    public Date getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(Date emissionDate) {
        this.emissionDate = emissionDate;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public Date getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(Date processingDate) {
        this.processingDate = processingDate;
    }

    public double getDesiredPrice() {
        return desiredPrice;
    }

    public double getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(double actualPrice) {
        this.actualPrice = actualPrice;
    }

    public int getDesiredNbOfStocks() {
        return desiredNbOfStocks;
    }

    public int getActualNbOfStocks() {
        return actualNbOfStocks;
    }

    public void setActualNbOfStocks(int actualNbOfStocks) {
        this.actualNbOfStocks = actualNbOfStocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id &&
                Double.compare(order.desiredPrice, desiredPrice) == 0 &&
                Double.compare(order.actualPrice, actualPrice) == 0 &&
                desiredNbOfStocks == order.desiredNbOfStocks &&
                actualNbOfStocks == order.actualNbOfStocks &&
                orderType == order.orderType &&
                Objects.equals(company, order.company) &&
                Objects.equals(client, order.client) &&
                Objects.equals(broker, order.broker) &&
                Objects.equals(emissionDate, order.emissionDate) &&
                state == order.state &&
                Objects.equals(processingDate, order.processingDate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, orderType, company, client, broker, emissionDate, state, processingDate, desiredPrice, actualPrice, desiredNbOfStocks, actualNbOfStocks);
    }
}
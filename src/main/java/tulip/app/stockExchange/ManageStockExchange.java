package tulip.app.stockExchange;
import java.util.Date;
import java.util.Map;

public interface ManageStockExchange {
	
	/**
	 * when a new Broker registers with the market (ie the StockExchange class), we add him to the Market's list of registered Brokers.
	 */
	public void registerBroker(String brkerName) ; 
	
	/**
	 * when a new client registers with a Broker, we must update the map (in the StockExchange class) which associates on one hand the name of
	 * the Broker, and on the other hand the number of clients he deals with 
	 */
	public void addClientToBroker (String brokerName, int nbClients) ; 
	
	/**
	 * this method return a map which give the Market state at a specific time. The map gives for each company name, its current stock value
	 */
	public Map<String,Double> readMarketState() ; 
	
	/**
	 * this method will place a Sell order and put it in the list of orders that the Market must execute.
	 */
	public void placeSellOrder (int idOrder, String client, String brokerName, String company, Date emissionDate, int nbStock, double minPrice, String clientName) ; 
	
	/**
	 * this method will place a Purchase order and put it in the list of orders that the Market must execute.
	 */
	public void placePurchaseOrder (int idOrder, String client, String brokerName, String company, Date emissionDate, int nbStock, double maxPrice, String clientName) ; 
	
	
}

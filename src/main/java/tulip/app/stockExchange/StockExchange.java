package tulip.app.stockExchange;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tulip.manageOrder.*;
import tulip.sockets.MultiServerSocket;
import tulip.sockets.MultiServerSocketThread;

/**
* this class represents the market, i.e the Stock exchange. 
* This class is a MultiServerSocket and will listen on a specific port for clients (Brokers) requests. 
* Once a request is held, a thread (MultiServerSocketThread) will be launched to reply to it without delaying the treatment of the other requests.
* Finally, this class will do all the actions we expect from a stock exchange : fairly treat submitted orders, update stock prices, notify brokers about stock prices changes, and so on... 
*
*  @author Nassym BEN KHALED
*/
public class StockExchange extends MultiServerSocket implements ManageStockExchange {
	/**
	* the socket name of the server
	*/
	final static String SERVER_SOCKET_NAME = "bourse" ; 
	
	/**
	* the port used by the server socket.
	*/
	final static int SERVER_PORT = 4000 ; 
	
	/**
	* a link to the server socket used for listening to connections.
	*/
	ServerSocket theServerSocket = null; 
	
	/**
	* this value is initially set to true (not started). Once the server is started (i.e the market is open), it will change to false. 
	* This value is exclusively used at the beginning to loop while waiting for the opening message ("OPEN").
	*/
	public boolean notStarted = true;
	
	/**
	* this value is initially set to true (market closed). 
	* When the market will open, it will switch to false.
	* When the market will close, it will switch to true. 
	*/
	public boolean closed = true ;
	
	/**
	* a list to store all the companies stored in our stockExchange app. 
	*/
	List<Company> companies ; 
	
	/**
	* a map that makes the link between a broker (identified by his name) and the number of clients who deal with him.
	*/
	Map <String, Integer> brokersAndNumberOfClients ; 
	
	/**
	* a map that makes the link between a broker (identified by his name) and the socket used by him to communicate with the server.
	*/
	Map <String, String> brokersAndSocketClientName ; 
	
	/**
	* this map will store, for each company, an information about supply and demand (respecting this order, in a list of Integer values)
	*/
	Map<Company, ArrayList<Integer>> supplyAndDemand ; 
	
	/**
	* this list will store the history of treated orders (and only them). 
	*/
	List<Order> history ; 
	
	/**
	* this mapper is used during the JSON creation process.
	*/
	ObjectMapper mapper = new ObjectMapper();
	
	/**
	* this mapper is also used during the JSON creation process (for building JSONArray)
	*/
    ArrayNode arrayNode = mapper.createArrayNode();
	
    /**
	*  the constructeur of the stockExchange object 
	*/
	public StockExchange () {
		super(SERVER_SOCKET_NAME, SERVER_PORT) ; 
		companies = new ArrayList<Company>() ; 
		brokersAndNumberOfClients = new HashMap<String, Integer>() ; 
		brokersAndSocketClientName = new HashMap<String, String>() ; 
		supplyAndDemand = new HashMap<Company, ArrayList<Integer>>() ; 
		CONSUMER = new ConsumerServer(this) ; 
	}
	
	/**
	 *  this method officially opens the market. 
	 *  in a v2, we can imagine retrieving (in a config .txt file) the last market state/list of registered brokers (saved when we closed it for the last time). 
	 *  Here, to make it easier to manipulate, we simply start the stock exchange (reset of market state)
	 */
	public void openStockExchange(){
		closed = false ; 
		System.out.println(" ----------- THE MARKET IS NOW OPEN  -----------") ; 
	}
	
	
	
	/**
	 *  this method updates the stock prices for the registered companies. 
	 */
	public void updateStockPrices() {
		
		for (Company company : companies) {
			/** we recover the supply/demand for company browsed */
			ArrayList<Integer> supplyAndDemandForCompany = supplyAndDemand.get(company) ; 
			
			/** update of the price stock with the mathematical formula */
			double deltaPrice = (supplyAndDemandForCompany.get(0) - supplyAndDemandForCompany.get(1) ) / company.nbStocksEmitted ; 
			double newPrice = company.stockPrice * (1 + deltaPrice) ; 
			company.stockPrice = newPrice ; 
		}
	}
	
	
	/**
	 *  this method notify all the registered brokers about the stock prices update.
	 */
	public void notifyBrokerOfPriceChanges() {
		
		String contentUpdatedMessage = ""; 
		
		/** create the JSONArray which represents the market state : for each company, we map its stockPrice. */
		for (Company company : companies) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("nameCompany", company.name); 				
			objectNode.put("priceStock", String.valueOf(company.stockPrice));
			arrayNode.add(objectNode);
		}
		
		/** create the string related to the JSONArray we just created. */
		try {
			contentUpdatedMessage = mapper.writeValueAsString(arrayNode) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} 
			
		/** now, we notify every broker about the stock prices update. */	
		for (String brokerName : brokersAndSocketClientName.keySet() ) {
			
			/** find each clientSocketName associated to the current broker's name.  */	
			String clientSocketNameOrder = brokersAndSocketClientName.get(brokerName) ;
				
			/** we then retrieve with this name the thread related to the Broker.  */
			MultiServerSocketThread threadOrder = registeredClients.get(clientSocketNameOrder) ; 
					
			/** we put the server's reply message in the thread's dedicated field */	
			threadOrder.replyFromServerWithOPCode.put("replyMessage", contentUpdatedMessage) ;
			threadOrder.replyFromServerWithOPCode.put("OP_CODE", "updatedMArketState") ;
				
			/** wake up the thread to resume the work, by changing their value of boolean isActionDone. It will then run the method for replying. */
			threadOrder.isActionDone = true ; 
		}
		
	}
	
	

	
	/**
	 *  this method will process a transaction in the market. 
	 */
	public void processTransactions() {
		/** Firstly : we recover the element at the head of the queue */
		Order order = null ; 
		try {
			order = orders.take() ;
		} catch (InterruptedException e) {
			System.out.println("interrupted while waiting in processTransaction ") ; 
			e.printStackTrace();
		} 
		
		/** if the order is already processed (by another transaction), we will remove it from the queue.*/
		if (order.getState().toString().equals("processed")) {
			history.add(order) ; 
			orders.remove() ; 
		}
		
		/** the order isn't already proceed, so we will treat it. */
		else {
			/** Secondly : we identify the real type of order and accordingly to it, we continue the treatment. */
			
			/** in case of a SellOrder */
			if (order instanceof SellOrder) {
				SellOrder sellOrder = (SellOrder) order ; 
				int nbStocksWantedSold = sellOrder.getDesiredNbOfStocks() ; 
				int nbActualStocks = 0 ;
				PurchaseOrder optimalPurchaseOrder = null ; 
				
				/** we assume that the  the transaction is flagged as feasible (should have been verified previously in Broker 
				 * after the submission of a SellOrder by his Client (i.e : min desired amount <= current stock price). */
				
				/** we update the data related to supply/demand for the company according to the number of stocks we want to sell. */
				Company thecompany = getCompanyWithName(sellOrder.getCompany()) ;
				ArrayList<Integer> newSupplyDemandData = new ArrayList<Integer>() ;
				ArrayList<Integer> oldSupplyDemandData = supplyAndDemand.get(thecompany) ; 
				newSupplyDemandData.add(oldSupplyDemandData.get(0) + nbStocksWantedSold) ; 
				newSupplyDemandData.add(oldSupplyDemandData.get(1)) ;
				supplyAndDemand.put(thecompany, newSupplyDemandData) ; 
			
				/** we try to find the optimal PurchaseOrder that satisfies as much as possible this SellOrder */
				optimalPurchaseOrder = findOptimalPurchaseOrder(nbStocksWantedSold, thecompany.name) ; 
				
				/** no optimal PurchaseOrder that matches found (with all stocks sold). 
				 * So we will try to sell as much as possible (even the order is partially completed).  */
				if (optimalPurchaseOrder == null) {
					optimalPurchaseOrder = findBestPurchaseOrder(nbStocksWantedSold, thecompany.name) ; 
					
					/** NO TRANSACTION POSSIBLE (no PurchaseOrder found => we can't sell anything)*/
					if (optimalPurchaseOrder == null) {
						/** we update the actual stocks sold */
						nbActualStocks = 0 ; 
						
						/** we update the order data */
						sellOrder.processOrder(nbActualStocks, new Date(), thecompany.stockPrice) ;
						
						/** only notify the seller (no transaction done) */
						notifyBrokerNoSell(sellOrder) ; 
						
						/** end of processing*/ 
						return ; 
					}
					
					/** TRANSACTION POSSIBLE (Partially)*/
					/** we update the actual stocks sold with the retrieved order*/
					nbActualStocks = Math.min(sellOrder.getDesiredNbOfStocks(), optimalPurchaseOrder.getDesiredNbOfStocks());
					
					/** we compute the mean of the actual price between the minimum price the seller is willing to sell 
					 * and the maximum price the purchaser is willing to pay */
					double actualPrice = (sellOrder.getMinSellingPrice() + optimalPurchaseOrder.getMaxPurchasingPrice())/2 ; 
					
					/** we then update the number of stocks sold, the processing date, and the actual price for the sellOrder */
					sellOrder.processOrder(nbActualStocks, new Date(), actualPrice) ; 
					
					/** we need also to update this in the optimalPurchaseOrder retrieved for the deal */
					optimalPurchaseOrder.processOrder(nbActualStocks, new Date(), actualPrice) ;
					
					/**we update the history of orders treated */
					history.add(sellOrder) ; 
					
					/** notify Brokers of 2 parts (the one of optimalPurchaseOrder and the one of sellOrder) */
					notifyBrokersOfTransaction(optimalPurchaseOrder, sellOrder);
					
					/** end of processing*/ 
					return ; 
				}
				
				/** TRANSACTION POSSIBLE (Completely)*/
				else {
					/** we update the actual stocks sold with the retrieved order*/
					nbActualStocks =  Math.min(sellOrder.getDesiredNbOfStocks(), optimalPurchaseOrder.getDesiredNbOfStocks());
					
					/** we compute the mean of the actual price between the minimum price the seller is willing to sell 
					 * and the maximum price the purchaser is willing to pay */
					double actualPrice = (sellOrder.getMinSellingPrice() + optimalPurchaseOrder.getMaxPurchasingPrice())/2 ; 
					
					/** we then update the number of stocks sold, the processing date, and the actual price for the sellOrder */
					sellOrder.processOrder(nbActualStocks, new Date(), actualPrice) ; 
					
					/** we need also to update this in the optimalPurchaseOrder retrieved for the deal */
					optimalPurchaseOrder.processOrder(nbActualStocks, new Date(), actualPrice) ;
					
					/**we update the history of orders treated */
					history.add(sellOrder) ; 
					
					/** notify Brokers of 2 parts (the one of optimalPurchaseOrder and the one of sellOrder) */
					notifyBrokersOfTransaction(optimalPurchaseOrder, sellOrder);
				
					/** end of processing*/ 
					return ; 
				}
			}
			
			/** in case of a PurchaseOrder */
			else {
				/** we assume that the  the transaction is flagged as feasible (should have been verified previously in Broker 
				 * after the submission of a PurchaseOrder by his Client (i.e : max desired amount <= current stock price). */
				
				PurchaseOrder purchaseOrder = (PurchaseOrder) order ; 
				Company thecompany = getCompanyWithName(purchaseOrder.getCompany()) ; 
				
				int nbStocksWanted = purchaseOrder.getDesiredNbOfStocks() ; 
				int nbFloatingStocks = thecompany.nbFloatingStocks ;
				int nbActualStocks = 0 ;
				SellOrder optimalSellOrder = null ; 
				
				/** we update the data related to supply/demand for the company according to the number of stocks we want to sell. */
				ArrayList<Integer> newSupplyDemandData = new ArrayList<Integer>() ;
				ArrayList<Integer> oldSupplyDemandData = supplyAndDemand.get(thecompany) ; 
				newSupplyDemandData.add(oldSupplyDemandData.get(0)) ; 
				newSupplyDemandData.add(oldSupplyDemandData.get(1) + nbStocksWanted) ;
				supplyAndDemand.put(thecompany, newSupplyDemandData) ; 
				
				/** TRANSACTION POSSIBLE (Completely) - with floating stocks only : 
				 * we can buy all the stocks desired from the floating stocks */
				if (nbStocksWanted <= nbFloatingStocks) {
					nbActualStocks = nbStocksWanted ; 
					
					/** we then update the processing date and the effective number of stocks purchased at the price of the market of the SellOrder */
					purchaseOrder.processOrder(nbActualStocks, new Date(), thecompany.stockPrice) ; 
					
					/**we update the history of orders treated */
					history.add(purchaseOrder) ; 
					
					/** notify the Broker involved in this purchaseOrder that the order was done with floating stocks */
					notifyBrokerPurchasedFromFloatingStocks(purchaseOrder);
					
					return ; 
					
				}

				/** TRANSACTION POSSIBLE (Partially) - or even impossible : we can try to mix 
				 * between floating stocks and the ones sold by Sellers through sellOrders */
				else {
					/** we take the max of floating stocks available (worst case : no stock available, i.e 0) */
					nbActualStocks = nbFloatingStocks ; 
					
					/** we determine the price for these stocks = the current stockPrice of the company*/
					double priceActualStocks = thecompany.stockPrice ; 
					
					/** we compute the number of stock that remains to satisfy the order (and that we will try to retrieve among SellOrders) */
					int nbStocksRemains = nbStocksWanted - nbActualStocks ; 
					
					int nbActualStocksForRemaining ; 
					
					/** we try to find the best SellOrder that satisfy our wishes (completely) in a fair way (for the seller) among SellOrders */
					optimalSellOrder = findOptimalSellOrder(nbStocksRemains, thecompany.name) ; 
				
					
					/** no optimal PurchaseOrder that matches found (with all stocks purchased). So we will try to purchase as much as possible.  */
					if (optimalSellOrder == null) {
						/** So we will try to purchase as much as possible.  */
						optimalSellOrder = findBestSellOrder(nbStocksRemains, thecompany.name) ; 
						
						/**NO TRANSACTION POSSIBLE (no SellOrder found => we can't purchase anything)*/
						if (optimalSellOrder == null) {
							/** at this step, either we got some floating stocks either not (worst case). We didn't success to fill the remaining stocks."*/
							
							purchaseOrder.processOrder(nbActualStocks, new Date(), priceActualStocks) ; 
							
							/** notify the Broker involved in this purchaseOrder depending on the success (even partial) of the order. */
							if (nbActualStocks == 0) {
								notifyBrokerNoPurchase(purchaseOrder) ;
							}
							else {
								notifyBrokerPurchasedFromFloatingStocks(purchaseOrder);
							}
							
							return ; 
						}
						
						/** TRANSACTION POSSIBLE (Partially) because findOptimalSellOrder returned null but not findBestSellOrder*/
						else {
							
							/** we update the actual stocks purchased with the retrieved order*/
							
							/** we compute the number of actual stocks purchased between what propose the seller and what remains for this orderPurchase*/
							nbActualStocksForRemaining =  Math.min(purchaseOrder.getDesiredNbOfStocks(), nbStocksRemains);
							
							/** we compute the actual value for the remaining stocks that we bought from a seller (with the same method than before)*/
							double actualFinalPriceForRemaining  = ((purchaseOrder.getMaxPurchasingPrice() + optimalSellOrder.getMinSellingPrice() )/2) ; 
							
							/** we compute the final actual value which is the weighted mean between the value of the floating stocks we bought from the market and the one of remaining stocks that we bought from a seller (with the same method than before)*/
							double actualFinalPriceTotal = ((priceActualStocks*nbActualStocks) + (actualFinalPriceForRemaining*nbActualStocksForRemaining) / (nbActualStocks+nbActualStocksForRemaining)) ; 
							
							/** so the number of actual stock is computed (with floating stocks and/or remaining stocks from sellers)*/
							nbActualStocks = nbActualStocks + nbActualStocksForRemaining  ; 
							
							/** we then update the number of stocks sold, the processing date, and the actual price for the purchaseOrder */
							purchaseOrder.processOrder(nbActualStocks, new Date(), actualFinalPriceTotal) ; 
							
							/** we need also to update this in the optimalSellOrder retrieved for the deal */
							optimalSellOrder.processOrder(nbActualStocks, new Date(), actualFinalPriceForRemaining) ;
							
							/**we update the history of orders treated */
							history.add(purchaseOrder) ; 
							
							/** notify Brokers of 2 parts (the one of optimalPurchaseOrder and the one of sellOrder) */
							notifyBrokersOfTransaction(optimalSellOrder, purchaseOrder);
							
							/** end of processing*/ 
							return ; 	
							
						}
					}	
					
					/** TRANSACTION POSSIBLE (Completely) for our remaining stocks with a seller because findOptimalSellOrder didn't returned null*/
					
					/** we update the actual stocks purchased with the retrieved order*/
					
					/** we compute the number of actual stocks purchased between what propose the seller and what remains for this orderPurchase*/
					nbActualStocksForRemaining =  Math.min(purchaseOrder.getDesiredNbOfStocks(), nbStocksRemains);
					
					/** we compute the actual value for the remaining stocks that we bought from a seller (with the same method than before)*/
					double actualFinalPriceForRemaining  = ((purchaseOrder.getMaxPurchasingPrice() + optimalSellOrder.getMinSellingPrice() )/2) ; 
					
					/** we compute the final actual value which is the weighted mean between the value of the floating stocks we bought from the market and the one of remaining stocks that we bought from a seller (with the same method than before)*/
					double actualFinalPriceTotal = ((priceActualStocks*nbActualStocks) + (actualFinalPriceForRemaining*nbActualStocksForRemaining) / (nbActualStocks+nbActualStocksForRemaining)) ; 
					
					/** so the number of actual stock is computed (with floating stocks and/or remaining stocks from sellers)*/
					nbActualStocks = nbActualStocks + nbActualStocksForRemaining  ; 
					
					/** we then update the number of stocks sold, the processing date, and the actual price for the purchaseOrder */
					purchaseOrder.processOrder(nbActualStocks, new Date(), actualFinalPriceTotal) ; 
					
					/** we need also to update this in the optimalSellOrder retrieved for the deal */
					optimalSellOrder.processOrder(nbActualStocks, new Date(), actualFinalPriceForRemaining) ;
					
					/**we update the history of orders treated */
					history.add(purchaseOrder) ; 
					
					/** notify Brokers of 2 parts (the one of optimalPurchaseOrder and the one of sellOrder) */
					notifyBrokersOfTransaction(optimalSellOrder, purchaseOrder);
					
					/** end of processing*/ 
					return ; 	

				}			
			}
		}
	}
	
	
	
	
	
	
	/**
	 * we try to smartly retrieve the "optimal" PurchaseOrder, i.e the one that pretty fits as much as possible for both parts
	 * and with which we can sell everything stock we intended to (the seller will have more priority since we actually 
	 * treat a SellOrder (from the queue of orders)
	 * @param nbStocksWantedSold number of stock we want to sell
	 * @param thecompanyName name of the company of the stocks we want to sell
	 * @return the "optimal" purchaseOrder in the queue that let us to sell everything AND that is the most advantageous for the purchaser 
	 * i.e that will approach his goal as much as possible.
	 */
	public PurchaseOrder findOptimalPurchaseOrder(int nbStocksWantedSold, String thecompanyName) {
		/** explicitly initialized with a very huge value (to be then well adjusted by searching the minimum)*/
		int minGap = 10000 ; 
		PurchaseOrder orderFound = null ; 
		PurchaseOrder temp = null ;
		String companyOfOrder = null ; 
		
		for (Order order : orders) {
			/** we only analyze PurchaseOrder objects*/
			if (order instanceof PurchaseOrder) {
				temp = (PurchaseOrder)order ; 
				companyOfOrder = temp.getCompany() ; 
				
				/** if we find a PurchaseOrder from someone who wants to buy at least as much stocks as we want to sell, we try to minimize the gap between each wish (to satisfy as much as possible both parts)*/
				if (temp.getDesiredNbOfStocks() >=  nbStocksWantedSold && thecompanyName.equals(companyOfOrder)) {
					if ((temp.getDesiredNbOfStocks() - nbStocksWantedSold) < minGap) {
						orderFound = temp ; 
					}
				}
			}
		}
		return orderFound ; 
	}
	
	
	/**
	 * we try to smartly retrieve the "best" PurchaseOrder, i.e the one that pretty fits as much as possible for both parts
	 * (i.e minimize the gap between the expected by the purchaser/the wanted by the seller (even if the seller will have more priority
	 * since we actually treat a SellOrder (from the queue of orders)
	 * and with which we can sell the max of stock purchases (unfortunately, not all otherwise we will never call this method since findOptimalPurchaseOrder returned an order). 
	 * @param nbStocksWantedSold number of stock we want to sell
	 * @param thecompanyName name of the company of the stocks we want to sell
	 * @return the "best" purchaseOrder in the queue that let us to sell as much as possible AND that is the most advantageous for the purchaser 
	 * i.e that will approach his goal as much as possible.
	 */
	public PurchaseOrder findBestPurchaseOrder(int nbStocksWantedSold, String thecompanyName) {
		/** explicitly initialized with the lowest value (to be then well adjusted by searching the maximum)*/
		int max = 0 ; 
		PurchaseOrder orderFound = null ; 
		PurchaseOrder temp = null ;
		String companyOfOrder = null ; 
		
		for (Order order : orders) {
			/** we only analyze PurchaseOrder objects*/
			if (order instanceof PurchaseOrder) {
				temp = (PurchaseOrder)order ; 
				companyOfOrder = temp.getCompany() ; 
				
				/** we try to find the best PurchaseOrder, i.e the order from someone who wants to buy the maximum of stocks 
				 * (but without reaching our desired number of stocks wanted to be sold) */
				if (thecompanyName.equals(companyOfOrder)) {
					/** if the order is better than the one we considered until there as "best", we update this "best" order. */
					 
					if (temp.getDesiredNbOfStocks() > max) {
						orderFound = temp ; 
					}
				}
			}
		}
		return orderFound ; 
		
		
	
	}
	
	/**
	 * we try to smartly retrieve the "optimal" SellOrder, i.e the one that pretty fits as much as possible for both parts
	 * and with which we can purchase every stock we intended to (the purchaser will have more priority since we actually 
	 * treat a purchaseOrder (from the queue of orders)
	 * @param nbStocksWantedSold number of stock we want to purchase
	 * @param thecompanyName name of the company of the stocks we want to purchase
	 * @return the "optimal" SellOrder in the queue that let us to purchase everything AND that is the most advantageous for the seller
	 * i.e that will approach his goal as much as possible.
	 */
	public SellOrder findOptimalSellOrder(int nbStocksToBuy, String thecompanyName) {
		/** explicitly initialized with a very huge value (to be then well adjusted by searching the minimum)*/
		int minGap = 10000 ; 
		SellOrder orderFound = null ; 
		SellOrder temp = null ;
		String companyOfOrder = null ; 
		
		for (Order order : orders) {
			/** we only analyze SellOrder objects*/
			if (order instanceof SellOrder) {
				temp = (SellOrder)order ; 
				companyOfOrder = temp.getCompany() ; 
				
				/** if we find a SellOrder from someone who wants to sell at least as much stocks as we want to sell, we try to minimize the gap between each wish (to satisfy as much as possible both parts)*/
				if ( (temp.getDesiredNbOfStocks() >=  nbStocksToBuy) && thecompanyName.equals(companyOfOrder)) {
					if ((temp.getDesiredNbOfStocks() - nbStocksToBuy) < minGap) {
						orderFound = temp ; 
					}
				}
			}
		}
		return orderFound ; 
	}
	
	
	/**
	 * we try to smartly retrieve the "best" SellOrder, i.e the one that pretty fits as much as possible for both parts
	 * and with which we can purchase the best number of stocks we intended to (the purchaser will have more priority since we actually 
	 * treat a purchaseOrder (from the queue of orders) - (unfortunately, not all stocks could be purchased otherwise we will never call this method since findOptimalSellOrder returned an order). 
	 * @param nbStocksWantedSold number of stock we want to purchase
	 * @param thecompanyName name of the company of the stocks we want to purchase
	 * @return the "best" SellOrder in the queue that let us to purchase as much as possible AND that is the most advantageous for the seller
	 * i.e that will approach his goal as much as possible.
	 */
	public SellOrder findBestSellOrder(int nbStocksToBuy, String thecompanyName) {

		/** explicitly initialized with the lowest value (to be then well adjusted by searching the maximum)*/
		int max = 0 ; 
		SellOrder orderFound = null ; 
		SellOrder temp = null ;
		String companyOfOrder = null ; 
	
		for (Order order : orders) {
			/** we only analyze SellOrder objects*/
			if (order instanceof SellOrder) {
				temp = (SellOrder)order ; 
				companyOfOrder = temp.getCompany() ; 
				
				/** we try to find the best SellOrder, i.e the order from someone who wants to sell the maximum of stocks
				 * (but without reaching our desired number of stocks wanted to be purchased) */
				if (thecompanyName.equals(companyOfOrder)) {
					/** if the order is better than the one we considered until there as "best", we update this "best" order. */
					if (temp.getDesiredNbOfStocks() > max) {
						orderFound = temp ; 
					}
				}
			}
		}
		return orderFound ; 
	}
	
	
	
	
	/**
	 * this method will return the Company object associated to a specific companyName
	 * @param companyName the name of the company we want to retrieve the object
	 * @return the Company object (null if unknown company)
	 */
	public Company getCompanyWithName (String companyName) {
		for (Company company : companies) {
			if (company.name.equals(companyName)) {
				return company ; 
			}
		}
		return null ; 
	}
	
	
	/**
	 * method that will be called to notify each Broker responsible the successful transaction between a PurchaseOrder
	 * and a SellOrder
	 * @param order1 the first order that was involved in the transaction
	 * @param order2 the second order that was involved in the transaction
	 */
	public void notifyBrokersOfTransaction(Order order1, Order order2) {
		
		/**create json message for each order */
		String jsonOrder1 = order1.toString() ; 
		String jsonOrder2 = order2.toString() ; 
		
		/**find each clientSocketName associated to each Broker's name */
		String clientSocketNameOrder1 = order1.getBroker() ; 
		String clientSocketNameOrder2 = order2.getBroker() ; 
		
		/**we then retrieve with this name the thread related to each Broker*/
		MultiServerSocketThread threadOrder1 = registeredClients.get(clientSocketNameOrder1) ; 
		MultiServerSocketThread threadOrder2 = registeredClients.get(clientSocketNameOrder2) ; ; 
		
		/**we put the reply message of the server in the dedicated map of each thread
		 * responsible for the client's request treatment and that submitted the orders involved in the transaction*/
		threadOrder1.replyFromServerWithOPCode.put("replyMessage", jsonOrder1) ;
		threadOrder1.replyFromServerWithOPCode.put("OP_CODE", new Boolean(true).toString()) ;
		
		threadOrder2.replyFromServerWithOPCode.put("replyMessage", jsonOrder2) ;
		threadOrder2.replyFromServerWithOPCode.put("OP_CODE", new Boolean(true).toString()) ;
		
		/**we wake these threads up by changing their values of isActionDone boolean. They will run the method for replying*/
		threadOrder1.isActionDone = true ; 
		threadOrder2.isActionDone = true ;
		
	}
	
	/**
	 * method that will be called to notify a Broker responsible of a PurchaseOrder, that was a fail 
	 * (no completed due to a lack of purchasers)
	 * @param order the order submitted with updated fields 
	 */
	public void notifyBrokerNoSell(Order order) {
		/**create json message for the order */
		String jsonOrder = order.toString() ; 
		
		/**find the clientSocketName associated to the Broker's name */
		String clientSocketNameOrder = order.getBroker() ;
		
		/**we then retrieve with this name the thread related to the Broker*/
		MultiServerSocketThread threadOrder = registeredClients.get(clientSocketNameOrder) ; 
		
		/**we put the reply message of the server in the dedicated map of the thread
		 * responsible for the client's request treatment and that submitted the order */
		threadOrder.replyFromServerWithOPCode.put("replyMessage", jsonOrder) ;
		threadOrder.replyFromServerWithOPCode.put("OP_CODE", new Boolean(false).toString()) ;
		
		/**we wake this thread up by changing its values of isActionDone boolean. It will run the method for replying*/
		threadOrder.isActionDone = true ; 
	}
	
	/**
	 * method that will be called to notify a Broker responsible of a PurchaseOrder, that was a fail 
	 * (no completed due to a lack of sellers)
	 * @param order the order submitted with updated fields 
	 */
	public void notifyBrokerNoPurchase(Order order) {
		/**create json message for the order */
		String jsonOrder = order.toString() ; 
				
		/**find the clientSocketName associated to the Broker's name */
		String clientSocketNameOrder = order.getBroker() ;
		
		/**we then retrieve with this name the thread related to the Broker*/
		MultiServerSocketThread threadOrder = registeredClients.get(clientSocketNameOrder) ; 
				
		/**we put the reply message of the server in the dedicated map of the thread
		 * responsible for the client's request treatment and that submitted the order */
		threadOrder.replyFromServerWithOPCode.put("replyMessage", jsonOrder) ;
		threadOrder.replyFromServerWithOPCode.put("OP_CODE", new Boolean(false).toString()) ;
		
		/**we wake this thread up by changing its values of isActionDone boolean. It will run the method for replying*/
		threadOrder.isActionDone = true ; 
	}
	
	/**
	 * method that will be called to notify a Broker responsible of a PurchaseOrder, that was correctly performed 
	 * by buying only floating stocks
	 * @param order the order submitted with updated fields 
	 */
	public void notifyBrokerPurchasedFromFloatingStocks(Order order) {
		/**create json message for the order */
		String jsonOrder = order.toString() ; 
		
		/**find the clientSocketName associated to the Broker's name */
		String clientSocketNameOrder = order.getBroker() ;
		
		/**we then retrieve with this name the thread related to the Broker*/
		MultiServerSocketThread threadOrder = registeredClients.get(clientSocketNameOrder) ; 
				
		/**we put the reply message of the server in the dedicated map of the thread
		 * responsible for the client's request treatment and that submitted the order */
		threadOrder.replyFromServerWithOPCode.put("replyMessage", jsonOrder) ;
		threadOrder.replyFromServerWithOPCode.put("OP_CODE", "floatingStocks") ;
		
		/**we wake this thread up by changing its values of isActionDone boolean. It will run the method for replying*/
		threadOrder.isActionDone = true ; 
	}
	
	
	/**
	 * method that will close the stockExchange and perform all the action needed to do so 
	 * updating stock prices, interrupting every thread that serves a client of this server (so a Broker), closing the ServerSocket
	 * and finally interrupting the current Thread related to our server.
	 *  in a v2, we can imagine saving (in a config .txt file) the market state/list of registered brokers. 
	 */
	public void closeStockExchange() {
		
		/**we firstly update stock prices */
		updateStockPrices() ; 
		
		/**we then update the boolean variable associated to the market closing */
		closed = true ; 
		System.out.println(" ----------- THE MARKET IS NOW CLOSED  -----------") ; 
		
		/**we interrupt each Thread launched for treating Broker's requests */
		for (String brokerName : registeredClients.keySet()) {
			registeredClients.get(brokerName).interrupt();
		}
		
		/**we stop listening the socket connection  */
		try {
			theServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/**we interrupt current Thread */
		interrupt() ; 
	}
	
	
	/**
	 * method that will check if a Broker (identified by his name) is registered in the stockExchange or not
	 * @param nameBroker the name of the Broker we want to verify
	 * @return true if he is registered. False otherwise
	 */
	public boolean checkBrokerRegistered(String nameBroker) {
		for (String broker : brokersAndNumberOfClients.keySet()) {
			if (broker.equals(nameBroker)) {
				return true ; 
			}
		}
		return false ; 
	}
	
	/**
	 * method that will remove a Broker (identified by his name) in the stockExchange
	 * @param nameBroker the name of the Broker we want to remove from the market
	 */
	public void removeBroker(String nameBroker){
		if (brokersAndNumberOfClients.containsKey(nameBroker)) {
			brokersAndNumberOfClients.remove(nameBroker) ; 
			
			/**we verify that it remains registered brokers. Otherwise, we close the market. */
			if (brokersAndNumberOfClients.isEmpty()) {
				System.out.println("emptyMap, every broker has been disconnected. So we can close the market ") ; 
				closeStockExchange();
			}
		}
		else {
			System.out.println("there is no broker with this name ! ") ; 
		}
	}
	
	
	/**
	 * method that will register a Broker (identified by his name) in the stockExchange
	 * @param nameBroker the name of the Broker we want to register
	 */
	@Override
	public void registerBroker(String brkerName) {
		// useless since the next method will be more efficient
		
	}
	
	

	/**
	 * method that will add a Client to a registered Broker (identified by his name) in the stockExchange
	 * if the Broker doesn't exists, he is registered. 
	 * @param nameBroker the name of the Broker we want to update the number of associated clients 
	 * @param the updated number of Clients of the Brokers
	 */
	@Override
	public void addClientToBroker(String brokerName, int nbClients) {
		System.out.println("Adding a broker and his number of clients to the list of registered brokers ");
		brokersAndNumberOfClients.put(brokerName, nbClients) ; 	
	}

	
	
	/**
	 * method that will read the market state (i.e for each company, its stock price)
	 * @return a Map that make the link between a company name and its stock price
	 */
	@Override
	public Map<String, Double> readMarketState() {
		Map<String, Double> marketState = new HashMap<String, Double>() ;
		String name ; 
		Double stockValue ; 
		
		for (Company company : companies) {
			name = company.name ; 
			stockValue = company.stockPrice ; 
			marketState.put(name, stockValue) ; 
		}
		return marketState ; 
	}
	
	
	/**
	 * method that will read the market state, parse it to JSONArray and send it to the broker which wants to disconnect
	 * @param threadClient the reference of the MultiServerSocketThread for putting the message in the right field
	 */
	
	public void sendMarketStateClientThread(MultiServerSocketThread threadClient) {
		Map<String, Double> marketState = readMarketState() ; 
		
		/** creation of the JSONArray related to the market state map*/
		String contentMarketState = ""; 
		
		/** create the JSONArray which represents the market state : for each company, we map its stockPrice. */
		for (String company : marketState.keySet()) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("nameCompany", company); 				
			objectNode.put("priceStock", String.valueOf(marketState.get(company)));
			arrayNode.add(objectNode);
		}
		
		/** create the string related to the JSONArray we just created. */
		try {
			contentMarketState = mapper.writeValueAsString(arrayNode) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} 
		
		/** we put the server's reply message in the thread's dedicated field */	
		threadClient.replyFromServerWithOPCode.put("replyMessage", contentMarketState) ;
		threadClient.replyFromServerWithOPCode.put("OP_CODE", "marketState") ;
		
	}
	
	
	

	/**
	 * method that will place a SellOrder
	 * @param idOrder the order id
	 * @param client the client name
	 * @param brokerName the name of the broker associated to the client
	 * @param company the company's name
	 * @param emissionDate the emission date of the order
	 * @param nbStock the wanted number of stocks that the Client want to sell
	 * @param minPrice the minimum price the Client want to sell
	 * @param clientSocketName the name of the client socket
	 */
	@Override
	public void placeSellOrder (int idOrder, String client, String brokerName, String company, Date emissionDate, int nbStock, double minPrice, String clientSocketName) {
		/** we recreate the SellOrder with the data given in arguments */
		Order sellOrder = new SellOrder(idOrder, company, client, brokerName, emissionDate, nbStock, minPrice) ; 
		
		/** we map the broker's name and the name of its client socket */
		brokersAndSocketClientName.put(brokerName, clientSocketName) ; 
				
		/** we put it into the queue of orders */
		try {
			orders.put(sellOrder);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * method that will place a purchaseOrder
	 * @param idOrder the order id
	 * @param client the client name
	 * @param brokerName the name of the broker associated to the client
	 * @param company the company's name
	 * @param emissionDate the emission date of the order
	 * @param nbStock the wanted number of stocks that the Client want to sell
	 * @param minPrice the minimum price the Client want to sell
	 * @param clientSocketName the name of the client socket
	 */
	@Override
	public void placePurchaseOrder (int idOrder, String client, String brokerName, String company, Date emissionDate, int nbStock, double maxPrice, String clientSocketName) {
		/** we recreate the PurchaseOrder with the data given in arguments */
		Order purchaseOrder = new PurchaseOrder(idOrder, company, client, brokerName, emissionDate, nbStock, maxPrice) ; 
		
		/** we map the broker's name and the name of its client socket */
		brokersAndSocketClientName.put(brokerName, clientSocketName) ; 
		
		/** we put it into the queue of orders */
		try {
			orders.put(purchaseOrder);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * method run of our server (which is basically a Thread)
	 */
	@Override
	public void run() {
		System.out.println("MultiServerSocket for the MARKET starting ");
		
		waitOpeningMarket();
		
		/**opening the market  */
		openStockExchange(); 
		
        System.out.println("Market opened. IT IS NOW OPENED ");	
        System.out.println("We can do all the actions we expect from the market during a working day ");
		
        /** treatment of the client requests that the server receives */
        while (!closed) {
        		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
        			theServerSocket = serverSocket ; 
        			while (true) {
        				/** Creates a new thread each time a new client socket initiates a connection with MultiServerSocket */
        				new MultiServerSocketThread(this, SERVER_SOCKET_NAME, serverSocket.accept()).start();
        			}
        		} catch (IOException e) {
        			System.out.println("IO Exception");
        			e.printStackTrace();
        		}
        }
        
        System.out.println("MultiServerSocket quitting. MARKET CLOSED.");
	}

	/**
	 * method that will loop until the market opens
	 * (the thread will sleep)
	 */
	public void waitOpeningMarket() {
		System.out.println("Starting to loop. Waiting for the market opening. ");
        while (notStarted) {
            //System.out.println("Running loop...Still not opened");
            try {
                Thread.sleep(5000);
            } 
            catch (InterruptedException e) {
            }
        }
	}
	
	
	
	
}

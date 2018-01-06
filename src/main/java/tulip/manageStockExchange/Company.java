package tulip.manageStockExchange;
import java.util.ArrayList;
import java.util.List;

/**
* this class represents a company
*  @author Nassym BEN KHALED
*/
public class Company {
	/**
	 * the company's name
	 */
	String name ; 
	
	/**
	 * the number of emitted stocks
	 */
	int nbStocksEmitted ; 
	
	/**
	 * the number of floating stocks (initially corresponds to the number of emitted stocks)
	 */
	int nbFloatingStocks ; 
	
	/**
	 * the stock's price for this company
	 */
	double stockPrice ; 
	
	/**
	 * the market capitalization related to the company
	 */
	double marketCap ; 
	
	/**
	 * the market stockExchange where the company is registered
	 */
	StockExchange market ; 
	
	/**
	 * the company's list of stocks 
	 */
	List<Stock> listStocks ; 
	
	/**
	 * constructor of Company
	 */
	public Company(String name, int nbStocksMax, double initialStockPrice, StockExchange market) {
		this.name = name ; 
		stockPrice = initialStockPrice ; 
		this.market = market ; 
		listStocks = new ArrayList<Stock>() ; 
		
		/** we emit all the stocks at the initial price */
		emitStocks(nbStocksMax, initialStockPrice) ; 
		
		/** initially, the number of floating stocks corresponds to the one of emitted stocks. 
		 * Then, we update the company's market capitalization.
		 */
		nbFloatingStocks = nbStocksEmitted ; 
		marketCap = initialStockPrice * nbStocksEmitted ; 
		
	}
	
	/**
	 * this method will create a company with the parameters given in argument and will return it
	 * @param the name of the company
	 * @param the maximum number of stocks the company can emit
	 * @param the initial stock price
	 * @param the market where the company is registered
	 *
	 */
	public Company createCompany(String name, int nbStocksMax, double initialStockPrice, StockExchange market) {
		return new Company(name, nbStocksMax, initialStockPrice, market) ; 
	}
	
	
	/**
	 * this method will emit the stocks of the company
	 * @param the number of stocks we want to emit 
	 * @param the issue price of each stock
	 */
	public void emitStocks(int nbStocksEmitted, double issuePrice) {
		
		for(int i=0 ; i<nbStocksEmitted ; i++) {
			listStocks.add(new Stock(this)) ; 
		}
		
		this.nbStocksEmitted = nbStocksEmitted ; 
	}
	
	
	
	
	
}

package tulip.manageStockExchange;

import java.util.Date;

import tulip.manageClient.Portfolio;

public class Stock {
	/**
	 * a counter to give a unique ID to each stock created
	 */
	static int counter = 0; 
	
	/**
	 * a unique ID for the stock
	 */
	int idNumber ; 
	
	/**
	 * the emitted date of the stock 
	 */
	Date emissionDate ; 
	
	/**
	 * the company related of the stock 
	 */
	Company company ; 
	
	/**
	 * the portfolio which the stock belongs to
	 */
	Portfolio portfolio ;
	
	/**
	 * constructor of a stock
	 */
	public Stock(Company company) {
		idNumber = counter++ ; 
		this.company = company ; 
		emissionDate = new Date() ; 
		portfolio = null ; 
	}
	
	
	
	
}

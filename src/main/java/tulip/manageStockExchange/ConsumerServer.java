package tulip.manageStockExchange;
import tulip.sockets.MultiServerSocket;
/**
 * This class is a more specific Producer. Indeed, this producer behaves by considering a stockExchange market.
 * This thread will consume (and treat) every message taken from the orders' queue of the stockExchangeMarket
 * @author Nassym BEN KHALED
 *
 */
public class ConsumerServer implements Consumer, Runnable {
	
	/**
	 * the stockExchangeMarket associated to this Consumer
	 */
	MultiServerSocket serverAssociated ; 
	
	
	public ConsumerServer(MultiServerSocket server) {
		serverAssociated = server ; 
	}


	/**
	 * the run method of the thread : continuously take an order from the orders' queue (of the stockExchange) and treat it 
	 */
	@Override
	public void run() {
		StockExchange stockExchange = (StockExchange)serverAssociated ; 
		
		while(true) {
			stockExchange.processTransactions();
		}

	}

}

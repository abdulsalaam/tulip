package tulip.app.stockExchange;

import org.junit.Test;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.app.stockExchange.model.StockExchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class StockExchangeTest {

    @Test
    public void processTransactions() throws IOException {
        // Companies
        StockExchange stockExchange = new StockExchange(new ServerSocket());
        stockExchange.addCompany("Basecamp", 200, 45);
        stockExchange.addCompany("Alphabet", 300, 70);
        stockExchange.addCompany("Spotify", 400, 950);
        System.out.println(stockExchange.companies.get("Basecamp").getNbFloatingStocks());

        // Pending orders
        Order order = new Order(118, OrderType.purchase, "Basecamp", "David", "Steven", 30, 100);
        stockExchange.companies.get("Basecamp").pendingPurchaseOrders.add(order);
        assertTrue(stockExchange.companies.get("Basecamp").getPendingPurchaseOrders().contains(order));
        stockExchange.processTransactions();
        assertFalse(stockExchange.companies.get("Basecamp").getPendingPurchaseOrders().contains(order));
    }

}


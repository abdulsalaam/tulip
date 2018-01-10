package tulip.app.stockExchange;

import org.junit.Test;
import tulip.app.client.model.Client;
import tulip.app.order.Order;
import tulip.app.order.OrderType;
import tulip.app.stockExchange.model.StockExchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        Order bcOrder = new Order(118, OrderType.purchase, "Basecamp", "David", "Steven", 30, 100);
        Order bcOrderBis = new Order(118, OrderType.purchase, "Basecamp", "David", "Steven", 30, 1000);
        //Order alphaOrder = new Order(118, OrderType.purchase, "Alphabet", "Joe", "Youris", 300, 1000);
        // stockExchange.companies.get("Basecamp").pendingPurchaseOrders.add(bcOrder);
        stockExchange.companies.get("Basecamp").pendingPurchaseOrders.add(bcOrderBis);
        // assertTrue(stockExchange.companies.get("Basecamp").getPendingPurchaseOrders().contains(bcOrder));

        // Amount desired
        //assertTrue(stockExchange.companies.get("Basecamp").pendingPurchaseOrders.element().getActualAmount() == 0);
        // assertTrue(stockExchange.companies.get("Basecamp").pendingPurchaseOrders.element().getDesiredAmount() == 3000);

        System.out.println((stockExchange.companies.get("Basecamp").pendingPurchaseOrders.element().getActualAmount()));
        System.out.println((stockExchange.companies.get("Basecamp").pendingPurchaseOrders.element().getDesiredAmount()));
        // Order processed
        stockExchange.processTransactions();
        assertFalse(stockExchange.companies.get("Basecamp").getPendingPurchaseOrders().contains(bcOrder));
        assertFalse(stockExchange.companies.get("Basecamp").getPendingPurchaseOrders().contains(bcOrderBis));

        // Actual amount
       // assertTrue(bcOrder.getActualAmount() == 4500);
        //assertTrue(bcOrder.getDesiredAmount() == 3000);
        System.out.println((bcOrderBis.getActualAmount()));
        System.out.println((bcOrderBis.getDesiredAmount()));
    }

}


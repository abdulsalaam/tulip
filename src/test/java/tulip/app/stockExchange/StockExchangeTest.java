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

        /*// Companies
        StockExchange stockExchange = new StockExchange(new ServerSocket());
        stockExchange.addCompany("Basecamp", 200, 45);

        // Pending order
        Order bcOrder = new Order(118, OrderType.purchase, "Basecamp", "David", "Steven", 30, 100);

        // Amount desired
        assertTrue(bcOrder.getActualAmount() == 0);
        assertTrue(bcOrder.getDesiredAmount() == 3000);



        // Actual amount
        assertTrue(bcOrder.getActualAmount() == 4500);
        assertTrue(bcOrder.getDesiredAmount() == 3000);*/

    }

}
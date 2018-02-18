package tulip.app.common.model.order;

import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertTrue;

public class OrderTest {

    @Test
    public void orderSerialization() {
        Order orderSent = new Order(118, OrderType.purchase, "Basecamp", "David", "Steven", 984, 100);
        String json = orderSent.toJSON();
        System.out.println(json);
        Order orderReceived = Order.fromJSON(json);
        assertTrue(orderSent.equals(orderReceived));

        orderSent.processOrder(new Date(10000000), 60);
        json = orderSent.toJSON();
        System.out.println(json);
        orderReceived = Order.fromJSON(json);
        assertTrue(orderSent.equals(orderReceived));
    }

}
package tulip.app.order;

import org.junit.Test;

import java.util.Date;

public class SellOrderTest {

    @Test
    public void sellOrderTest() {
        SellOrder sellOrder = new SellOrder(118, "David", "Steven", new Date(), 487, 47);
        System.out.println(sellOrder.toString());
    }

}
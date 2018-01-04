package tulip.manageOrder;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class SellOrderTest {

    @Test
    public void sellOrderTest() {
        SellOrder sellOrder = new SellOrder(118, "Google","David", "Steven", new Date(), 487, 47);
        System.out.println(sellOrder.toString());
    }

}
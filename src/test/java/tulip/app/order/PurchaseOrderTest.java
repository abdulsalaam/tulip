package tulip.app.order;

import org.junit.Test;

import java.util.Date;

public class PurchaseOrderTest {

    @Test
    public void purchaseOrderTest() {
        PurchaseOrder purchaseOrder = new PurchaseOrder(118,"Basecamp", "David", "Steven", new Date(), 984, 100);
        System.out.println(purchaseOrder.toString());
    }

}
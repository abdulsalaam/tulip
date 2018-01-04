package tulip.manageOrder;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class PurchaseOrderTest {

    @Test
    public void purchaseOrderTest() {
        PurchaseOrder purchaseOrder = new PurchaseOrder(118, "Basecamp","David", "Steven", new Date(), 984, 100);
        System.out.println(purchaseOrder.toString());
    }

}
package tulip.app;

import org.junit.Test;
import tulip.app.common.model.MarketState;

import static org.junit.Assert.*;

public class MarketStateTest {

    @Test
    public void marketState() {

        MarketState marketStateSent = new MarketState();
        marketStateSent.put("Google", 120.0);
        marketStateSent.put("Apple", 240.0);
        String json = marketStateSent.toJSON();
        System.out.println(json);
        MarketState marketStateReceived = MarketState.fromJSON(json);
        assertTrue(marketStateReceived.get("Google").equals(marketStateSent.get("Google")));
        assertTrue(marketStateReceived.get("Apple").equals(marketStateSent.get("Apple")));

    }

}
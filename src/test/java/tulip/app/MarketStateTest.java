package tulip.app;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarketStateTest {

    @Test
    public void marketState() {

        MarketState marketStateSent = new MarketState();
        marketStateSent.put("Google", 120);
        marketStateSent.put("Apple", 240);
        String json = marketStateSent.toJSON();
        System.out.println(json);
        MarketState marketStateReceived = MarketState.fromJSON(json);
        assertTrue(marketStateReceived.get("Google").equals(marketStateSent.get("Google")));
        assertTrue(marketStateReceived.get("Apple").equals(marketStateSent.get("Apple")));

    }

}
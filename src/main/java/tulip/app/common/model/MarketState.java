package tulip.app.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class MarketState extends HashMap<String, Double> {

    /** ObjectMapper used to serialize and deserialize */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Returns the JSON representation of the MarketState */
    public String toJSON() {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /** Constructs a MarketState from a JSON string */
    public static MarketState fromJSON(String json) {
        MarketState marketState = null;

        try {
            marketState = mapper.readValue(json, MarketState.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return marketState;
    }
}

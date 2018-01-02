package tulip.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        /*MultiServer multiServer = new MultiServer(4000);
        multiServer.start();
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            Client client = new Client(localhost, 4000, "Thibaud");
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        class Name implements Serializable {
            public String firstName;
            public String lastName;

            Name(String firstName, String lastName) {
                this.firstName = firstName;
                this.lastName = lastName;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(new Name("Thibaud", "Martinez"));
            Map<String, String> connection = mapper.readValue(jsonString, Map.class);

            for (Map.Entry<String, String> entry : connection.entrySet()) {
                System.out.println("Key : " + entry.getKey());
                System.out.println("Value : " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

package tulip.service.producerConsumer.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MessageTest {
    @Test
    public void JSONSerialization() {

        ObjectMapper mapper = new ObjectMapper();

        Message messageSent = new Message(Target.producer, ContentType.app,"{ \"client\" : \"Thibaud\" }");
        String jsonSent = messageSent.toJSON();

        // Prettily print json message
        try {
            System.out.println("-- Message --");
            System.out.println(
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(jsonSent, Object.class))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print content
        System.out.println("-- Content --");
        System.out.println(messageSent.getContent());

        Message messageReceived = Message.fromJSON(jsonSent);

        assertTrue(messageSent.equals(messageReceived));

    }
}
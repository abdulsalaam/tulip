package tulip.app.appMessage;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppMessageTest {

    @Test
    public void appMessageTest() {

        AppMessage appMessageSent =
                new AppMessage("David", ActorType.client, "Steven", ActorType.broker,
                        AppMessageContentType.registrationRequest, "Georges");

        String json = appMessageSent.toJSON();

        AppMessage appMessageReceived = AppMessage.fromJSON(json);
        appMessageSent.equals(appMessageReceived);

    }
}
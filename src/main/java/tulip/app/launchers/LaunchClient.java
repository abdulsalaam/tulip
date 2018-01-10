package tulip.app.launchers;

import tulip.app.client.model.Client;

import java.io.IOException;
import java.net.Socket;

public class LaunchClient {

    public static void main(String[] args) {
        try {
            new Client("Quentin", 100000, new Socket("127.0.0.1", 5000));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

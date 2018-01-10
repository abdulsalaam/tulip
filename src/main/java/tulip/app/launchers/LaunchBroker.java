package tulip.app.launchers;

import tulip.app.broker.model.Broker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LaunchBroker {

    public static void main(String[] args) {

        try {
            Broker broker = new Broker(
                    "Martin",
                    new ServerSocket(5000),
                    new Socket("127.0.0.1", 4000)
            );

            broker.start();

            // broker.registerToStockExchange();
            // broker.getMarketState();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

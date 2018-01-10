package tulip.app.launchers;


import tulip.app.stockExchange.model.StockExchange;

import java.io.IOException;
import java.net.ServerSocket;

public class LaunchStockExchange {

    public static void main(String[] args) {

        try {
            new StockExchange(new ServerSocket(4000)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

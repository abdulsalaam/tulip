package tulip.app;

import tulip.app.broker.view.BrokerUI;
import tulip.app.client.view.ClientUI;
import tulip.app.stockExchange.view.StockExchangeUI;

public class Launcher {

    /*
    stock-exchange server-socket
    broker name server-socket-port socket-host socket-port
    client name socket-host socket-port
     */

    public static void main(String[] args) {
        switch (args[0]) {

            case "stock-exchange":
                StockExchangeUI.startup(Integer.parseInt(args[1]));
                break;

            case "broker":
                BrokerUI.startup(args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
                break;

            case "client":
                ClientUI.startup(args[1], args[3], Integer.parseInt(args[4]));
                break;

            default:
                System.out.println("Please enter valid arguments");

        }
    }
}

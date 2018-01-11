package tulip.app;

import tulip.app.broker.view.BrokerUI;
import tulip.app.client.view.ClientUI;
import tulip.app.stockExchange.view.StockExchangeUI;

public class Launcher {

    public static void main(String[] args) {
        switch (args[0]) {

            case "stock-exchange":
                StockExchangeUI.startup(Integer.parseInt(args[1]));
                break;

            case "broker":
                BrokerUI.startup(args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
                break;

            case "client":
                ClientUI.startup(args[1], args[2], Integer.parseInt(args[3]));
                break;

            default:
                System.out.println("Please enter valid arguments");
                System.out.println("To launch the stock exchange:");
                System.out.println("    stock-exchange [server-socket-port]");
                System.out.println("To launch the broker:");
                System.out.println("    broker [name] [server-socket-port] [socket-host] [socket-port]");
                System.out.println("To launch the client");
                System.out.println("    client [name] [socket-host] [socket-port]");
                break;
        }
    }
}

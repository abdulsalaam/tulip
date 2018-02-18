package tulip;

import org.apache.commons.cli.*;
import tulip.app.broker.view.BrokerUI;
import tulip.app.client.view.ClientUI;
import tulip.app.stockExchange.view.StockExchangeUI;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("se", "stock-exchange", false, "Launch a stock exchange instance.");
        options.addOption("b", "broker", false, "Launch a broker instance.");
        options.addOption("c", "client", false, "Launch a client instance.");
        options.addOption("n", "name", true, "Broker's or client's name. (Use only with --broker and --client.)");
        options.addOption("sp", "server-port", true, "The port the server socket listens to. (Use only with --stock-exchange and --broker.)");
        options.addOption("h", "host", true, "The host the socket connects to. (Use only with --broker and --client.)");
        options.addOption("p", "port", true, "The port the socket connects to. (Use only with --broker and --client.)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error: invalid command line format.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tulip", options);
            System.exit(1);
        }

        try {
            if (cmd.hasOption("se")) {

                if (!cmd.hasOption("sp")) { throw new ParseException("Missing mandatory --server-port argument."); }
                StockExchangeUI.launch(args);

            } else if (cmd.hasOption("b")) {

                if (!cmd.hasOption("n")) { throw new ParseException("Missing mandatory --name argument."); }
                if (!cmd.hasOption("sp")) { throw new ParseException("Missing mandatory --server-port argument."); }
                if (!cmd.hasOption("h")) { throw new ParseException("Missing mandatory --host argument."); }
                if (!cmd.hasOption("p")) { throw new ParseException("Missing mandatory --port argument."); }
                BrokerUI.launch(args);

            } else if (cmd.hasOption("c")) {

                if (!cmd.hasOption("n")) { throw new ParseException("Missing mandatory --name argument."); }
                if (!cmd.hasOption("h")) { throw new ParseException("Missing mandatory --host argument."); }
                if (!cmd.hasOption("p")) { throw new ParseException("Missing mandatory --port argument."); }
                ClientUI.launch(args);

            } else {
                throw new ParseException("You must specify at least one of the following options: --stock-exchange --broker --client ");
            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tulip", options);
            System.exit(1);
        }

        System.exit(0);
    }
}

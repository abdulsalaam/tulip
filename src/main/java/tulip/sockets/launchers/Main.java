package tulip.sockets.launchers;

import tulip.sockets.ClientSocket;
import tulip.sockets.MultiServerSocket;

public class Main {

    public static void main(String[] args) {
        String localhost = "127.0.0.1";
        int port = 4000;

        MultiServerSocket multiServer = new MultiServerSocket("Bourse", port);
        multiServer.start();
        try {
            ClientSocket client = new ClientSocket(localhost, port, "Thibaud");
            client.start();
            ClientSocket client2 = new ClientSocket(localhost, port, "Yanis");
            client2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

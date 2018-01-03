package tulip.sockets.launchers;

import tulip.sockets.ClientSocket;

public class LaunchClient1 {

    public static void main(String[] args) {

        String localhost = "127.0.0.1";
        int port = 4000;

        try {
            ClientSocket client = new ClientSocket(localhost, port, "Thibaud");
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

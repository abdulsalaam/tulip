package tulip.sockets.launchers;

import tulip.sockets.ClientSocket;

public class LaunchClient2 {

    public static void main(String[] args) {

        String localhost = "127.0.0.1";
        int port = 4000;

        try {
            ClientSocket client = new ClientSocket(localhost, port, "Yanis");
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

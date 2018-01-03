package tulip.sockets.launchers;

import tulip.sockets.MultiServerSocket;

public class LaunchServer {

    public static void main(String[] args) {
        int port = 4000;

        MultiServerSocket multiServer = new MultiServerSocket("Bourse", port);
        multiServer.start();
    }

}

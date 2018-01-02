package tulip.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        MultiServerSocket multiServer = new MultiServerSocket("Bourse", 4000);
        multiServer.start();
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            ClientSocket client = new ClientSocket(localhost, 4000, "Thibaud");
            client.start();
            ClientSocket client2 = new ClientSocket(localhost, 4000, "Yanis");
            client2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

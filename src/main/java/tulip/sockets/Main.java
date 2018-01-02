package tulip.sockets;

public class Main {

    public static void main(String[] args) {
        MultiServer multiServer = new MultiServer(4000);
        multiServer.start();
        Client client = new Client(4000);
        client.start();
        Client client1 = new Client(4000);
        client1.start();

        client.addMessage("{ name : Thibaud }");
        client1.addMessage("{ order : 66 }");
        client.addMessage("{ order : 67 }");
        client.addMessage("{ name : Luc }");
        client.addMessage("Bye");

    }

}

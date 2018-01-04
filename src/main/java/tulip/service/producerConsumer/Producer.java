package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.ClientSocket;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;

public class Producer {

    private final ClientSocket CLIENT_SOCKET;
    private String name;

    private int Ni = 10;
    private Message[] Ti = new Message[Ni];
    private int ini = 0;
    private int outi = 0;
    private int nbmessi = 0;
    private int nbauti = 0;
    private int tempi;
    private final int SEUILi = 3;

    public Producer(String name) {
        this.name = name;
        CLIENT_SOCKET = new ClientSocket(this,"127.0.0.1", 4000);
        CLIENT_SOCKET.start();
        new Facteur().start();
    }

    public void sur_reception_de(Message message) {

        // System.out.println("Producer " + name + " receives: " + message.toJSON());

        if (message.getContentType().equals(ContentType.token)) {
            int tokenValue = Integer.parseInt(message.getContent());
            sur_reception_de_JETON(tokenValue);
        }

    }

    public boolean canProduce() {
        return nbmessi < Ni;
    }

    public void produire(Message message) {

        if (canProduce()) {
            Ti[ini] = message;
            ini = (ini + 1) % Ni;
            nbmessi++;
        } else {
            System.out.println("Buffer overflow");
        }
    }

    public void sur_reception_de_JETON(int val) {
        tempi = Math.min(nbmessi - nbauti, val);
        val -= tempi;
        nbauti += tempi;
        if (val > SEUILi) {
            envoyer_JETON_a_producteur(val);
        } else {
            envoyer_JETON_a_consommateur(val);
        }
    }

    public class Facteur extends Thread {

        @Override
        public void run() {
            while (true) {
                if (nbauti > 0) {
                    CLIENT_SOCKET.sendMessage(Ti[outi]);
                    outi = (outi + 1) % Ni;
                    nbauti--;
                    nbmessi--;
                }
            }
        }
    }

    public void envoyer_JETON_a_producteur(int val) {

        Message message = new Message(Target.nextProducer, ContentType.token, Integer.toString(val));

        CLIENT_SOCKET.sendMessage(message);

        // System.out.println("Producer " + name + " sends TOKEN: " + message.toJSON());
    }

    public void envoyer_JETON_a_consommateur(int val) {

        Message message = new Message(Target.consumer, ContentType.token, Integer.toString(val));

        CLIENT_SOCKET.sendMessage(message);

        // System.out.println("Producer " + name + " sends TOKEN: " + message.toJSON());

    }
}

package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.MultiServerSocket;
import tulip.service.producerConsumer.messages.Message;

public class Consumer {

    private MultiServerSocket multiServerSocket;
    private String name;

    private int N = 10;
    private Message[] T = new Message[N];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbcell = 0;
    private final int SEUIL = 6;
    private boolean present = false;
    private int p = 0;
    private int prochain = 0;

    private boolean tokenStarted = false;
    private Object monitor = new Object();

    public Consumer(String name) {
        this.name = name;
        multiServerSocket = new MultiServerSocket(this,4000);
        multiServerSocket.start();
    }

    public void sur_reception_de(Message message) {

        System.out.println("Consumer " + name + " receives: " + message.toJSON());

        if (message.getContentType().equals(ContentType.token)) {

            if (message.getTarget().equals(Target.nextProducer)) {
                prochain = (prochain + 1) % p;
                multiServerSocket.sendMessageToClient(prochain, message);
            } else {
                int tokenValue = Integer.parseInt(message.getContent());
                sur_reception_de_JETON(tokenValue);
            }

        } else if (message.getContentType().equals(ContentType.app)) {
            sur_reception_de_APP(message);
        }

    }

    public boolean peutConsommer() {
        return nbmess > 0;
    }

    public Message consommer() {

        if (nbmess > 0) {

            Message m = T[out];
            out = (out + 1) % N;
            nbmess--;
            nbcell++;
            if (present && nbcell > SEUIL) {
                envoyer_token_a(prochain, nbcell);
                present = false;
                nbcell = 0;
            }

            return m;
        }

        return null;
    }


    public void sur_reception_de_APP(Message message) {
        System.out.println(message.toJSON());
        T[in] = message;
        in = (in + 1) % N;
        nbmess++;
    }

    public void sur_reception_de_JETON(int val) {
        prochain = (prochain + 1) % p;
        nbcell += val;
        if (nbcell > SEUIL) {
            envoyer_token_a(prochain, nbcell);
            nbcell = 0;
        } else {
            present = true;
        }
    }

    public void envoyer_token_a(int producerNumber, int valJeton) {

        Message message = new Message(Target.consumer, ContentType.token, Integer.toString(valJeton));
        multiServerSocket.sendMessageToClient(producerNumber, message);

        // System.out.println("Consumer " + name + " sends TOKEN: " + message.toJSON());
    }

    public void addProducer() {
        System.out.println("Add producer");
        p++;

        if (!tokenStarted) {
            startToken();
        }
    }

    public void startToken() {
        tokenStarted = true;
        envoyer_token_a(prochain, N);
    }
}

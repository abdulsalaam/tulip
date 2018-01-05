package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.MultiServerSocket;
import tulip.service.producerConsumer.messages.Message;

public class Consumer {

    private final MultiServerSocket MULTI_SERVER_SOCKET;
    private final String NAME;

    private int N = 10;
    private Message[] T = new Message[N];
    private int in = 0;
    private int out = 0;
    private int nbmess = 0;
    private int nbcell = 0;
    private final int SEUIL = 5;
    private boolean present = false;
    private int p = 0;
    private int prochain = 0;

    private boolean tokenStarted = false;
    private final Object monitor = new Object();

    public Consumer(String name) {
        this.NAME = name;
        MULTI_SERVER_SOCKET = new MultiServerSocket(this,4000);
        MULTI_SERVER_SOCKET.start();
    }

    public void sur_reception_de(Message message) {
        System.out.println("Consumer " + NAME + " receives: " + message.toJSON());

        if (p > 0) {

            if (message.getContentType().equals(ContentType.token)) {

                if (message.getTarget().equals(Target.nextProducer)) {
                    passTokenToNextProducer(message);
                } else {
                    int tokenValue = Integer.parseInt(message.getContent());
                    sur_reception_de_JETON(tokenValue);
                }

            } else if (message.getContentType().equals(ContentType.app)) {
                sur_reception_de_APP(message);
            }

        }
    }

    public boolean peutConsommer() {
        synchronized (monitor) {
            return nbmess > 0;
        }
    }

    public Message consommer() {
        synchronized (monitor) {
            if (nbmess > 0) {
                System.out.println("Consommation");
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

            System.out.println("Consommation impossible");
            return null;
        }
    }


    public void sur_reception_de_APP(Message message) {
        synchronized (monitor) {
            System.out.println(message.toJSON());
            T[in] = message;
            in = (in + 1) % N;
            nbmess++;
        }
    }

    private void sur_reception_de_JETON(int val) {
        synchronized (monitor) {
            prochain = (prochain + 1) % p;
            nbcell += val;
            if (nbcell > SEUIL) {
                envoyer_token_a(prochain, nbcell);
                nbcell = 0;
            } else {
                present = true;
            }
        }
    }

    private void envoyer_token_a(int producerNumber, int valJeton) {
        System.out.println("Producer number " + producerNumber);
        Message message = new Message(Target.producer, ContentType.token, Integer.toString(valJeton));
        System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
        MULTI_SERVER_SOCKET.sendMessageToClient(producerNumber, message);
    }

    public void addProducer() {
        synchronized (monitor) {
            System.out.println("Add producer");
            p++;

            if (!tokenStarted) {
                startToken();
            }
        }
    }

    private void startToken() {
        tokenStarted = true;
        envoyer_token_a(prochain, N);
    }

    private void passTokenToNextProducer(Message message) {
        synchronized (monitor) {
            prochain = (prochain + 1) % p;
            MULTI_SERVER_SOCKET.sendMessageToClient(
                    prochain,
                    new Message(Target.producer, ContentType.token, message.getContent())
            );
            System.out.println("Consumer " + NAME + " sends TOKEN: " + message.toJSON());
        }
    }
}

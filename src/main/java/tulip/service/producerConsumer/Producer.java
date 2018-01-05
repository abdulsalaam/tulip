package tulip.service.producerConsumer;

import tulip.service.producerConsumer.messages.Target;
import tulip.service.sockets.ClientSocket;
import tulip.service.producerConsumer.messages.ContentType;
import tulip.service.producerConsumer.messages.Message;

public class Producer extends Thread {

    private final ClientSocket CLIENT_SOCKET;
    private final String NAME;

    private int Ni = 10;
    private Message[] Ti = new Message[Ni];
    private int ini = 0;
    private int outi = 0;
    private int nbmessi = 0;
    private int nbauti = 0;
    private int tempi = 0;
    private final int SEUILi = 4;

    private final Object monitor = new Object();

    public Producer(String name) {
        this.NAME = name;
        CLIENT_SOCKET = new ClientSocket(this,"127.0.0.1", 4000);
        CLIENT_SOCKET.start();
        new Facteur().start();
    }

    private class Facteur extends Thread {

        @Override
        public void run() {
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (nbauti > 0) {
                        envoyer_message(Ti[outi]);
                        outi = (outi + 1) % Ni;
                        nbauti--;
                        nbmessi--;
                    }
                }
            }
        }
    }

    public void sur_reception_de(Message message) {
        synchronized (monitor) {
            System.out.println("Producer " + NAME + " receives: " + message.toJSON());
            if (message.getContentType().equals(ContentType.token)) {
                int tokenValue = Integer.parseInt(message.getContent());
                sur_reception_de_JETON(tokenValue);
            }
        }
    }

    public boolean canProduce() {
        synchronized (monitor) {
            return nbmessi < Ni;
        }
    }

    public void produire(Message message) {
        synchronized (monitor) {
            System.out.println("Produire");
            if (canProduce()) {
                Ti[ini] = message;
                ini = (ini + 1) % Ni;
                nbmessi++;
            } else {
                System.out.println("Buffer overflow");
            }
        }
    }

    private void sur_reception_de_JETON(int val) {
        synchronized (monitor) {
            tempi = Math.min(nbmessi - nbauti, val);
            int value = val;
            value -= tempi;
            nbauti += tempi;
            if (value > SEUILi) {
                envoyer_JETON_a_producteur(value);
            } else {
                envoyer_JETON_a_consommateur(value);
            }
        }
    }

    private void envoyer_JETON_a_producteur(int val) {
        Message message = new Message(Target.nextProducer, ContentType.token, Integer.toString(val));
        CLIENT_SOCKET.sendMessage(message);
        System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
    }

    private void envoyer_JETON_a_consommateur(int val) {
        Message message = new Message(Target.consumer, ContentType.token, Integer.toString(val));
        CLIENT_SOCKET.sendMessage(message);
        System.out.println("Producer " + NAME + " sends TOKEN: " + message.toJSON());
    }

    private void envoyer_message(Message message) {
        System.out.println("Producer " + NAME + " sends MESSAGE: " + message.toJSON());
        CLIENT_SOCKET.sendMessage(message);
    }
}

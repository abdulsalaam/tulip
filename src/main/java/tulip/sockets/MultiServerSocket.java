package tulip.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import tulip.manageOrder.Order;
import tulip.manageStockExchange.Consumer;

/**
 * A MultiServerSocket listens to a port and each time a ClientSocket wants to communicate on this port, it creates
 * a MultiServerSocketThread to handle the communication. Therefore, several MultiServerSocketThread correspond to a
 * unique MultiServerSocket.
 *
 * The MultiServerSocket is also used to orchestrate the token system.
 */
public class MultiServerSocket extends Thread {

    /**
     * The name that identifies the server socket on the network.
     * Please note that the MultiServerSocket and the MultiServerSocketThreads have the same name.
     * */
    private final String SERVER_SOCKET_NAME;

    /** The port the server socket listens to */
    private final int PORT;

    /** Maps the name of each registered client to its corresponding MultiServerSocketThread */
    protected Map<String, MultiServerSocketThread> registeredClients = new ConcurrentHashMap<>();

    /** Indicates whether the token system has been started by sending a the token */
    private boolean tokenStarted = false;

    /** Iterator used in the token system to iterate through the server socket threads corresponding to the registered clients */
    private Iterator<Map.Entry<String, MultiServerSocketThread>> tokenIterator;

    private final Object monitor = new Object();
    
    //added
    protected Consumer CONSUMER ; //initialized in the real type of MultiServerSocket
    protected BlockingQueue<Order> orders ; 

    public MultiServerSocket(String serverSocketName, int port) {
        SERVER_SOCKET_NAME = serverSocketName;
        PORT = port;
        orders = new ArrayBlockingQueue<Order>(100) ; 
    }

    @Override
    public void run() {

        System.out.println("MultiServerSocket starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Creates a new thread each time a new client socket initiates a connection with MultiServerSocket
                new MultiServerSocketThread(this, SERVER_SOCKET_NAME, serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MultiServerSocket quitting");
    }

    /**
     * This methods is used when a client socket sends a registration request.
     * It enables to map the name of the socket client with the MultiServerSocketThread which is used
     * to communicate with it.
     * @param clientName
     * @param thread
     */
    public void registerConnectedClient(String clientName, MultiServerSocketThread thread) {
        synchronized (monitor) {
            registeredClients.put(clientName, thread);
            System.out.println(
                    "MultiServerSocket \"" + SERVER_SOCKET_NAME + "\": registers MultiServerSocketThread \"" + clientName
                            + "\" on thread " + thread.getId()
            );

            resetTokenIterator();
        }

        // Start the token system only once, when the first client registers
        if (!tokenStarted) {
            passToken(10);
            tokenStarted = true;
        }
    }

    
    /**
     * The MultiServerSocket passes the token to the next client on the token system.
     * This method is called by the MultiServerSocketThread which currently has the token.
     * @param tokenValue
     */
    public void passToken(int tokenValue) {
        synchronized (monitor) {

            // If the last client on the token ring is attained, the iterator is resetted in order to send the token
            // to the first client and make a loop
            if (!tokenIterator.hasNext()) {
                resetTokenIterator();
                tokenValue = 10;
            }

            Map.Entry<String, MultiServerSocketThread> entry = tokenIterator.next();
            entry.getValue().sendToken(tokenValue);
        }
    }

    
    /**
     * Resets the token iterator
     */
    public void resetTokenIterator() {
        tokenIterator = registeredClients.entrySet().iterator();
    }
    
    
    
    
    
    //-----------GETTERS & SETTERS ----------------------
    public MultiServerSocket getServer() {
    		return this ; 
    }
    
    public Map<String, MultiServerSocketThread> getRegisteredClients() {
		return registeredClients;
	}

	public void setRegisteredClients(Map<String, MultiServerSocketThread> registeredClients) {
		this.registeredClients = registeredClients;
	}

	public boolean isTokenStarted() {
		return tokenStarted;
	}

	public void setTokenStarted(boolean tokenStarted) {
		this.tokenStarted = tokenStarted;
	}

	public Iterator<Map.Entry<String, MultiServerSocketThread>> getTokenIterator() {
		return tokenIterator;
	}

	public void setTokenIterator(Iterator<Map.Entry<String, MultiServerSocketThread>> tokenIterator) {
		this.tokenIterator = tokenIterator;
	}

	public Consumer getConsumer() {
		return CONSUMER;
	}

	public void setConsumer(Consumer consumer) {
		this.CONSUMER = consumer;
	}

	public BlockingQueue<Order> getOrders() {
		return orders;
	}

	public void setOrders(BlockingQueue<Order> orders) {
		this.orders = orders;
	}

	public String getSERVER_SOCKET_NAME() {
		return SERVER_SOCKET_NAME;
	}

	public int getPORT() {
		return PORT;
	}

	public Object getMonitor() {
		return monitor;
	}

}

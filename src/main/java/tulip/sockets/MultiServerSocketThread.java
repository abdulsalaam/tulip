package tulip.sockets;


import tulip.manageStockExchange.StockExchange;
import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A MultiServerSocketThread handles a connection with a client socket on a specific port.
 * Each MultiServerSocketThread corresponds to a MultiServerSocket.
 */
public class MultiServerSocketThread extends Thread {

    /** The MultiServerSocket behind this MultiServerSocketThread */
    private final MultiServerSocket MULTI_SERVER_SOCKET;

    /**
     * The name that identifies the server socket on the network.
     * Please note that the MultiServerSocket and the MultiServerSocketThreads have the same name.
     * */
    private final String SERVER_SOCKET_NAME;

    /** The name that identifies the client socket the MultiServerSocketThread is communicating with */
    private String clientSocketName;

    /**
     * Indicates whether the client is registered ie. whether the client has sent a registration request to the server
     * socket
     * */
    private boolean isRegisterd = false;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    /**
     * indicate whether the server finished the action submitted by the Thread (dedicated to treat clients requests) or not
     */
    public boolean isActionDone = false ; 
    
    /** 
     * store the reply of the server in a Map with only 2 elements : (replyMessage, messageContent) and (OP_CODE, "status")
     */
    public Map<String, String> replyFromServerWithOPCode ; 
    

    public MultiServerSocketThread(MultiServerSocket multiServerSocket, String serverSocketName, Socket socket) {
        this.MULTI_SERVER_SOCKET = multiServerSocket;
        this.SERVER_SOCKET_NAME = serverSocketName;
        this.socket = socket;
        replyFromServerWithOPCode = new HashMap<String, String>() ; 
    }

    @Override
    public void run() {
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" starting on thread " + this.getId());

        try {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" receives: " + fromClient);
                Message msgFromClient = Message.fromJSON(fromClient);

                if (!isRegisterd) {

                    /**
                     *  If the server detects a registration request from the client
                     */
                    if (msgFromClient.getContentType().equals(ContentType.registrationClientRequest)) {
                        registerClientSocket(msgFromClient.getContent());
                       
                        /**
                         *  Sends a registration acknowledgment to the client of the server (who is actually a client of a Broker)
                         */
                        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.registrationClientAcknowledgement, SERVER_SOCKET_NAME));
                    } 
                    
                    /**
                     *  If the server detects a registration request from the broker
                     */
                    else if ( (msgFromClient.getContentType().equals(ContentType.registrationBrokerRequest))){
                    		registerClientSocket(msgFromClient.getContent());
                    		
                    		/**
                    		 * we assume that the content of the message is under the form "broker's name, number of clients connected to him at that time")
                    		 */
            				String message = msgFromClient.getContent();
            				final String[] splittedMessage = message.split(Pattern.quote(","));
            				
            				/**
            				 * we recover the broker's name and its number of connected clients, then we consequently update the map
            				 */
            				String brokerName = splittedMessage[0] ; 
            				int nbClientsConnected = Integer.parseInt(splittedMessage[1]) ; 
            				
            				/**
            				 * we recover the stockExchange associated to this thread (the MultiServerSocket that launched the current thread)
            				 */
            				StockExchange stockExchange = (StockExchange)MULTI_SERVER_SOCKET ; 
            				stockExchange.addClientToBroker(brokerName, nbClientsConnected);
            				
                        /**
                         * Sends a registration acknowledgment to the client (who is a broker)
                         */
                        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.registrationBrokerAcknowledgement, SERVER_SOCKET_NAME));
                   
                    }
                    
                    else {
                        System.out.println("Error: the client is not registred. Send a registration request first.");
                    }

                } 
                else {
                		/**
                		 *  the other different types of messages that can be received by the server from its clients (the brokers in case of a StockExchange as server)
                		 */
                		switch (msgFromClient.getContentType()) {
                			case token : 
                				int tokenValue = Integer.parseInt(msgFromClient.getContent());
                				MULTI_SERVER_SOCKET.passToken(tokenValue);
                				
                				break ; 
                			
                			case order : 
                				 /**
                				  *  Sends an order acknowledgment to the client (who is a broker)
                				  */
                             send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.orderAcknowledgment, "order received by the server. We will let you know once the order has been treated. "));
                             System.out.println("order message received : begin to parse ") ; 
                				
                				StockExchange stockExchange = (StockExchange)MULTI_SERVER_SOCKET ; 
                				String messageOrderJSON = msgFromClient.toJSON() ; 
            					JSONObject orderDataJSON = null ;
            					Date emissionDate = null ; 
            					int idOrder = -1; 
            					String company = null; 
            					String client = null ; 
            					String broker = null ; 
            					SimpleDateFormat dateFormat = null; 
            					int desiredNbOfStocks = -1 ; 
            					
            					/**
            					 * we initializate the value of this field to -1. 
            					 * If it's a PurchaseOrder, it will be updated with the max value the Client (through his Broker) is willing to pay for this order
            					 */
            					double maxPurchasingPrice = -1 ;
            					
            					/**
            					 * we initializate the value of theses fields to -1. If it's a SellOrder,it will be updated with the min value the Client (through his Broker) is willing to sell for this order
            					 */
            					double minSellingPrice = -1 ; 
            					
                				try {
                					orderDataJSON = new JSONObject(messageOrderJSON);
                				
                					idOrder = Integer.parseInt((String) orderDataJSON.get("id"));
                					company = (String) orderDataJSON.get("company") ; 
                					client = (String) orderDataJSON.get("client") ; 
                					broker = (String) orderDataJSON.get("broker") ;
                				
                					dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							
                					try {
                						emissionDate = dateFormat.parse( (String) orderDataJSON.get("emissionDate") ) ;
                					} catch (ParseException e) {
                						e.printStackTrace();
                					}
                					
                					desiredNbOfStocks = Integer.parseInt((String) orderDataJSON.get("desiredNbOfStocks"));
                					
                				}catch(JSONException e) {
                					System.out.println("EXCEPTION OCCURED DURING THE PARSING OF THE COMMON PART OF THE ORDER OBJECT");
                					e.printStackTrace();
                				}
                				
                				try {
    								maxPurchasingPrice = Double.parseDouble((String) orderDataJSON.get("maxPurchasingPrice") ) ;
                				} catch (JSONException e) {
    								try {
    									System.out.println("It's not a PurchaseOrder, so it's a SellOrder, we can recover its fields ");
    									minSellingPrice = Double.parseDouble((String) orderDataJSON.get("minSellingPrice") ) ;
    								}catch(JSONException ex) {
    									System.out.println("EXCEPTION OCCURED DURING THE PARSING OF THE SELLORDER OBEJCT ");
    								}
    							}
                				
                				
                				/**
                				 * we have all the fields to create the Order Object and adding it to the queue. It's the stockExchange thread that will treat it. 
                				 * if this condition is verified, it means that the the order we parsed is a PurchaseOrder
                				 */
                				if (maxPurchasingPrice != -1) {
                					System.out.println("it's a PurchaseOrder ! "); 		
                					/**
                					 * we call a method that will add this order to the queue of the stock exchange's orders
                					 */
                					stockExchange.placePurchaseOrder (idOrder, client, broker, company, emissionDate, desiredNbOfStocks, maxPurchasingPrice, clientSocketName) ;
                				}
                				
                				/**
                				 * otherwise, it means that the the order we parsed is a SellOrder
                				 */
                				else {
                					System.out.println("it's a SellOrder ! ");
                					
                					/**
                					 * we call a method that will add this order to the queue of the stock exchange's orders
                					 */
                					stockExchange.placeSellOrder (idOrder, client, broker, company, emissionDate, desiredNbOfStocks, minSellingPrice, clientSocketName) ; 
                					
                				}
                				
                				/**
                				 * we wait for the treatment of the message by the server that will send us the data of the order (in JSON format) in order to create a Message object and forward it to the broker 
                				 */
                				sleepUntilServerACK() ; 
                				
                				/**
                				 * time to send the reply to the clientSocket (the broker)
                				 */
                				
                				/**
                				 * if OP_CODE = true so we got an agreement with the submitted order
                				 */
                				if (replyFromServerWithOPCode.get("OP_CODE").equals("true")) {
                					send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.agreementAcknowledgment, replyFromServerWithOPCode.get("replyMessage") ));
                                    
                				}
                				
                				/**
                				 * OP_CODE = false so we didn't get an agreement with the submitted order
                				 */
                				else if (replyFromServerWithOPCode.get("OP_CODE").equals("false")) {
                					send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.orderImpossible, replyFromServerWithOPCode.get("replyMessage")));
                                    
                				}
                				
                				/**
                				 * once we replied to the broker (order treated), we finished our work
                				 */
                				break ; 
                				
                				
                			case deconnect : 
                				StockExchange theStockExchange = (StockExchange)MULTI_SERVER_SOCKET ; 
                				
                				/**
                				 *  Sends a registration acknowledgment to the client (who is a broker)
                				 */
                             send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.deconnectAcknowledgment, SERVER_SOCKET_NAME));
                             
                             /**
                              * we call the action to get the market state from the server
                              */
                             theStockExchange.sendMarketStateClientThread(this) ; 
                             
                             /**
                              * we recover the JSON (in String format) of the state market under the key "replyMessage" of the map
                              * we then create a message filled with this content and we send it to our client (Broker that asked for logout)
                              */
                				String stateMarket = replyFromServerWithOPCode.get("replyMessage") ; 
                				send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.stateMarket, stateMarket ));
                           
                				/**
                				 * we assume that the broker send a disconnection message by simply sending its name and that the removing 
                				 * is done by deleting the entry related to him in the stockExchange's dedicated map 
                				 */
                				String nameBroker = msgFromClient.getContent() ; 
                				theStockExchange.removeBroker(nameBroker) ; 
                				
                				/**
                				 * close the registration of the client
                				 */
                				isRegisterd = false ; 
                			  break ; 	
                			
                			default :
                				break ; 
                				
                		}
                }
            }
     
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" stopping");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a message to the client socket through the socket output stream.
     * Takes care of converting the message to JSON first.
     * @param message The message you want to send
     */
    private void send(Message message) {
        String json = message.toJSON();
        out.println(json);
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\" sends: " + json);
    }

    /**
     * When a client socket registers (ie. when a registration request is received), this methods is used to take
     * into account the registration.
     * @param clientSocketName The name of the client socket being registered
     */
    private void registerClientSocket(String clientSocketName) {
        this.clientSocketName = clientSocketName;
        this.isRegisterd = true;
        System.out.println("MultiServerSocketThread \"" + SERVER_SOCKET_NAME + "\": registers client socket \"" + clientSocketName + "\"");
        this.MULTI_SERVER_SOCKET.registerConnectedClient(clientSocketName, this);
    }

    /**
     * this message with send to the client a token associated to a value
     * @param tokenValue the value of token to send
     */
    void sendToken(int tokenValue) {
        send(new Message(SERVER_SOCKET_NAME, clientSocketName, ContentType.token, Integer.toString(tokenValue)));
    }
    
    
    /**
     * the thread will pass in this method to enter in a continuous loop to wait the answer of the server
     */
    public void sleepUntilServerACK() {	
    		while (!isActionDone) {
    			try {
    				this.wait();
            } catch (InterruptedException ignore) {
            }
         }
    }
    
    
    
    
    
}

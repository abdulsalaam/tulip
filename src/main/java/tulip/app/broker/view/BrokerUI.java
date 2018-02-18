package tulip.app.broker.view;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tulip.app.common.view.Util;
import tulip.app.broker.model.Broker;
import tulip.app.common.model.exceptions.RegistrationException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrokerUI extends Application {

    private static Broker broker;

    public static void main(String[] args) {
        BrokerUI.launch("Leonardo", "5000", "127.0.0.1", "4000");
    }

    @Override
    public void init() throws Exception {
        super.init();
        List<String> parameters = getParameters().getRaw();

        String name = parameters.get(0);
        int serverSocketPort = Integer.parseInt(parameters.get(1));
        String socketHost = parameters.get(2);
        int socketPort = Integer.parseInt(parameters.get(3));

        try {
            broker = new Broker(name, new ServerSocket(serverSocketPort), new Socket(socketHost, socketPort));
            new Thread(broker).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tulip - Broker");
        primaryStage.setScene(getScene());
        primaryStage.show();
    }

    private Scene getScene() {

        BorderPane borderPane = new BorderPane();

        /* Header */
        TilePane tilePaneHeader = new TilePane();
        tilePaneHeader.setPrefRows(1);
        tilePaneHeader.setPrefColumns(2);
        tilePaneHeader.setPrefTileWidth(450);
        tilePaneHeader.setPrefTileHeight(100);
        tilePaneHeader.setMaxWidth(900);
        tilePaneHeader.getChildren().addAll(getHeaderLabels());
        borderPane.setTop(tilePaneHeader);

        /* Body */
        TilePane tilePaneBody = new TilePane();
        tilePaneBody.setPrefRows(2);
        tilePaneBody.setPrefColumns(3);
        tilePaneBody.setPrefTileWidth(300);
        tilePaneBody.setPrefTileHeight(70);
        tilePaneBody.setMaxWidth(900);
        tilePaneBody.getChildren().addAll(getButtons());
        borderPane.setCenter(tilePaneBody);

        /* Footer */
        TilePane tilePaneFooter = new TilePane();
        tilePaneFooter.setPrefRows(1);
        tilePaneFooter.setPrefColumns(2);
        tilePaneFooter.setPrefTileWidth(450);
        tilePaneFooter.setPrefTileHeight(100);
        tilePaneFooter.setMaxWidth(900);
        tilePaneFooter.getChildren().addAll(getFooterLabels());
        borderPane.setBottom(tilePaneFooter);

        Util.setBackground(borderPane, "/img/tulip-flower.jpg");

        return new Scene(borderPane, 900, 400);
    }

    private List<Label> getHeaderLabels() {
        Label name = new Label("Broker: " + broker.getName());
        return Arrays.asList(name);
    }

    private List<StackPane> getButtons() {

        List<Button> buttons = new ArrayList<>();

        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                broker.requestMarketState();
                Util.showMarketState(broker.getMarketState());
            }
        });

        Button placeOrderBtn = new Button("Process order");
        buttons.add(placeOrderBtn);
        placeOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    broker.placeOrder();
                } catch (IndexOutOfBoundsException e) {
                    Util.warningWindow("Index out of bounds", "No more order to process", "");
                } catch (RegistrationException e) {
                    Util.warningWindow("Registration error", "The broker is not registered", "");
                }

            }
        });

        Button showPendingOrdersBtn = new Button("Show pending orders");
        buttons.add(showPendingOrdersBtn);
        showPendingOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showOrders(broker.getPendingOrders());
            }

        });

        Button showClientsBtn = new Button("My registered clients");
        buttons.add(showClientsBtn);
        showClientsBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showActors("Client", broker.getClients());
            }

        });

        ArrayList<StackPane> stackPanes = new ArrayList<>();
        for (Button button : buttons) {
            button.setPrefWidth(250);
            StackPane sp = new StackPane(button);
            StackPane.setAlignment(sp, Pos.CENTER);
            stackPanes.add(sp);
        }

        return stackPanes;
    }

    private List<Label> getFooterLabels() {

        Label registration = new Label(broker.getIsRegistered() ? "Registered" : "Not registered");
        broker.isRegisteredProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                registration.setText(broker.getIsRegistered() ? "Registered" : "Not registered");
            }
        });


        return Arrays.asList(registration);
    }

}

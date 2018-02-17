package tulip.app.client.view;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.client.model.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class View extends Application {

    private static Stage stage;
    private static Client client;

    public static void main(String [] args) {
        startup("Emma", "127.0.0.1", 5000);
    }

    public static void startup(String name, String socketHost, int socketPort) {
        try {
            client = new Client(name, 100000, new Socket(socketHost, socketPort));
            new Thread(client).start();
            Application.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Tulip - Client");
        stage.setScene(getScene());
        stage.show();
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

        return new Scene(borderPane, 900, 500);
    }

    private List<Label> getHeaderLabels() {

        Label name = new Label("Client: " + client.getNAME());

        Label cash = new Label("Cash: " + client.getCash());
        client.cashProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                cash.setText("Cash: " + client.getCash());
            }
        });

        return Arrays.asList(name, cash);
    }

    private List<StackPane> getButtons() {

        List<Button> buttons = new ArrayList<>();

        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showMarketState(client.getMarketState());
            }
        });

        Button pendingPurchaseOrdersBtn = new Button("My purchase orders");
        buttons.add(pendingPurchaseOrdersBtn);
        pendingPurchaseOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showOrders(client.getPendingPurchaseOrders());
            }
        });

        Button pendingSellOrdersBtn = new Button("My sell orders");
        buttons.add(pendingSellOrdersBtn);
        pendingSellOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showOrders(client.getPendingSellOrders());
            }
        });

        Button placeOrderBtn = new Button("Place an order");
        buttons.add(placeOrderBtn);
        placeOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showOrderPlacement();
            }
        });

        Button closeTheDayBtn = new Button("Close the day");
        buttons.add(closeTheDayBtn);
        closeTheDayBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // client.closeTheDay();
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

        Label registration = new Label(client.getIsRegistered() ? "Registered" : "Not registered");
        client.isRegisteredProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                registration.setText(client.getIsRegistered() ? "Registered" : "Not registered");
            }
        });

        Label broker = new Label("Broker: " + client.getBroker());
        client.brokerProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                broker.setText("Broker: " + client.getBroker());
            }
        });

        return Arrays.asList(registration, broker);
    }
}

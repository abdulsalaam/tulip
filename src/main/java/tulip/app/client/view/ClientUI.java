package tulip.app.client.view;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tulip.app.common.view.Util;
import tulip.app.client.model.Client;
import tulip.app.common.model.exceptions.IllegalOrderException;
import tulip.app.common.model.exceptions.RegistrationException;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientUI extends Application {

    private static Client client;

    @Override
    public void init() throws Exception {
        super.init();
        List<String> parameters = getParameters().getRaw();

        String name = parameters.get(0);
        String socketHost = parameters.get(1);
        int socketPort = Integer.parseInt(parameters.get(2));

        try {
            client = new Client(name, 100000, new Socket(socketHost, socketPort));
            new Thread(client).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tulip - Client");
        primaryStage.setScene(getScene());
        primaryStage.show();
    }

    private Scene getScene() {

        BorderPane borderPane = new BorderPane();

        /* Header */
        borderPane.setTop(Util.getHeading("Client - " + client.getName()));

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
        tilePaneFooter.setPrefColumns(3);
        tilePaneFooter.setPrefTileWidth(300);
        tilePaneFooter.setPrefTileHeight(100);
        tilePaneFooter.setMaxWidth(900);
        tilePaneFooter.getChildren().addAll(getFooterLabels());
        borderPane.setBottom(tilePaneFooter);

        Util.setBackground(borderPane, "/img/leo.jpg");

        return new Scene(borderPane, 900, 400);
    }

    private List<StackPane> getButtons() {

        List<Button> buttons = new ArrayList<>();

        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showMarketState(client.getMarketState());
            }
        });

        Button pendingPurchaseOrdersBtn = new Button("Pending purchase orders");
        buttons.add(pendingPurchaseOrdersBtn);
        pendingPurchaseOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showOrders(client.getPendingPurchaseOrders());
            }
        });

        Button pendingSellOrdersBtn = new Button("Pending sell orders");
        buttons.add(pendingSellOrdersBtn);
        pendingSellOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showOrders(client.getPendingSellOrders());
            }
        });

        Button placeOrderBtn = new Button("Place an order");
        buttons.add(placeOrderBtn);
        placeOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrderPlacement();
            }
        });

        Button closeTheDayBtn = new Button("Close the day");
        buttons.add(closeTheDayBtn);
        closeTheDayBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                client.closeTheDay();
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

    private List<StackPane> getFooterLabels() {

        Label registration = new Label(client.getIsRegistered() ? "Registered" : "Not registered");
        registration.setTextFill(Color.WHITE);
        registration.setFont(Font.font(STYLESHEET_CASPIAN, 15));
        registration.setPadding(new Insets(10));
        client.isRegisteredProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                registration.setText(client.getIsRegistered() ? "Registered" : "Not registered");
            }
        });

        Label broker = new Label("Broker: " + client.getBroker());
        broker.setTextFill(Color.WHITE);
        broker.setFont(Font.font(STYLESHEET_CASPIAN, 15));
        broker.setPadding(new Insets(10));
        client.brokerProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                broker.setText("Broker: " + client.getBroker());
            }
        });

        Label cash = new Label("Cash: " + client.getCash());
        cash.setTextFill(Color.WHITE);
        cash.setFont(Font.font(STYLESHEET_CASPIAN, 15));
        cash.setPadding(new Insets(10));
        client.cashProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                cash.setText("Cash: " + client.getCash());
            }
        });

        StackPane.setAlignment(registration, Pos.CENTER);
        StackPane.setAlignment(broker, Pos.CENTER);
        StackPane.setAlignment(cash, Pos.CENTER);

        return Arrays.asList(
                new StackPane(registration),
                new StackPane(broker),
                new StackPane(cash)
        );
    }

    private static void showOrderPlacement(){

        Stage showOrderPlacement = new Stage();
        showOrderPlacement.setTitle("Place an order");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        @SuppressWarnings("unchecked")
        ComboBox company = new ComboBox(FXCollections.observableArrayList(client.getMarketState().keySet()));

        final TextField nbStock = new TextField();
        nbStock.setPromptText("Number of stocks");

        final TextField price = new TextField();
        price.setPromptText("Price");

        Button purchase = new Button("Purchase");
        purchase.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                checkOrderPlacement(showOrderPlacement, company, nbStock, price);
            }
        });

        Button sell = new Button("Sell");
        sell.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                checkOrderPlacement(showOrderPlacement, company, nbStock, price);
            }
        });

        TilePane tilePane = new TilePane();
        tilePane.setPrefRows(4);
        tilePane.setPrefColumns(2);
        tilePane.setPrefTileWidth(200);
        tilePane.setPrefTileHeight(50);
        tilePane.setMaxWidth(400);
        tilePane.getChildren().addAll(
                new Label("Company:"), company,
                new Label("Number of stocks: "), nbStock,
                new Label("Price: "), price,
                purchase, sell
        );

        root.getChildren().add(tilePane);
        showOrderPlacement.setScene(scene);
        showOrderPlacement.show();
    }

    private static void checkOrderPlacement(Stage showOrderPlacement, ComboBox company, TextField nbStock, TextField price) {
        if ( company.getValue() == null || nbStock.getText().equals("") || price.getText().equals("")) {
            Util.warningWindow("Error", "Please fill all the fields", "");
        } else {
            try {
                client.placePurchaseOrder((String) company.getValue(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
                showOrderPlacement.close();
            } catch (RegistrationException e) {
                Util.warningWindow("Registration error", "The client is not registered", "");
            } catch (IllegalOrderException e) {
                Util.warningWindow("Illegal order", "You do not have enough money available for this operation", "");
            }
        }
    }

}

package tulip.app.client.view;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.MarketState;
import tulip.app.Util;
import tulip.app.client.model.Client;
import tulip.app.exceptions.IllegalOrderException;
import tulip.app.exceptions.RegistrationException;
import tulip.app.order.Order;
import static javafx.scene.paint.Color.ALICEBLUE;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientUI extends Application {

    private static GridPane grid;
    private static List<Button> buttons = new ArrayList<>();
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

        primaryStage.setTitle("Tulip");
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 1000, 500);
        grid = new GridPane();

        // Gridpane
        for (int i = 0; i < 12; i++)
        {
            ColumnConstraints column = new ColumnConstraints(150);
            grid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 15; i++)
        {
            RowConstraints row = new RowConstraints(70);
            grid.getRowConstraints().add(row);
        }

        // Title
        Text title = new Text("Tulip for Clients");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(STYLESHEET_CASPIAN, 50));
        grid.add(title, 3, 0);
        GridPane.setHalignment(title, HPos.CENTER);

        Label name = new Label("    " + client.getNAME());
        name.setStyle("-fx-text-fill: white;");
        name.setFont(Font.font(STYLESHEET_CASPIAN, 20));
        grid.add(name, 0, 0);

        Label registration = new Label("Not registered");
        registration.setStyle("-fx-text-fill: white;");
        registration.setFont(Font.font(STYLESHEET_CASPIAN, 20));
        grid.add(registration, 0, 1);

        client.isRegisteredProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if (client.getIsRegistered()) {
                    registration.setText("Registered");
                } else {
                    registration.setText("Not registered");
                }
            }
        });

        Label cash = new Label("Cash: " + client.getCash());
        cash.setStyle("-fx-text-fill: white;");
        cash.setFont(Font.font(STYLESHEET_CASPIAN, 20));
        grid.add(cash, 0, 2);

        client.cashProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                cash.setText("Cash: " + client.getCash());
            }
        });

        Label broker = new Label("Broker: " + client.getBroker());
        broker.setStyle("-fx-text-fill: white;");
        broker.setFont(Font.font(STYLESHEET_CASPIAN, 20));
        grid.add(broker, 0, 3);

        client.brokerProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                broker.setText("Cash: " + client.getBroker());
            }
        });

        // Buttons
        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        grid.add(requestMarketStateBtn, 1, 1);

        Button pendingPurchaseOrdersBtn = new Button("My purchase orders");
        buttons.add(pendingPurchaseOrdersBtn);
        grid.add(pendingPurchaseOrdersBtn, 1, 3);

        Button pendingSellOrdersBtn = new Button("My sell orders");
        buttons.add(pendingSellOrdersBtn);
        grid.add(pendingSellOrdersBtn, 3, 3);

        Button placeOrderBtn = new Button("Place an order");
        buttons.add(placeOrderBtn);
        grid.add(placeOrderBtn, 3, 1);


        Button closeTheDayBtn = new Button("Close the day");
        buttons.add(closeTheDayBtn);
        grid.add(closeTheDayBtn, 5, 1);

        int index = 0;
        for(Button button : buttons) {
            button.setStyle("-fx-pref-width: 500px;");
            GridPane.setHalignment(button, HPos.CENTER);
        }

        // Actions
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showMarketState(client.getMarketState());
            }
        });

        pendingPurchaseOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrders(client.getPendingPurchaseOrders());
            }
        });

        pendingSellOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrders(client.getPendingSellOrders());
            }
        });

        placeOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrderPlacement();
            }
        });

        closeTheDayBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                client.closeTheDay();
                for(Button button : buttons) {
                    button.setVisible(false);
                }
            }
        });

        Image img = new Image(ClientUI.class.getResourceAsStream("/img/leo.png"));
        BackgroundImage backgroundImage = new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false));
        borderPane.setBackground(new Background(backgroundImage));


        borderPane.setCenter(grid);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public static void showMarketState(MarketState marketState){

        Stage MarketPopUp = new Stage();
        MarketPopUp.setTitle("Current Market State");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setStyle("-fx-background-color: #DBDBDB");

        xAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFill(Color.WHITE);

        XYChart.Series serie = new XYChart.Series();

        for(Map.Entry<String, Double> stock : marketState.entrySet()) {
            serie.getData().add(new XYChart.Data(stock.getKey(), stock.getValue()));
        }


        bc.setStyle(
                "-fx-background-color: #CFCFCF;" +
                "-fx-background-size: cover;");
        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(serie);
        MarketPopUp.setScene(scene);
        MarketPopUp.show();
    }

    // Text fields
    TextField company = new TextField ("Company");
    TextField nbStock = new TextField ("Number of stocks");
    TextField price = new TextField ("Price");

    public static void showOrderPlacement(){

        Stage showOrderPlacement = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        //GridPane
        GridPane gridpane = new GridPane();
        for (int i = 0; i < 5; i++)
        {
            ColumnConstraints column = new ColumnConstraints(150);
            grid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 6; i++)
        {
            RowConstraints row = new RowConstraints(70);
            grid.getRowConstraints().add(row);
        }

        @SuppressWarnings("unchecked")
        ComboBox company = new ComboBox(FXCollections.observableArrayList(client.getMarketState().keySet()));

        final TextField nbStock = new TextField();
        nbStock.setPromptText("Number of stocks");

        final TextField price = new TextField();
        price.setPromptText("Price");

        // Buttons
        Button purchase = new Button("Purchase");

        Button sell = new Button("Sell");

        // Actions
        purchase.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
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
        });

        sell.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if ( company.getValue() == null || nbStock.getText().equals("") || price.getText().equals("")) {
                    Util.warningWindow("Error", "Please fill all the fields", "");
                } else {
                    try {
                        client.placeSellOrder((String) company.getValue(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
                        showOrderPlacement.close();
                    } catch (RegistrationException e) {
                        Util.warningWindow("Registration error", "The client is not registered", "");
                    } catch (IllegalOrderException e) {
                        Util.warningWindow("Illegal order", "You do not have enough stocks available for this operation", "");
                    }
                }
            }
        });

        gridpane.add(new Label("Company:"), 0, 1);
        gridpane.add(company, 0, 2);
        gridpane.add(new Label("Number of stocks: "),0, 3 );
        gridpane.add(nbStock, 0, 4);
        gridpane.add(new Label("Price: "), 0, 5);
        gridpane.add(price, 0, 6);

        gridpane.add(purchase, 3, 3);
        gridpane.add(sell, 3, 5);

        root.getChildren().add(gridpane);
        showOrderPlacement.setScene(scene);
        showOrderPlacement.show();
    }

    public static void showOrders(List<Order> pendingOrders){

        Stage MarketPopUp = new Stage();
        MarketPopUp.setTitle("Pending Orders");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setStyle("-fx-background-color: #CFCFCF");

        xAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFill(Color.WHITE);

        XYChart.Series serie = new XYChart.Series();

        for(Order order : pendingOrders) {
            serie.getData().add(new XYChart.Data(order.getCompany(), order.getDesiredNbOfStocks()));
        }

        bc.setStyle(
                "-fx-background-color: #CFCFCF;" +
                "-fx-background-size: cover;");

        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(serie);
        MarketPopUp.setScene(scene);
        MarketPopUp.show();
    }
}

package tulip.app.client.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.MarketState;
import tulip.app.Util;
import tulip.app.client.model.Client;
import tulip.app.order.Order;
import static javafx.scene.paint.Color.ALICEBLUE;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientUI extends Application {

    private static GridPane grid;
    private static List<Button> buttons = new ArrayList<>();
    private static Client client;

    private static Label cash;
    private static Label connected;

    public static void main(String [] args) {
        try {
            client = new Client("Emma", 3000, new Socket("127.0.0.1", 5000));
            new Thread(client).start();
            Application.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Tulip");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 500);
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

        // Label
        connected = new Label("Not connected");
        connected.setTextFill(ALICEBLUE);
        connected.setFont(Font.font(STYLESHEET_CASPIAN, 15));
        grid.add(connected, 0, 0);

        cash = new Label("Cash: "+ String.valueOf(client.getCash()));
        cash.setTextFill(ALICEBLUE);
        cash.setFont(Font.font(STYLESHEET_CASPIAN, 15));
        grid.add(cash, 0, 1);

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

        Button archivedOrdersBtn = new Button("My archived orders");
        buttons.add(archivedOrdersBtn);
        grid.add(archivedOrdersBtn, 5, 3);

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
                cash.setText("Cash: "+String.valueOf(client.getCash()));
            }
        });

        pendingSellOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrders(client.getPendingSellOrders());
                cash.setText("Cash: "+String.valueOf(client.getCash()));
            }
        });

        archivedOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showOrders(client.getArchivedOrders());
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

        // Style and final set up
        root.setStyle(
                "-fx-background-image: url('leo.png');-fx-background-size: cover");

        root.getChildren().add(grid);

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
        bc.setStyle(
                "-fx-background-color: white;"
        );

        xAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFill(Color.WHITE);

        XYChart.Series serie = new XYChart.Series();

        for(Map.Entry<String, Double> stock : marketState.entrySet()) {
            serie.getData().add(new XYChart.Data(stock.getKey(), stock.getValue()));
        }


        bc.setStyle(
                "-fx-background-image: url('background.png');-fx-background-size: cover");
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

        // Text fields
        final TextField company = new TextField();
        company.setPromptText("Company");

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
                if (company.getText().equals("") || nbStock.getText().equals("") || price.getText().equals("")) {
                    Util.warningWindow("Error", "Please fill all the fields", "");
                } else {
                    client.placePurchaseOrder(company.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
                    showOrderPlacement.close();
                }
            }
        });

        sell.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (company.getText().equals("") || nbStock.getText().equals("") || price.getText().equals("")) {
                    Util.warningWindow("Error", "Please fill all the fields", "");
                } else {
                    client.placeSellOrder(company.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
                    showOrderPlacement.close();
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
        bc.setStyle(
                "-fx-background-color: white;"
        );

        xAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFill(Color.WHITE);

        XYChart.Series serie = new XYChart.Series();

        for(Order order : pendingOrders) {
            serie.getData().add(new XYChart.Data(order.getCompany(), order.getDesiredNbOfStocks()));
        }


        bc.setStyle(
                "-fx-background-image: url('background.png');-fx-background-size: cover");
        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(serie);
        MarketPopUp.setScene(scene);
        MarketPopUp.show();
    }

    public static void setCashAmount(double amount) {
        cash.setText(Double.toString(amount));
    }

    public static void setConnectedText(String text) {
        connected.setText(text);
    }
}

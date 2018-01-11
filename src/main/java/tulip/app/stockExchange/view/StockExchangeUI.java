package tulip.app.stockExchange.view;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.MarketState;
import tulip.app.client.model.Client;
import tulip.app.order.Order;
import tulip.app.stockExchange.model.StockExchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StockExchangeUI extends Application {

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static StockExchange stockExchange;

    public static void main(String [] args) throws IOException {
        stockExchange = new StockExchange(new ServerSocket(4000));
        stockExchange.start();
        Application.launch();
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
        Text title = new Text("Tulip for the Stock Exchange");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(STYLESHEET_CASPIAN, 50));
        grid.add(title, 3, 0);
        GridPane.setHalignment(title, HPos.CENTER);

        // Buttons
        Button requestMarketStateBtn = new Button("Show market state");
        buttons.add(requestMarketStateBtn);
        grid.add(requestMarketStateBtn, 3, 3 );

        Button addCompanyBtn = new Button("Add a company");
        buttons.add(addCompanyBtn);
        grid.add(addCompanyBtn, 5, 1);

        Button demandBtn = new Button("Current demand");
        buttons.add(demandBtn);
        grid.add(demandBtn, 1, 1);

        Button supplyBtn = new Button("Current supply");
        buttons.add(supplyBtn);
        grid.add(supplyBtn, 3, 1);

        Button showBrokersBtn = new Button("My registered clients");
        buttons.add(showBrokersBtn);
        grid.add(showBrokersBtn, 1, 3);


        int index = 0;
        for(Button button : buttons) {
            button.setStyle("-fx-pref-width: 500px;");
            GridPane.setHalignment(button, HPos.CENTER);
        }

        // Actions
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showMarketState(stockExchange.getMarketState());
            }
        });

        addCompanyBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCompanyPlacement();
            }
        });

        demandBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPendingOrders(stockExchange.getCurrentDemand());
            }
        });
        supplyBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPendingOrders(stockExchange.getCurrentSupply());
            }
        });

        showBrokersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showBrokers(stockExchange.getBrokers());
            }

        });

        // Style and final set up
        root.setStyle(
                "-fx-background-image: url('city.jpg');-fx-background-size: cover");

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

    public static void showCompanyPlacement() {

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
        for (int i = 0; i < 5; i++)
        {
            RowConstraints row = new RowConstraints(70);
            grid.getRowConstraints().add(row);
        }

        // Text fields
        final TextField companyName = new TextField();
        companyName.setPromptText("Company name");

        final TextField nbEmittedStocks = new TextField();
        nbEmittedStocks.setPromptText("Number of emitted stocks");

        final TextField initialStockPrice = new TextField();
        initialStockPrice.setPromptText("Initial stock price");

        // Buttons
        Button addBtn = new Button("Add");


        // Actions
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stockExchange.addCompany(companyName.getText(), Integer.parseInt(nbEmittedStocks.getText()), Integer.parseInt(initialStockPrice.getText()));
            }
        });

        gridpane.add(companyName, 0, 1);
        gridpane.add(nbEmittedStocks, 0, 2);
        gridpane.add(initialStockPrice, 0, 3);
        gridpane.add(addBtn, 3, 1);

        root.getChildren().add(gridpane);
        showOrderPlacement.setScene(scene);
        showOrderPlacement.show();
    }

    public static void showPendingOrders(List<Order> pendingOrders){

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

    public static void showBrokers(List<String> brokers){

        Stage showClients = new Stage();
        showClients.setTitle("Registered brokers");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        //GridPane
        GridPane gridpane = new GridPane();
        for (int i = 0; i < 5; i++)
        {
            ColumnConstraints column = new ColumnConstraints(150);
            grid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 5; i++)
        {
            RowConstraints row = new RowConstraints(70);
            grid.getRowConstraints().add(row);
        }
        int index = 0;
        for(String broker : brokers) {
            gridpane.add(new Label(broker), 0, index++);
        }

        root.getChildren().add(gridpane);
        showClients.setScene(scene);
        showClients.show();

    }
}




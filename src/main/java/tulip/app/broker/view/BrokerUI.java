package tulip.app.broker.view;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.MarketState;
import tulip.app.broker.model.Broker;
import tulip.app.order.Order;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BrokerUI extends Application{

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static Broker broker;

    public static void main(String [] args) {
        try {
            broker = new Broker(
                    "Leonardo",
                    new ServerSocket(5000),
                    new Socket("127.0.0.1", 4000)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        broker.start();
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
        Text title = new Text("Tulip for Brokers");
        title.setFill(Color.BLACK);
        title.setFont(Font.font(STYLESHEET_CASPIAN, 50));
        grid.add(title, 3, 0);
        GridPane.setHalignment(title, HPos.CENTER);

        // Label
        Label cash = new Label("Cash: "+ String.valueOf(broker.getCash()));
        grid.add(cash, 0, 0);

        // Buttons
        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        grid.add(requestMarketStateBtn, 1, 1);

        Button placeOrderBtn = new Button("Process order");
        buttons.add(placeOrderBtn);
        grid.add(placeOrderBtn, 3,1);

        Button showPendingOrdersBtn = new Button("Show pending orders");
        buttons.add(requestMarketStateBtn);
        grid.add(showPendingOrdersBtn, 5, 1);

        int index = 0;
        for(Button button : buttons) {
            button.setStyle("-fx-pref-width: 500px;");
            GridPane.setHalignment(button, HPos.CENTER);
        }

        // Actions
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                broker.requestMarketState();
                showMarketState(broker.getMarketState());
            }

        });

        placeOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                broker.placeOrder();
                cash.setText("Cash: "+String.valueOf(broker.getCash()));

            }
        });

        showPendingOrdersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPendingOrders(broker.getPendingOrders());
            }

        });



        // Style and final set up
        root.setStyle(
                "-fx-background-image: url('tulipFlower.jpg');-fx-background-size: cover");

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


}

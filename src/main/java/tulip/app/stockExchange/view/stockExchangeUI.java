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

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class stockExchangeUI extends Application {

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static Client client;

    public static void main(String [] args) {
        client = new Client("Emma", 3000, new Socket());
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
        grid.add(requestMarketStateBtn, 1, 1);

        Button placeOrderBtn = new Button("Add a company");
        buttons.add(placeOrderBtn);
        grid.add(placeOrderBtn, 3, 1);


        Button closeTheDayBtn = new Button("Process transactions");
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
                client.requestMarketState();
                showMarketState(client.getMarketState());
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
        for (int i = 0; i < 5; i++)
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
                client.placePurchaseOrder(company.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
            }
        });

        sell.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                client.placeSellOrder(company.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(price.getText()));
            }
        });


        gridpane.add(company, 0, 1);
        gridpane.add(nbStock, 0, 2);
        gridpane.add(price, 0, 3);

        gridpane.add(purchase, 3, 3);
        gridpane.add(sell, 3, 1);

        root.getChildren().add(gridpane);
        showOrderPlacement.setScene(scene);
        showOrderPlacement.show();
    }



}




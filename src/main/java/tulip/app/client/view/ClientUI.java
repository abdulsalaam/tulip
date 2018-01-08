package tulip.app.client.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.client.model.Client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUI extends Application{

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static Client client;
    private static Map<String, Double> map = new HashMap<>();

    public static void main(String [] args) {
        map.put("Basecamp", 250.0);
        map.put("Tesla", 596.70);
        map.put("Facebook", 450.0);
        map.put("Alphabet", 270.0);
        map.put("Apple", 430.0);
        map.put("Spotify", 220.0);
        map.put("LVMH", 550.0);
        map.put("Ecosia", 120.0);
        map.put("Biocoop", 140.0);
        map.put("Veolia", 245.8);
        map.put("Samsung", 240.0);
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
        Text title = new Text("Tulip for Clients");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(STYLESHEET_CASPIAN, 50));
        grid.add(title, 3, 0);
        GridPane.setHalignment(title, HPos.CENTER);

        // Buttons

        Button requestMarketStateBtn = new Button("Request market state");
        buttons.add(requestMarketStateBtn);
        grid.add(requestMarketStateBtn, 1, 2);


        Button notifyOfTransactionBtn = new Button("Notify of transaction");
        buttons.add(notifyOfTransactionBtn);
        grid.add(notifyOfTransactionBtn, 3, 2);

        int index = 0;
        for(Button button : buttons) {
            button.setStyle("-fx-pref-width: 500px;");
            GridPane.setHalignment(button, HPos.CENTER);
        }


       /* // Actions
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                //client.requestMarketState();
                showMarketState(map);
            }
        });

        notifyOfTransactionBtn.setOnAction(new EventHandler<ActionEvent>() {
           // public void handle(ActionEvent event) { client.notifyOfTransaction();}
        });
*/
        // Style and final set up
        root.setStyle(
                "-fx-background-image: url('pineappleSoft.png');-fx-background-size: cover");

        root.getChildren().add(grid);

        primaryStage.setScene(scene);

        primaryStage.show();

    }
    public static void showMarketState(Map<String, Double> map){

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

            for(Map.Entry<String, Double> stock : map.entrySet()) {
                serie.getData().add(new XYChart.Data(stock.getKey(), stock.getValue()));
            }


            bc.setStyle(
                    "-fx-background-image: url('background.png');-fx-background-size: cover");
            Scene scene  = new Scene(bc,800,600);
            bc.getData().addAll(serie);
            MarketPopUp.setScene(scene);
            MarketPopUp.show();
    }


}

package tulip.app.broker.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
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
import tulip.app.broker.model.Broker;

import java.util.ArrayList;
import java.util.List;

public class BrokerUI extends Application{

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static Broker broker;

    public static void main(String [] args) {
        broker = new Broker("Leonardo");
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

        // Buttons
        Button registerClientBtn = new Button("Register a client");
            buttons.add(registerClientBtn);
            grid.add(registerClientBtn, 1, 1);

        Button requestMarketStateBtn = new Button("Request market state");
            buttons.add(requestMarketStateBtn);
            grid.add(requestMarketStateBtn, 5, 2);

        Button placePurchaseOrderBtn = new Button("Purchase order");
            buttons.add(placePurchaseOrderBtn);
            grid.add(placePurchaseOrderBtn, 3,1);

        Button placeSellOrderBtn = new Button("Sell order");
            grid.add(placeSellOrderBtn, 3,2);
            buttons.add(placeSellOrderBtn);

        Button notifyOfTransactionBtn = new Button("Notify of transaction");
            buttons.add(notifyOfTransactionBtn);
            grid.add(notifyOfTransactionBtn, 1, 2);

        int index = 0;
        for(Button button : buttons) {
            button.setStyle("-fx-pref-width: 500px;");
            GridPane.setHalignment(button, HPos.CENTER);
        }

        // Text fields
        TextField client = new TextField ("Client");
        TextField nbStock = new TextField ("Number of stocks");
        TextField purchasingPrice = new TextField ("Price");

        // Actions
        registerClientBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { broker.registerClient();}
        });

        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { broker.requestMarketState();}
        });

        placePurchaseOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                broker.placePurchaseOrder(
                        client.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(purchasingPrice.getText()
                        ));
            }
        });

        placeSellOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                broker.placeSellOrder(
                        client.getText(), Integer.parseInt(nbStock.getText()), Double.parseDouble(purchasingPrice.getText()
                        ));
            }
        });

        notifyOfTransactionBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { broker.notifyOfTransaction();}
        });


        // Style and final set up
        root.setStyle(
                "-fx-background-color: linear-gradient(CornFlowerBlue, MediumSpringGreen);-fx-background-image: url('tulipFlower.jpg');-fx-background-size: cover");

        grid.add(client, 3, 3);
        grid.add(nbStock, 3, 4);
        grid.add(purchasingPrice, 3, 5);
        root.getChildren().add(grid);

        primaryStage.setScene(scene);

        primaryStage.show();

    }


}

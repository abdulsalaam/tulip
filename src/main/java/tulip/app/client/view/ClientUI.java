package tulip.app.client.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tulip.app.broker.model.Broker;
import tulip.app.client.model.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientUI extends Application{

    private static GridPane grid;
    private List<Button> buttons = new ArrayList<>();
    private static Client client;

    public static void main(String [] args) {
        client = new Client("Emma");
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


        // Actions
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { client.requestMarketState();}
        });

        notifyOfTransactionBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) { client.notifyOfTransaction();}
        });


        // Style and final set up
        root.setStyle(
                "-fx-background-color: linear-gradient(CornFlowerBlue, MediumSpringGreen);-fx-background-image: url('pineappleSoft.png');-fx-background-size: cover");

        root.getChildren().add(grid);

        primaryStage.setScene(scene);

        primaryStage.show();

    }


}

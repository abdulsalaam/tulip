package tulip.manageBroker.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BrokerUI extends Application{

    private static GridPane grid;

    public static void main(String [] args) {
        Application.launch();
    }

    @Override

    public void start(Stage primaryStage) {

        primaryStage.setTitle("Tulip");

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1000, 500);

        grid = new GridPane();

        // Other way to put a background, without CSS
       /* BackgroundImage myBI= new BackgroundImage(new Image("pineappleSoft.png",1000,500,false,true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));*/

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
        title.setFill(Color.FLORALWHITE);
        title.setFont(Font.font(STYLESHEET_CASPIAN, 50));
        grid.add(title, 3, 0);
        GridPane.setHalignment(title, HPos.CENTER);

        List<Button> buttons = new ArrayList<>();

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

        registerClientBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Hello World");
            }
        });

        root.setStyle(
                "-fx-background-color: linear-gradient(CornFlowerBlue, MediumSpringGreen);-fx-background-image: url('pineappleSoft.png');");

        root.getChildren().add(grid);

        primaryStage.setScene(scene);

        primaryStage.show();

    }


}

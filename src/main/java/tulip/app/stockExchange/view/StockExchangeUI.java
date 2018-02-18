package tulip.app.stockExchange.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tulip.app.common.view.Util;
import tulip.app.stockExchange.model.StockExchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockExchangeUI extends Application {

    private static Stage stage;
    private static StockExchange stockExchange;

    public static void main(String[] args) {
        StockExchangeUI.launch("4000");
    }

    @Override
    public void init() throws Exception {
        super.init();
        List<String> parameters = getParameters().getRaw();
        int serverSocketPort = Integer.parseInt(parameters.get(0));

        try {
            stockExchange = new StockExchange(new ServerSocket(serverSocketPort));
            new Thread(stockExchange).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Tulip - Stock Exchange");
        stage.setScene(getScene());
        stage.show();
    }

    private Scene getScene() {

        BorderPane borderPane = new BorderPane();

        /* Header */
        TilePane tilePaneHeader = new TilePane();
        tilePaneHeader.setPrefRows(1);
        tilePaneHeader.setPrefColumns(2);
        tilePaneHeader.setPrefTileWidth(450);
        tilePaneHeader.setPrefTileHeight(100);
        tilePaneHeader.setMaxWidth(900);
        tilePaneHeader.getChildren().addAll(getHeaderLabels());
        borderPane.setTop(tilePaneHeader);

        /* Body */
        TilePane tilePaneBody = new TilePane();
        tilePaneBody.setPrefRows(2);
        tilePaneBody.setPrefColumns(3);
        tilePaneBody.setPrefTileWidth(300);
        tilePaneBody.setPrefTileHeight(70);
        tilePaneBody.setMaxWidth(900);
        tilePaneBody.getChildren().addAll(getButtons());
        borderPane.setCenter(tilePaneBody);

        Util.setBackground(borderPane, "/img/city.jpg");

        return new Scene(borderPane, 900, 400);
    }

    private List<Label> getHeaderLabels() {
        Label name = new Label("Stock exchange");
        return Arrays.asList(name);
    }

    private List<StackPane> getButtons() {

        List<Button> buttons = new ArrayList<>();

        Button requestMarketStateBtn = new Button("Show market state");
        buttons.add(requestMarketStateBtn);
        requestMarketStateBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showMarketState(stockExchange.getMarketState());
            }
        });

        Button addCompanyBtn = new Button("Add a company");
        buttons.add(addCompanyBtn);
        addCompanyBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCompanyPlacement();
            }
        });

        Button demandBtn = new Button("Current demand");
        buttons.add(demandBtn);
        demandBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showOrders(stockExchange.getCurrentDemand());
            }
        });

        Button supplyBtn = new Button("Current supply");
        buttons.add(supplyBtn);
        supplyBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showOrders(stockExchange.getCurrentSupply());
            }
        });

        Button showBrokersBtn = new Button("Registered brokers");
        buttons.add(showBrokersBtn);
        showBrokersBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Util.showActors("Broker", stockExchange.getBrokers());
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

    public static void showCompanyPlacement() {

        Stage showCompanyPlacement = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        final TextField companyName = new TextField();
        companyName.setPromptText("Company name");

        final TextField nbEmittedStocks = new TextField();
        nbEmittedStocks.setPromptText("Number of emitted stocks");

        final TextField initialStockPrice = new TextField();
        initialStockPrice.setPromptText("Initial stock price");

        Button addBtn = new Button("Add");
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (companyName.getText().equals("") || nbEmittedStocks.getText().equals("") || initialStockPrice.getText().equals("")) {
                    Util.warningWindow("Error", "Please fill all the fields", "");
                } else {
                    try {
                        stockExchange.addCompany(companyName.getText(), Integer.parseInt(nbEmittedStocks.getText()), Integer.parseInt(initialStockPrice.getText()));
                        showCompanyPlacement.close();
                    } catch (IllegalArgumentException e) {
                        Util.warningWindow("Company name invalid", "The company you tried to add already exists", "");
                    }
                }
            }
        });

        TilePane tilePane = new TilePane();
        tilePane.setPrefRows(4);
        tilePane.setPrefColumns(2);
        tilePane.setPrefTileWidth(200);
        tilePane.setPrefTileHeight(50);
        tilePane.setMaxWidth(400);
        tilePane.getChildren().addAll(
                new Label("Company:"), companyName,
                new Label("Number of emitted stocks: "), nbEmittedStocks,
                new Label("Initial stock price: "), initialStockPrice,
                addBtn
        );

        root.getChildren().add(tilePane);
        showCompanyPlacement.setScene(scene);
        showCompanyPlacement.show();
    }

}

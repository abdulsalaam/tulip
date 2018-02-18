package tulip.app.common.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tulip.app.common.model.MarketState;
import tulip.app.common.model.order.Order;

import java.util.List;
import java.util.Map;

public class Util {

    public static void warningWindow(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showMarketState(MarketState marketState){

        Stage showMarketState = new Stage();
        showMarketState.setTitle("Current Market State");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> barChart = new BarChart<String,Number>(xAxis,yAxis);

        XYChart.Series serie = new XYChart.Series();

        for (Map.Entry<String, Double> stock : marketState.entrySet()) {
            serie.getData().add(new XYChart.Data(stock.getKey(), stock.getValue()));
        }

        Scene scene  = new Scene(barChart,800,600);
        barChart.getData().addAll(serie);
        showMarketState.setScene(scene);
        showMarketState.show();
    }

    public static void showOrders(List<Order> pendingOrders){

        Stage MarketPopUp = new Stage();
        MarketPopUp.setTitle("Pending Orders");

        TableView tableView = new TableView();
        TableColumn idCol = new TableColumn("Id");
        TableColumn typeCol = new TableColumn("Type");
        TableColumn companyCol = new TableColumn("Company");
        TableColumn dateCol = new TableColumn("Emission Date");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        companyCol.setCellValueFactory(new PropertyValueFactory<>("company"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("emissionDate"));
        tableView.getColumns().addAll(idCol, typeCol, companyCol, dateCol);
        tableView.setItems(FXCollections.observableArrayList(pendingOrders));

        Scene scene  = new Scene(tableView,800,600);
        MarketPopUp.setScene(scene);
        MarketPopUp.show();
    }

    public static void showActors(String actorName, List<String> actors){

        Stage stage = new Stage();
        stage.setTitle(actorName + "s");

        ObservableList<String> list = FXCollections.observableArrayList(actors);

        TableView<String> tableView = new TableView<>();
        TableColumn<String, String> col1 = new TableColumn<>();
        tableView.getColumns().addAll(col1);

        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        tableView.setItems(list);

        Scene scene  = new Scene(tableView,400,400);
        stage.setScene(scene);
        stage.show();

    }

    public static void setBackground(Region region, String imagePath) {

        Image img = new Image(Util.class.getResourceAsStream(imagePath));
        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        false,
                        false,
                        true,
                        true));
        region.setBackground(new Background(backgroundImage));

    }
}

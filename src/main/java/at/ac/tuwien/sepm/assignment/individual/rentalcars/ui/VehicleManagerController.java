package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

public class VehicleManagerController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RentalServiceInterface rentalServiceInterface;
    private ObservableList<DTOVehicle> newDataVehicles;
    private ObservableList<DTOOrder> newDataOrders;

    public VehicleManagerController(RentalServiceInterface rentalServiceInterface) {
        this.rentalServiceInterface = rentalServiceInterface;
    }

    @FXML
    private TableView<DTOVehicle> tableViewVehicles;

    @FXML
    private TableView<DTOOrder> tableViewOrders;

    @FXML
    private TableColumn<DTOVehicle, String> columnLicense, columnName, columnDrivetrain, columnPrice;

    @FXML
    private TableColumn<DTOOrder, String> columnCustomer, columnStart, columnEnd, columnState;

    @FXML
    private TableColumn<DTOOrder, Integer> columnPriceTotal;

    @FXML
    private Button buttonEditVehicle, buttonDeleteVehicle, buttonYesDelete;

    @FXML
    private CheckBox tickBoxEditMode, tickBoxDeleteMode;

    @FXML
    private Label labelException;

    @FXML
    private TextField fieldSeats, fieldPriceMin, fieldPriceMax, fieldName;

    @FXML
    private ComboBox choiceDrivetrain, choiceLicense;

    @FXML
    private DatePicker datePickerStart, datePickerEnd;

    /**
     * Is called from main when the program starts.
     * The method gets all the vehicles and orders which are available for display and sets them in their corresponding
     * TableViews.
     */
    public void fillTable() {
        try {
            newDataVehicles = rentalServiceInterface.populateTableViewVehicles();
            newDataOrders = rentalServiceInterface.populateTableViewOrders();

            setPropertiesTableView();
            tableViewVehicles.setItems(newDataVehicles);
            tableViewOrders.setItems(newDataOrders);
            tableViewOrders.getSortOrder().add(columnStart);

            tickBoxEditMode.setSelected(true);
            tickBoxDeleteMode.setSelected(false);
            buttonDeleteVehicle.setDisable(true);
            LOG.trace("TableView filled with dataVehicles and dataOrders");
        } catch (Exception e) {
            LOG.error("Error while populating table with data!");
            LOG.error(e.getMessage());
        }
    }

    /**
     * Sets which values from the objects go to which column before they are loaded into, otherwise the tables would be blank
     */
    private void setPropertiesTableView() {
        columnStart.setSortType(TableColumn.SortType.DESCENDING);

        columnLicense.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("vehicleName"));
        columnDrivetrain.setCellValueFactory(new PropertyValueFactory<>("drivetrain"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        columnCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        columnStart.setCellValueFactory(new PropertyValueFactory<>("dateOrderedStart"));
        columnEnd.setCellValueFactory(new PropertyValueFactory<>("dateOrderedEnd"));
        columnState.setCellValueFactory(new PropertyValueFactory<>("state"));
        columnPriceTotal.setCellValueFactory(new PropertyValueFactory<>("totalOrder"));
    }

    @FXML
    private void addNewVehicleButtonPressed(){
        LOG.debug("Called addNewVehicleButtonPressed");
        try{
            LOG.debug("Opening addVehicleWindow");
            Stage stage = new Stage();
            stage.setTitle("Add new DTOVehicle");
            stage.centerOnScreen();
            stage.setOnCloseRequest(event -> LOG.debug("addVehicleWindow closed"));

            AddVehicleController addVehicleController = new AddVehicleController(rentalServiceInterface);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/addVehicleWindow.fxml"));
            fxmlLoader.setControllerFactory(param -> param.isInstance(addVehicleController) ? addVehicleController : null);

            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.toFront();
            LOG.debug("addVehicleWindow successfully opened");
            stage.setOnCloseRequest(event -> {
                try {
                    fillTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            LOG.error("Could not load scene AddVehicleWindow");
        }
    }

    @FXML
    private void addOrderButtonPressed() {
        LOG.debug("Called addOrderButtonPressed");
        try {
            LOG.debug("Opening addOrderWindow");
            Stage stage = new Stage();
            stage.setTitle("Add new Order");
            stage.centerOnScreen();
            stage.setOnCloseRequest(event -> LOG.debug("addNewOrderWindow closed"));
            stage.setOnCloseRequest(event -> fillTable());

            AddOrderController addOrderController = new AddOrderController(rentalServiceInterface);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/addOrderWindow.fxml"));
            fxmlLoader.setControllerFactory(param -> param.isInstance(addOrderController) ? addOrderController : null);
            addOrderController.setUp();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.toFront();
            LOG.debug("addOrderWindow successfully opened");
        } catch (IOException i) {
            LOG.error("Could not load scene AddOrderWindow");
            i.printStackTrace();
        }
    }

    @FXML
    private void editVehicleButtonPressed() {
        LOG.debug("Called editVehicleButtonPressed");
        showInfoButtonPressed();
    }

    @FXML
    private void deleteVehicleButtonPressed() {
        LOG.debug("Called deleteVehicleButtonPressed");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ObservableList<DTOVehicle> deletedVehicles = tableViewVehicles.getSelectionModel().getSelectedItems();
        alert.setTitle("Caution! Please confirm deleting those vehicles");
        alert.setHeaderText("Are You sure to delete those vehicles?");
        alert.setContentText(selectedVehiclesToString(deletedVehicles));
        LOG.debug("Alert Window successfully opened");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            rentalServiceInterface.deleteVehicle(deletedVehicles);
            try {
                fillTable();
                LOG.debug("Table updated successfully");
            } catch (Exception e) {
                LOG.error("Error while updating table!");
                LOG.error(e.getMessage());
            }
        } else {
            alert.close();
        }
    }

    @FXML
    private void showInfoButtonPressed() {
        LOG.debug("Called showInfoButtonPressed");
        try {
            LOG.debug("Opening vehicleInfoWindow");
            Stage stage = new Stage();
            stage.setTitle("Vehicle Info");
            stage.centerOnScreen();
            stage.setOnCloseRequest(event -> LOG.debug("vehicleInfoWindow closed"));
            stage.setOnCloseRequest(event -> fillTable());

            EditVehicleController editVehicleController = new EditVehicleController(rentalServiceInterface);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/vehicleInfoWindow.fxml"));
            fxmlLoader.setControllerFactory(param -> param.isInstance(editVehicleController) ? editVehicleController : null);

            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.toFront();
            DTOVehicle vehicle = tableViewVehicles.getSelectionModel().getSelectedItem();
            editVehicleController.setUpInfo(vehicle);
            LOG.debug("vehicleInfoWindow successfully opened");
        } catch (IOException i) {
            LOG.error("Could not load scene vehicleInfoWindow");
            LOG.error(i.getMessage());
        } catch (InvalidArgumentException e) {
            LOG.error("No Vehicle selected!");
        }
    }

    @FXML
    private void tickBoxEditModeSelected() {
        tickBoxDeleteMode.setSelected(false);
        tableViewVehicles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        buttonDeleteVehicle.setDisable(true);
        buttonEditVehicle.setDisable(false);
    }

    @FXML
    private void tickBoxDeleteModeSelected() {
        tickBoxEditMode.setSelected(false);
        tableViewVehicles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        buttonEditVehicle.setDisable(true);
        buttonDeleteVehicle.setDisable(false);
    }

    /**
     * Turns the selected vehicle(s) in ObservableList into readable output, so the user can choose which vehicle he wants to delete
     * @param vehicleList ObservableList<DTOVehicle> with the chosen vehicle objects from the tableViewVehicles
     * @return String with the vehicles which are about to be deleted
     */
    private String selectedVehiclesToString(ObservableList<DTOVehicle> vehicleList) {
        String deletedVehicles = "";
        for (DTOVehicle v: vehicleList
             ) {
            deletedVehicles += v.getVehicleName()+"\n";
        }
        return deletedVehicles;
    }

    @FXML
    private void orderInfoButtonPressed() {
        LOG.debug("Called orderInfoButtonPressed");
        try {
            LOG.debug("Opening orderInfoWindow");
            Stage stage = new Stage();
            stage.setTitle("Order Info");
            stage.centerOnScreen();
            stage.setOnCloseRequest(event -> LOG.debug("orderInfoWindow closed"));

            InfoWindowController infoWindowController = new InfoWindowController(rentalServiceInterface);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/orderInfoWindow.fxml"));
            fxmlLoader.setControllerFactory(param -> param.isInstance(infoWindowController) ? infoWindowController : null);

            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.toFront();
            DTOOrder order = tableViewOrders.getSelectionModel().getSelectedItem();
            infoWindowController.setFieldsOrder(order);
            LOG.debug("orderInfoWindow successfully opened");
        } catch (IOException i) {
            LOG.error("Could not load scene orderInfoWindow");
            LOG.error(i.getMessage());
        } catch (InvalidArgumentException e) {
            LOG.error("No Order selected!");
            LOG.error(e.getMessage());
        }
    }

    @FXML
    private void editOrderButtonPressed() {
        LOG.debug("Called editOrderButtonPressed");
        if (!tableViewOrders.getSelectionModel().getSelectedItem().getState().equals("open")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("This order cant be edited");
            alert.setContentText("This order cant be edited because it is "+tableViewOrders.getSelectionModel().getSelectedItem().getState());
            alert.showAndWait();
            LOG.debug("Order from "+tableViewOrders.getSelectionModel().getSelectedItem().getCustomerName()+" cant be edited because it is "+tableViewOrders.getSelectionModel().getSelectedItem().getState());
            return;
        }
        LocalDate now = LocalDate.now();
        if (LocalDate.parse(tableViewOrders.getSelectionModel().getSelectedItem().getDateOrderedStart().substring(0, 10)).isBefore(now)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("This order cant be edited");
            alert.setContentText("This order cant be edited because it is already in use");
            alert.showAndWait();
            LOG.debug("This order cant be edited because it is already in use");
            return;
        }
        try {
            LOG.debug("Opening editOrderWindow");
            Stage stage = new Stage();
            stage.setTitle("Edit Order");
            stage.centerOnScreen();
            stage.setOnCloseRequest(event -> LOG.debug("editOrderWindow closed"));
            stage.setOnCloseRequest(event -> fillTable());

            AddOrderController addOrderController = new AddOrderController(rentalServiceInterface);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/editOrderWindow.fxml"));
            fxmlLoader.setControllerFactory(param -> param.isInstance(addOrderController) ? addOrderController : null);

            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.toFront();
            addOrderController.editSetUp(tableViewOrders.getSelectionModel().getSelectedItem());
            LOG.debug("editOrderWindow successfully opened");
        } catch (IOException i) {
            LOG.error("Could not load scene editOrderWindow");
            LOG.error(i.getMessage());
        }
    }

    @FXML
    private void finishOrderButtonPressed() {
        try {
            rentalServiceInterface.finishOrder(tableViewOrders.getSelectionModel().getSelectedItem());
            LOG.debug("Order finished successfully");
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            labelException.setText(e.getMessage());
            LOG.debug("This order cant be finished because it hasnt started yet");
        }
    }

    @FXML
    private void cancelOrderButtonPressed() {
        LocalDate now = LocalDate.now();
        LocalDate order = LocalDate.parse(tableViewOrders.getSelectionModel().getSelectedItem().getDateOrderedStart().substring(0, 10));
        String name = tableViewOrders.getSelectionModel().getSelectedItem().getCustomerName();
        String state = tableViewOrders.getSelectionModel().getSelectedItem().getState();
        if (!state.equals("open")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("This order cant be canceled");
            alert.setContentText("This order cant be canceled because it is "+state);
            alert.showAndWait();
            LOG.debug("Order from "+name+" cant be canceledbecause it is "+state);
            return;
        }
            Duration duration = Duration.between(order.atStartOfDay(), now.atStartOfDay());
            long diff = Math.abs(duration.toHours());
        if (diff > 24*7) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cancel Info");
            alert.setHeaderText("Order cancel successful");
            alert.setContentText("Order " + name + " was successfully canceled. No fees will be charged");
            alert.showAndWait();
            deleteOrder(tableViewOrders.getSelectionModel().getSelectedItem());
            fillTable();
        } else if (diff > 72) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("Please consider this");
                alert.setContentText("Cancelling this order will result in a fee of 40 percent of the total price");
                alert.showAndWait();
                tableViewOrders.getSelectionModel().getSelectedItem().setState("canceled");
                int newTotal = (tableViewOrders.getSelectionModel().getSelectedItem().getTotalOrder() * 40)/ 100;
                tableViewOrders.getSelectionModel().getSelectedItem().setTotalOrder(newTotal);
                tableViewOrders.getSelectionModel().getSelectedItem().setDateBilled(LocalDate.now().toString());
                tableViewOrders.getSelectionModel().getSelectedItem().setBillNumber(
                    tableViewOrders.getSelectionModel().getSelectedItem().getCustomerName()+" "+LocalDate.now()
                );
                LOG.debug("Order " + name + " was canceled");
                updateOrder(tableViewOrders.getSelectionModel().getSelectedItem());
                fillTable();
            } else if (72 > diff && diff > 24) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("Please consider this");
                alert.setContentText("Cancelling this order will result in a fee of 75 percent of the total price");
                alert.showAndWait();
                tableViewOrders.getSelectionModel().getSelectedItem().setState("canceled");
                tableViewOrders.getSelectionModel().getSelectedItem().setTotalOrder(tableViewOrders.getSelectionModel().getSelectedItem().getTotalOrder()*75/100);
                tableViewOrders.getSelectionModel().getSelectedItem().setDateBilled(LocalDate.now().toString());
                tableViewOrders.getSelectionModel().getSelectedItem().setBillNumber(
                    tableViewOrders.getSelectionModel().getSelectedItem().getCustomerName()+" "+LocalDate.now()
                );
                LOG.debug("Order " + name + " was canceled");
                updateOrder(tableViewOrders.getSelectionModel().getSelectedItem());
                fillTable();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("Cancelling not possible");
                alert.setContentText("Cancelling this order is not possible and will result in a full payment of the total price");
                alert.showAndWait();
                LOG.debug("Order " + name + " cancelling not possible");
            }
    }

    /**
     * Generates a String Array (length = 8) in which the search arguments chosen by the user are filled in.
     */
    @FXML
    private void searchButtonPressed() {
        ObservableList<DTOVehicle> selectedVehicles = FXCollections.observableArrayList();
        String[] query = new String[8];
        if (choiceLicense.getSelectionModel().getSelectedItem() != null) {
            query[0] = choiceLicense.getSelectionModel().getSelectedItem().toString();
        }
        if (datePickerStart.getValue() != null) {
            query[2] = datePickerStart.getValue().toString();
        }
        if (datePickerEnd.getValue() != null) {
            query[3] = datePickerEnd.getValue().toString();
        }
        if (choiceDrivetrain.getSelectionModel().getSelectedItem() != null) {
            query[4] = choiceDrivetrain.getSelectionModel().getSelectedItem().toString();
        }
        query[1] = fieldName.getText();
        query[5] = fieldSeats.getText();
        query[6] = fieldPriceMin.getText();
        query[7] = fieldPriceMax.getText();
        try {
            selectedVehicles = rentalServiceInterface.searchVehicles(query);
        } catch (InvalidArgumentException e) {
            labelException.setText(e.getMessage());
        }
        tableViewVehicles.setItems(selectedVehicles);
    }

    @FXML
    private void resetSearchButtonPressed() {
        choiceLicense.getSelectionModel().clearSelection();
        choiceDrivetrain.getSelectionModel().clearSelection();
        fieldName.setText("");
        fieldPriceMax.setText("");
        fieldPriceMin.setText("");
        fieldSeats.setText("");
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        fillTable();
    }

    private void updateOrder(DTOOrder order) {
        try {
            rentalServiceInterface.checkOrder(order);
        } catch (InvalidArgumentException e) {
            LOG.error(e.getMessage());
            labelException.setText(e.getMessage());
        }
    }

    private void deleteOrder(DTOOrder order) {
        rentalServiceInterface.deleteOrderCheck(order);
    }
}
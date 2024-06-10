package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AddOrderController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RentalServiceInterface rentalServiceInterface;
    private ObservableList<DTOOrder> order;
    private ObservableList<DTOVehicle> selectedVehicles;
    private ObservableList<DTOLicenseInfo> licenseInfos;
    private List<String> choices = new ArrayList<>();
    private String dateStart;
    private String dateEnd;
    private String dateAdded;
    private String ID;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public AddOrderController(RentalServiceInterface rentalServiceInterface) {
        this.rentalServiceInterface = rentalServiceInterface;
    }


    @FXML
    private Label labelTotalOrder, labelException;

    @FXML
    private Button buttonDeleteVehicle, buttonAddNewOrder, buttonCancelOrder;

    @FXML
    private TextField fieldCustomer, fieldIBAN;

    @FXML
    private ComboBox choiceHourStart, choiceHourEnd, boxPaymentmethod;

    @FXML
    private DatePicker datePickerStart, datePickerEnd;

    @FXML
    private TableView<DTOVehicle> orderTableView;

    @FXML
    private TableColumn<DTOOrder, String> columnLicense, columnName, columnPrice;

    @FXML
    private ObservableList<DTOVehicle> vehicles = FXCollections.observableArrayList();

    @FXML
    private VBox vBoxLicenses;

    /**
     * Takes the vehicle from its Info UI and adds it to the selected order
     * @param ID String type ID of the vehicle
     * @param order DTOOrder object of the selected order
     * @throws InvalidArgumentException is thrown if the checks for the vehicle fail
     */
    public void addVehicleFromInfo(String ID, DTOOrder order) throws InvalidArgumentException {
        LOG.debug("Called addVehicleFromInfo");
        editSetUp(order);
        LOG.debug("Edit set up");
        for (DTOVehicle v : selectedVehicles
            ) {
            if (v.getID().equals(ID)) {
                LOG.error("Vehicle already in order " + order.getCustomerName() + "!");
                throw new InvalidArgumentException("Vehicle already in order " + order.getCustomerName() + "!");
            }
        }
        try {
            checkDates(ID);
        } catch (Exception e) {
            LOG.error("Vehicle not available in this period");
            throw new InvalidArgumentException("Vehicle not available in this period");
        }
        for (DTOVehicle v: vehicles
             ) {
            if (v.getID().equals(ID)) {
                if (rentalServiceInterface.checkIfLicesenceInfoNeeded(v)) {
                    labelException.setText("We need additional license info for vehicle " + v.getVehicleName());
                    try {
                        vBoxLicenses.getChildren().add(createPane(v, null));
                        LOG.debug("New Pane added for vehicle " + v.getVehicleName());
                    } catch (IOException e) {
                        LOG.error("Error while adding pane");
                        LOG.error(e.getMessage());
                    }
                    selectedVehicles.add(v);
                    LOG.debug("Vehicle " + v.getVehicleName() + " added to order " + order.getCustomerName());
                    return;
                } else {
                    selectedVehicles.add(v);
                    LOG.debug("Vehicle " + v.getVehicleName() + " added to order " + order.getCustomerName());
                    return;
                }
            }
        }
    }

    public void checkIfInUse(String ID, DTOOrder order) throws InvalidArgumentException {
        LOG.debug("Called checkIfInUse");
        editSetUp(order);
        LOG.debug("Edit set up");
        for (DTOVehicle v : selectedVehicles
            ) {
            if (v.getID().equals(ID)) {
                LOG.error("Vehicle already in order " + order.getCustomerName() + "!");
                throw new InvalidArgumentException("Vehicle already in order " + order.getCustomerName() + "!");
            }
        }
    }

    public void setUp() {
        selectedVehicles = FXCollections.observableArrayList();
        licenseInfos = FXCollections.observableArrayList();
        try {
            vehicles = rentalServiceInterface.populateTableViewVehicles();
        } catch (Exception e) {
            LOG.error("Could not obtain vehicles for adding order");
            LOG.error(e.getMessage());
        }
        selectedVehiclesToString(vehicles);
    }

    public void editSetUp(DTOOrder dtoOrder){
        buttonAddNewOrder.setText("Save Changes");
        ID = dtoOrder.getID();
        dateAdded = dtoOrder.getDateOrderAdded();
        selectedVehicles = FXCollections.observableArrayList();
        licenseInfos = FXCollections.observableArrayList();
        try {
            vehicles = rentalServiceInterface.populateTableViewVehicles();
        } catch (Exception e) {
            LOG.error("Could not obtain vehicles for adding order");
            LOG.error(e.getMessage());
        }
        selectedVehicles = dtoOrder.getVehicles();
        ObservableList<DTOVehicle> vehi = FXCollections.observableArrayList();
        for (DTOVehicle d: selectedVehicles
             ) {
            for (DTOVehicle v: vehicles
                 ) {
                if (d.getID().equals(v.getID())) {
                    if (!vehi.contains(v)) {
                        vehi.add(v);
                    }
                }
            }
        }
        vehicles.removeAll(vehi);
        selectedVehiclesToString(vehicles);
        fillTable();
        fieldCustomer.setText(dtoOrder.getCustomerName());
        fieldIBAN.setText(dtoOrder.getPaymentNumber());
        boxPaymentmethod.setValue(dtoOrder.getPaymentMethod());
        datePickerStart.setValue(LocalDate.parse(dtoOrder.getDateOrderedStart().substring(0,10)));
        datePickerEnd.setValue(LocalDate.parse(dtoOrder.getDateOrderedEnd().substring(0, 10)));
        choiceHourStart.setValue(dtoOrder.getDateOrderedStart().substring(10,13));
        choiceHourEnd.setValue(dtoOrder.getDateOrderedEnd().substring(10,13));
        labelTotalOrder.setText(""+dtoOrder.getTotalOrder());
        for (DTOVehicle v: dtoOrder.getVehicles()
            ) {
            if (rentalServiceInterface.checkIfLicesenceInfoNeeded(v)) {
                for (DTOLicenseInfo l : dtoOrder.getLicenseInfo()
                    ) {
                    if (v.getID().equals(l.getVehicleID())) {
                        try {
                            vBoxLicenses.getChildren().add(createPane(v, l));
                        } catch (IOException e) {
                            LOG.error("Could not create pane for vehicle " + v.getVehicleName());
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }
        refreshTotal();
        LOG.debug("Order edit setup completed successfully");
    }

    private void fillTable() {
        orderTableView.setItems(selectedVehicles);
        columnLicense.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("vehicleName"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    public int refreshTotal() {
        int total = 0;
        for (DTOVehicle d: selectedVehicles
             ) {
            total += Integer.parseInt(d.getPrice());
        }
        try {
            total *= calcHours();
        } catch (ParseException e) {
            LOG.error("Error while parsing dates!");
        }
        return total;
    }

    public int calcHours() throws ParseException {
        int hours = 0;
        dateStart = datePickerStart.getValue().toString() + " " + choiceHourStart.getSelectionModel().getSelectedItem().toString() + ":00";
        dateEnd = datePickerEnd.getValue().toString() + " " + choiceHourEnd.getSelectionModel().getSelectedItem().toString() + ":00";
        Date dStart = format.parse(dateStart);
        Date dEnd = format.parse(dateEnd);
        long diff = dEnd.getTime() - dStart.getTime();
        hours = (int) diff / (60 * 60 * 1000);
        return hours;
    }

    @FXML
    private void addNewOrderButtonPressed() {
        try {
            checkFields();
            for (DTOVehicle v: selectedVehicles
                 ) {
                checkDates(v.getID());
            }
            labelException.setText("");
            if (checkIfCancelPossible()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("No free cancel possible!");
                alert.setContentText("Cancelling this order will result in fees that have to be paid!");
                alert.showAndWait();
            }
            LOG.debug("called addNewOrderButtonPressed");
            try {
                DTOOrder order = new DTOOrder(
                    ID,
                    "open",
                    fieldCustomer.getText(),
                    boxPaymentmethod.getSelectionModel().getSelectedItem().toString() == null ? "" : boxPaymentmethod.getSelectionModel().getSelectedItem().toString(),
                    fieldIBAN.getText(),
                    datePickerStart.getValue().toString() + "T" + choiceHourStart.getSelectionModel().getSelectedItem().toString().substring(1) + ":00:00",
                    datePickerEnd.getValue().toString() + "T" + choiceHourEnd.getSelectionModel().getSelectedItem().toString().substring(1) + ":00:00",
                    selectedVehicles,
                    generateLicenseInfo(),
                    refreshTotal(),
                    dateAdded,
                    null,
                    null
                );
                System.out.println(selectedVehicles.size());
                rentalServiceInterface.checkOrder(order);
                labelException.setText("");
                Stage stage = (Stage) buttonAddNewOrder.getScene().getWindow();
                stage.fireEvent(
                    new WindowEvent(
                        stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                    )
                );
            } catch (InvalidArgumentException e) {
                labelException.setText(e.getMessage());
            }
        }catch (InvalidArgumentException e) {
            labelException.setText(e.getMessage());
        }
    }

    private boolean checkIfCancelPossible() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateNow = LocalDate.now().toString();
        LocalDate now = LocalDate.parse(dateNow, formatter);
        LocalDate startDate = LocalDate.parse(datePickerStart.getValue().toString());
        Duration duration = Duration.between(now.atStartOfDay(), startDate.atStartOfDay());
        long diff = Math.abs(duration.toHours());
        if (diff > 72) {
            return false;
        }
        return true;
    }

    /**
     * Takes all the DTOVehicle objects from selectedVehicles and checks if a vehicle needs further license info. If this is true for a vehicle
     * the corresponding pane with the info is looked up in the VBox vBoxLicenses and saved in a ObservableList
     * @return ObservableList<DTOLicenseInfo> with all the license info from the vehicles where it is needed to save
     * @throws InvalidArgumentException is thrown if somewhere info is missing
     */
    @FXML
    public ObservableList<DTOLicenseInfo> generateLicenseInfo() throws InvalidArgumentException {
        for (DTOVehicle d : selectedVehicles
            ) {
            if (rentalServiceInterface.checkIfLicesenceInfoNeeded(d)) {
                TextField licenseNumber = (TextField) vBoxLicenses.lookup("#licenseNumber" + d.getID());
                DatePicker licenseDate = (DatePicker) vBoxLicenses.lookup("#date" + d.getID());
                if (!licenseNumber.getText().isEmpty()) {
                    DTOLicenseInfo licenseInfo = new DTOLicenseInfo(
                        d.getID(),
                        licenseNumber.getText(),
                        licenseDate.getValue().toString()
                    );
                    licenseInfos.add(licenseInfo);
                } else {
                    throw new InvalidArgumentException("No License info filled in for vehicle " + d.getVehicleName() + "!");
                }
            }
        }
        return licenseInfos;
    }

    @FXML
    private void deleteVehicleButtonPressed() throws InvalidArgumentException {
        orderTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        if (orderTableView.getSelectionModel().getSelectedItem() == null) {
            throw new InvalidArgumentException("Please select a vehicle to delete");
        }
        DTOVehicle v = orderTableView.getSelectionModel().getSelectedItem();
        selectedVehicles.remove(v);
        choices.add(v.getVehicleName());
        vBoxLicenses.getChildren().remove(v.getID());
        labelTotalOrder.setText(String.valueOf(refreshTotal()));
    }

    /**
     * When triggered, a popup opens and the user selects one of the available vehicles. If one is selected
     * several checks are done and the vehicle is added into selectedVehicles.
     */
    @FXML
    private void selectVehiclesButtonPressed(){
        try {
            checkFields();
            labelException.setText("");
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.getItems().addAll(choices);
        dialog.setTitle("Select Vehicle");
        dialog.setHeaderText("Please select a vehicle for your order");
        dialog.setContentText("Choose your vehicle:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            checkDates(vehicles.get(choices.indexOf(dialog.getSelectedItem())).getID());
            }
            int index = choices.indexOf(dialog.getSelectedItem());
            selectedVehicles.add(vehicles.get(index));
            fillTable();
            if (rentalServiceInterface.checkIfLicesenceInfoNeeded(vehicles.get(index))) {
                try {
                    vBoxLicenses.getChildren().add(createPane(vehicles.get(index), null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                buttonDeleteVehicle.setDisable(false);
                LOG.debug("Pane successfully added into vBoxLicenses");
            }
            labelTotalOrder.setText(String.valueOf(refreshTotal()));
            vehicles.remove(index);
            choices.remove(index);
            dialog.close();
        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            labelException.setText(e1.getMessage());
        }
    }

    private void checkDates(String ID) throws InvalidArgumentException{
        String dateStart = datePickerStart.getValue().toString() + "T" + choiceHourStart.getSelectionModel().getSelectedItem().toString().substring(1) + ":00:00";
        String dateEnd = datePickerEnd.getValue().toString() + "T" + choiceHourEnd.getSelectionModel().getSelectedItem().toString().substring(1) + ":00:00";
        List<String> list = null;
        try {
            list = rentalServiceInterface.checkDatesforVehicle(dateStart, dateEnd);
        } catch (Exception e) {
            LOG.error("Error while checking dates");
            LOG.error(e.getMessage());
        }
        if (list != null) {
            if (!list.contains(ID)) {
                return;
            }
        } else {
            LOG.debug("Vehicle not available in this period");
            throw new InvalidArgumentException("Vehicle not available in this period");
        }
    }

    private void checkFields() throws InvalidArgumentException {
        if (fieldCustomer.getText().isEmpty()) {
            throw new InvalidArgumentException("Please specify a customer name!");
        } else if (boxPaymentmethod.getSelectionModel().getSelectedItem() == null) {
            throw new InvalidArgumentException("Please select a payment method!");
        } else if (fieldIBAN.getText().isEmpty()) {
            throw new InvalidArgumentException("Please specify a "+boxPaymentmethod.getSelectionModel().getSelectedItem().toString());
        } else if (datePickerStart.getValue() == null) {
            throw new InvalidArgumentException("Please specify a start date!");
        } else if (datePickerEnd.getValue() == null) {
            throw new InvalidArgumentException("Please specify an end date!");
        } else if (choiceHourStart.getSelectionModel().getSelectedItem() == null) {
            throw new InvalidArgumentException("Please specify a start hour!");
        } else if (choiceHourEnd.getSelectionModel().getSelectedItem() == null) {
            throw new InvalidArgumentException("Please specify an end hour!");
        }
    }

    private Pane createPane(DTOVehicle v, DTOLicenseInfo licenseInfo) throws IOException {
        Pane pane = new Pane();
        Label text = new Label("The order needs further information about the driver");
        Label vehicleInfo = new Label("For Vehicle "+v.getVehicleName());
        Label licenseNumber = new Label("License Number: ");
        Label licenseDate = new Label("Date of issue: ");
        DatePicker dateLicense = new DatePicker();
        TextField licenseNumberField = new TextField();
        Line line = new Line();

        pane.setId(v.getID());
        pane.setPrefSize(357.0, 150.0);
        text.setLayoutX(14.0);
        text.setLayoutY(14.0);
        vehicleInfo.setLayoutX(14.0);
        vehicleInfo.setLayoutY(39.0);
        licenseNumber.setLayoutX(14.0);
        licenseNumber.setLayoutY(66.0);
        licenseDate.setLayoutX(14.0);
        licenseDate.setLayoutY(104.0);
        dateLicense.setLayoutX(114.0);
        dateLicense.setLayoutY(100.0);
        dateLicense.setId("date"+v.getID());
        licenseNumberField.setLayoutX(114.0);
        licenseNumberField.setLayoutY(62.0);
        licenseNumberField.setId("licenseNumber"+v.getID());
        line.setStartX(-100.0);
        line.setEndX(215.0);
        line.setLayoutX(114.0);
        line.setLayoutY(140.0);
        if (licenseInfo != null) {
            dateLicense.setValue(LocalDate.parse(licenseInfo.getLicenseDate()));
            licenseNumberField.setText(licenseInfo.getLicenseNumber());
        }
        pane.getChildren().addAll(text,vehicleInfo,licenseNumber,licenseDate,dateLicense,licenseNumberField,line);
        return pane;
    }

    @FXML
    private void cancelOrderButtonPressed(javafx.event.ActionEvent event) {
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
    }

    /**
     * Fills the choices List with the vehicle names, which are later available for selection when selecting a vehicle
     * @param vehicleList List with the names of the vehicles which are available for order (isDeleted flag = 'false')
     */
    private void selectedVehiclesToString(ObservableList<DTOVehicle> vehicleList) {
        for (DTOVehicle v: vehicleList
            ) {
            choices.add(v.getVehicleName());
        }
    }
}
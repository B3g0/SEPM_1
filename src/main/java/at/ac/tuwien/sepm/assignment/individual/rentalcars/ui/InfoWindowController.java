package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class InfoWindowController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RentalServiceInterface rentalServiceInterface;

    public InfoWindowController(RentalServiceInterface rentalServiceInterface) {
        this.rentalServiceInterface = rentalServiceInterface;
    }

    @FXML
    private Label labelCustomersName, labelTotalOrder, labelPaymentMethod, labelPaymentNumber, labelDateStart, labelDateEnd, labelState, labelReceipt, labelReceiptNumber, labelBillDate;

    @FXML
    private TableView<DTOVehicle> orderTableView;

    @FXML
    private TableColumn<DTOOrder, String> columnLicense, columnPrice, columnName;

    @FXML
    private VBox vBoxLicenses;

    public void setFieldsOrder(DTOOrder o) throws InvalidArgumentException {
        if (o == null) {
            LOG.error("No Vehicle selected for editing!");
            throw new InvalidArgumentException("No Vehicle selected!");
        }
        labelReceipt.setVisible(false);
        if (!o.getState().equals("open")) {
            labelReceipt.setVisible(true);
            labelReceiptNumber.setText(o.getBillNumber());
            labelBillDate.setText(o.getDateBilled().substring(0, 10));
        }
        labelCustomersName.setText(o.getCustomerName());
        labelPaymentMethod.setText(o.getPaymentMethod());
        labelPaymentMethod.setAlignment(Pos.CENTER_RIGHT);
        labelPaymentNumber.setText(o.getPaymentNumber());
        labelDateStart.setText(o.getDateOrderedStart());
        labelDateEnd.setText(o.getDateOrderedEnd());
        labelState.setText(o.getState());
        labelTotalOrder.setText(""+o.getTotalOrder());
        columnLicense.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("vehicleName"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        orderTableView.setItems(o.getVehicles());
        setPanes(o);
    }

    @FXML
    private void cancelOrderButtonPressed(javafx.event.ActionEvent event) {
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
    }

    private void setPanes(DTOOrder d) {
        for (DTOVehicle v: d.getVehicles()
             ) {
            if (rentalServiceInterface.checkIfLicesenceInfoNeeded(v)) {
                for (DTOLicenseInfo l : d.getLicenseInfo()
                    ) {
                    if (v.getID().equals(l.getVehicleID())) {
                        try {
                            vBoxLicenses.getChildren().add(createPane(l, v.getVehicleName()));
                        } catch (IOException e) {
                            LOG.error("Could not create pane for vehicle " + v.getVehicleName());
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private Pane createPane(DTOLicenseInfo licenseInfo, String name) throws IOException {
        Pane pane = new Pane();
        Label text = new Label("The order needs further information about the driver");
        Label vehicleInfo = new Label("For Vehicle "+name);
        Label licenseNumber = new Label("License Number: ");
        Label licenseDate = new Label("Date of issue: ");
        Label dateLicense = new Label();
        Label licenseNumberField = new Label();
        Line line = new Line();

        pane.setId(licenseInfo.getVehicleID());
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
        dateLicense.setLayoutY(104.0);
        dateLicense.setText(licenseInfo.getLicenseDate());
        licenseNumberField.setLayoutX(114.0);
        licenseNumberField.setLayoutY(66.0);
        licenseNumberField.setText(licenseInfo.getLicenseNumber());
        line.setStartX(-100.0);
        line.setEndX(215.0);
        line.setLayoutX(114.0);
        line.setLayoutY(140.0);
        pane.getChildren().addAll(text,vehicleInfo,licenseNumber,licenseDate,dateLicense,licenseNumberField,line);
        return pane;
    }
}

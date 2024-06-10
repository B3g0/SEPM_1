package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditVehicleController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Image newImage;
    private ObservableList<DTOOrder> orders = FXCollections.observableArrayList();
    private List<String> choices = new ArrayList<>();

    private final RentalServiceInterface rentalServiceInterface;
    private DTOVehicle editedVehicle;

    public EditVehicleController(RentalServiceInterface rentalServiceInterface) {
        this.rentalServiceInterface = rentalServiceInterface;
    }

    @FXML
    private TextField fieldName, fieldProduction, fieldDescription, fieldSeats, fieldRegistration, fieldPower, fieldPrice;

    @FXML
    private ComboBox<String> boxDrivetrainSelection;

    @FXML
    private CheckBox tickBoxA, tickBoxB, tickBoxC, tickBoxNone;

    @FXML
    private Label labelException, labelEdit;
    @FXML
    private ImageView imageViewBox;

    @FXML
    private Button buttonSaveChanges, buttonAddImage, buttonDeleteImage, buttonCancel;

    public void setUpInfo(DTOVehicle vehicle) throws InvalidArgumentException {
        if (vehicle == null) {
            LOG.error("No Vehicle selected for editing!");
            throw new InvalidArgumentException("No Vehicle selected!");
        }
        setFields(vehicle);
        buttonSaveChanges.setDisable(true);
        buttonAddImage.setDisable(true);
        buttonDeleteImage.setDisable(true);
        fieldName.setDisable(true);
        fieldDescription.setDisable(true);
        fieldProduction.setDisable(true);
        fieldSeats.setDisable(true);
        fieldRegistration.setDisable(true);
        fieldPower.setDisable(true);
        fieldPrice.setDisable(true);
        tickBoxA.setDisable(true);
        tickBoxB.setDisable(true);
        tickBoxC.setDisable(true);
        tickBoxNone.setDisable(true);
        boxDrivetrainSelection.setDisable(true);
        labelEdit.setText(vehicle.getDateEdited() == null ? "Never" : vehicle.getDateEdited().substring(0, 10));
        try {
            orders = rentalServiceInterface.populateTableViewOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }
        choices = getOrders();
    }


    @FXML
    private void editVehicleButtonPressed() {
        buttonSaveChanges.setDisable(false);
        buttonAddImage.setDisable(false);
        buttonDeleteImage.setDisable(false);
        fieldName.setDisable(false);
        fieldDescription.setDisable(false);
        fieldProduction.setDisable(false);
        fieldSeats.setDisable(false);
        fieldRegistration.setDisable(false);
        fieldPower.setDisable(false);
        fieldPrice.setDisable(false);
        tickBoxA.setDisable(false);
        tickBoxB.setDisable(false);
        tickBoxC.setDisable(false);
        tickBoxNone.setDisable(false);
        boxDrivetrainSelection.setDisable(false);
    }

    /**
     * When triggered a popup opens with the available orders. When a order is chosen checks are made to assure that
     * this vehicle can be added into the selected order.
     * Some checks are done by this controller class, the checks for the license are done by the AddOrderController class.
     * If more license info is needed then the EditOrderWindow shows up and
     * the user enters the info and saves the changes.
     */
    @FXML
    private void addToOrderButtonPressed() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.getItems().addAll(choices);
        dialog.setTitle("Select Order");
        dialog.setHeaderText("Please select an order for your vehicle");
        dialog.setContentText("Choose your order:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!orders.get(choices.indexOf(dialog.getSelectedItem())).getState().equals("open")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("This vehicle cant be added");
                alert.setContentText("This vehicle cant be added because the order is " + orders.get(choices.indexOf(dialog.getSelectedItem())).getState());
                alert.showAndWait();
                LOG.debug("Vehicle cant be added to order " + orders.get(choices.indexOf(dialog.getSelectedItem())).getCustomerName() + " because it is " + orders.get(choices.indexOf(dialog.getSelectedItem())).getState());
                return;
            }
            LocalDate now = LocalDate.now();
            if (LocalDate.parse(orders.get(choices.indexOf(dialog.getSelectedItem())).getDateOrderedStart().substring(0, 10)).isBefore(now)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("This vehicle cant be added");
                alert.setContentText("This vehicle cant be added because the order is already in use");
                alert.showAndWait();
                LOG.debug("This vehicle cant be added because the order is already in use");
                return;
            }
            try {
                LOG.debug("Opening editOrderWindow");
                Stage stage = new Stage();
                stage.setTitle("Edit Order");
                stage.centerOnScreen();
                stage.setOnCloseRequest(event -> LOG.debug("editOrderWindow closed"));

                AddOrderController addOrderController = new AddOrderController(rentalServiceInterface);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/editOrderWindow.fxml"));
                fxmlLoader.setControllerFactory(param -> param.isInstance(addOrderController) ? addOrderController : null);

                stage.setScene(new Scene(fxmlLoader.load()));
                addOrderController.addVehicleFromInfo(editedVehicle.getID(), orders.get(choices.indexOf(dialog.getSelectedItem())));
                stage.toFront();
                stage.show();
            } catch (IOException i) {
                LOG.error("Could not load scene editOrderWindow");
                LOG.error(i.getMessage());
            } catch (InvalidArgumentException e) {
                labelException.setText(e.getMessage());
            }
        }
    }

    private List<String> getOrders() {
        for (DTOOrder o: orders
            ) {
            choices.add(o.getCustomerName());
        }
        return choices;
    }

    @FXML
    private void deleteVehicleButtonPressed() {
        LOG.debug("Called deleteVehiclesButtonPressed");
        LocalDate now = LocalDate.now();
        String header = "";
        String content = "";
        boolean alertOpen = false;
        for (DTOOrder o : orders
            ) {
            LocalDate order = LocalDate.parse(o.getDateOrderedStart().substring(0,10));
            for (DTOVehicle v : o.getVehicles()
                ) {
                if (o.getVehicles().size() == 1 && o.getState().equals("open")) {
                    alertOpen = true;
                    header = "This vehicle cant be deleted!";
                    content = "Please cancel order " + o.getCustomerName() + " first before deleting vehicle.";
                    LOG.error("Deleting vehicle not possible!");
                } else if (v.getID().equals(editedVehicle.getID()) && o.getState().equals("open") && now.isAfter(order)) {
                    alertOpen = true;
                    header = "This Vehicle cant be deleted!";
                    content = "Please finish order " + o.getCustomerName() + " first before deleting vehicle.";
                    LOG.error("Deleting vehicle not possible!");
                } else if (v.getID().equals(editedVehicle.getID()) && o.getState().equals("open") && now.isBefore(order)) {
                    alertOpen = true;
                    header = "This Vehicle cant be deleted!";
                    content = "Vehicle is in order " + o.getCustomerName() + ". Please delete vehicle from order first.";
                    LOG.error("Deleting vehicle not possible!");
                }
            }
        }
        if (alertOpen) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
            return;
        }
        LOG.debug("Called deleteVehicleButtonPressed");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Caution! Please confirm deleting this vehicle");
        alert.setHeaderText("Are You sure to delete this vehicle?");
        alert.setContentText(editedVehicle.getVehicleName());
        LOG.debug("Alert Window successfully opened");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            ObservableList<DTOVehicle> deleteVehicle = FXCollections.observableArrayList();
            deleteVehicle.add(editedVehicle);
            rentalServiceInterface.deleteVehicle(deleteVehicle);
            Stage stage = (Stage) buttonCancel.getScene().getWindow();
            stage.fireEvent(
                new WindowEvent(
                    stage,
                    WindowEvent.WINDOW_CLOSE_REQUEST
                )
            );
        } else {
            alert.close();
        }
    }

    @FXML
    private void tickBoxAChecked() {
        tickBoxNone.setSelected(false);
        fieldRegistration.setEditable(true);
        LOG.info("Tickbox A has been checked, None unchecked");
    }

    @FXML
    private void tickBoxBChecked() {
        tickBoxNone.setSelected(false);
        fieldRegistration.setEditable(true);
        LOG.info("Tickbox B has been checked, None unchecked");
    }

    @FXML
    private void tickBoxCChecked() {
        tickBoxNone.setSelected(false);
        fieldRegistration.setEditable(true);
        LOG.info("Tickbox C has been checked, None unchecked");
    }

    @FXML
    private void tickBoxNoneChecked() {
        tickBoxA.setSelected(false);
        tickBoxB.setSelected(false);
        tickBoxC.setSelected(false);
        fieldRegistration.setEditable(false);
        fieldRegistration.clear();
        LOG.info("Tickbox None has been checked, A B C unchecked");
    }

    @FXML
    private void buttonAddImagePressed() {
        final FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png")
        );
        File file = filechooser.showOpenDialog(null);
        if (file != null) {
            try {
                newImage = rentalServiceInterface.checkImage(file);
            } catch (InvalidArgumentException i) {
                labelException.setText(i.getMessage());
            }
            imageViewBox.setImage(newImage);
            setFix();
        }
    }

    private void setFix(){
        if(newImage.getHeight() < newImage.getWidth()){
            imageViewBox.setFitHeight(500);
        }else{
            imageViewBox.setFitWidth(500);
        }
        imageViewBox.setPreserveRatio(true);
        imageViewBox.setSmooth(true);
        imageViewBox.setCache(true);
    }

    private String tickBoxStringSetter() {

        if (tickBoxNone.isSelected()) {
            return "No License needed";
        }

        String licenseType = "";

        if (tickBoxA.isSelected()) {
            licenseType += "A";
            if (tickBoxB.isSelected()) {
                licenseType += ", B";
                if (tickBoxC.isSelected()) {
                    licenseType += ", C";
                }
            }
            return licenseType;
        } else if (tickBoxB.isSelected()) {
            licenseType += "B";
            if (tickBoxC.isSelected()) {
                licenseType += ", C";
            }
            return licenseType;
        } else if (tickBoxA.isSelected() && tickBoxC.isSelected()) {
            licenseType += ", C";
            return licenseType;
        } else if (tickBoxC.isSelected()) {
            licenseType += "C";
        }
        return licenseType;
    }

    @FXML
    private void deleteImageButtonPressed() {
        imageViewBox.setImage(null);
        newImage = null;
    }

    @FXML
    private void cancelButtonPressed() {
        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.fireEvent(
            new WindowEvent(
                stage,
                WindowEvent.WINDOW_CLOSE_REQUEST
            )
        );
    }

    @FXML
    private void saveChangesButtonPressed() {
        try {
            checkChanges();
        DTOVehicle vehicle = new DTOVehicle(
            editedVehicle.getID(),
            tickBoxStringSetter(),
            fieldName.getText(),
            fieldProduction.getText(),
            fieldDescription.getText(),
            fieldSeats.getText(),
            fieldRegistration.getText(),
            boxDrivetrainSelection.getSelectionModel().getSelectedItem()==null?"":boxDrivetrainSelection.getSelectionModel().getSelectedItem(),
            fieldPower.getText(),
            fieldPrice.getText(),
            editedVehicle.getDateAdded(),
            null,
            "false",
            newImage
        );
            rentalServiceInterface.checkVehicle(vehicle);
            LOG.debug("Vehicle " + vehicle.getVehicleName() + " updated successfully!");
            labelException.setText("");

            Stage stage = (Stage) buttonSaveChanges.getScene().getWindow();
            stage.fireEvent(
                new WindowEvent(
                    stage,
                    WindowEvent.WINDOW_CLOSE_REQUEST
                )
            );
        } catch (InvalidArgumentException e) {
            LOG.error("Error while updating vehicle "+ editedVehicle.getVehicleName());
            labelException.setText(e.getMessage());
        }
    }

    private void checkChanges() throws InvalidArgumentException {
        Stage stage = new Stage();
        stage.setTitle("Save Changes");
        stage.centerOnScreen();
        stage.setOnCloseRequest(event -> LOG.debug("addOrderWindow closed"));

        AddOrderController addOrderController = new AddOrderController(rentalServiceInterface);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/editOrderWindow.fxml"));
        fxmlLoader.setControllerFactory(param -> param.isInstance(addOrderController) ? addOrderController : null);

        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e) {
            LOG.error("Error while loading scene");
            LOG.error(e.getMessage());
        }

        for (DTOOrder o : orders
            ) {
                addOrderController.checkIfInUse(editedVehicle.getID(), o);
        }
    }

    /**
     * Pre-fills the window with the existing vehicle info for later editing
     * @param v DTOVehicle object with the vehicle info
     * @throws InvalidArgumentException is thrown when no vehicle object is selected
     */
    public void setFields(DTOVehicle v) throws InvalidArgumentException{
        if (v == null) {
            LOG.error("No Vehicle selected for editing!");
            throw new InvalidArgumentException("No Vehicle selected!");
        }
        fieldName.setText(v.getVehicleName());
        fieldDescription.setText(v.getDescription());
        fieldPower.setText(v.getPower());
        fieldPrice.setText(v.getPrice());
        fieldRegistration.setText(v.getRegistration());
        fieldSeats.setText(v.getSeats());
        fieldProduction.setText(v.getProduction());
        boxDrivetrainSelection.setValue(v.getDrivetrain());

        if (v.getLicenseType().contains("A")) {
            tickBoxA.setSelected(true);
        }
        if (v.getLicenseType().contains("B")) {
            tickBoxB.setSelected(true);
        }
        if (v.getLicenseType().contains("C")) {
            tickBoxC.setSelected(true);
        }
        if (v.getLicenseType().contentEquals("No License needed")) {
            tickBoxNone.setSelected(true);
        }
        imageViewBox.setImage(v.getImage());
        editedVehicle = v;
    }
}

package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

public class AddVehicleController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RentalServiceInterface rentalServiceInterface;
    private Image newImage;

    public AddVehicleController(RentalServiceInterface rentalServiceInterface){
        this.rentalServiceInterface = rentalServiceInterface;
    }

    @FXML
    private TextField fieldName, fieldProduction, fieldDescription, fieldSeats, fieldRegistration, fieldPower, fieldPrice;

    @FXML
    private ComboBox<String> boxDrivetrainSelection;

    @FXML
    private CheckBox tickBoxA, tickBoxB, tickBoxC, tickBoxNone;

    @FXML
    private Label labelException;

    @FXML
    private ImageView imageViewBox;

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
            }
            imageViewBox.setImage(newImage);
            setFix();
        }
    }

    /**
     * Adjusts the view of the inserted image in the ImageView imageViewBox
     */
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

    @FXML
    private void buttonAddToDatabasePressed(){
        LOG.debug("called buttonAddToDatabasePressed");

        DTOVehicle v = new DTOVehicle(
            null,
            tickBoxStringSetter(),
            fieldName.getText(),
            fieldProduction.getText(),
            fieldDescription.getText(),
            fieldSeats.getText(),
            fieldRegistration.getText(),
            boxDrivetrainSelection.getSelectionModel().getSelectedItem() == null ? "" : boxDrivetrainSelection.getSelectionModel().getSelectedItem(),
            fieldPower.getText(),
            fieldPrice.getText(),
            LocalDateTime.now().toString(),
            null,
            "false",
            newImage
        );
       try{
           rentalServiceInterface.checkVehicle(v);
           labelException.setText("");
           LOG.debug("DTOVehicle "+fieldName.getText()+" added to database");
    }catch (InvalidArgumentException e){
           labelException.setText(e.getMessage());
       }
    }

    @FXML
    private void cancelButtonPressed(javafx.event.ActionEvent event) {
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
    }

    @FXML
    private void deleteImageButtonPressed() {
        imageViewBox.setImage(null);
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
}

package at.ac.tuwien.sepm.assignment.individual.application;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalService;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.VehicleManagerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class MainApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void start(Stage primaryStage) throws Exception {
        // setup application
        primaryStage.setTitle("Rental Car Service");
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(event -> LOG.debug("Application shutdown initiated"));

        // initiate service and controller
        RentalServiceInterface rentalServiceInterface = new RentalService();
        VehicleManagerController vehicleManagerController = new VehicleManagerController(rentalServiceInterface);

        // prepare fxml loader to inject controller
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/vehicleManager.fxml"));
        fxmlLoader.setControllerFactory(param -> param.isInstance(vehicleManagerController) ? vehicleManagerController : null);
        primaryStage.setScene(new Scene(fxmlLoader.load()));
        vehicleManagerController.fillTable();

        // show application
        primaryStage.show();
        primaryStage.toFront();
        LOG.debug("Application startup complete");
    }

    public static void main(String[] args) {
        LOG.debug("Application starting with arguments={}", (Object) args);
        Application.launch(MainApplication.class, args);
    }

}

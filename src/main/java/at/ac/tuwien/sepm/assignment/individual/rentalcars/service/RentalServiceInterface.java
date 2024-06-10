package at.ac.tuwien.sepm.assignment.individual.rentalcars.service;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOOrder;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import java.io.File;
import java.util.List;

public interface RentalServiceInterface {

    /**
     * Takes an Vehicle object from the UI Controller and checks all fields for validation
     * @param v DTOVehicle object with data from the UI Controller entered by the user
     * @throws InvalidArgumentException is thrown in case a field is not valid
     */
    public void checkVehicle(DTOVehicle v) throws InvalidArgumentException;

    /**
     * Checks an Image File for the needed properites like size storagewise as pixelwise
     * @param image image taken from the UI uploaded by the user
     * @return returns the image into the ImageView displayed to the user
     * @throws InvalidArgumentException is thrown if one of the properties is not met and the user has to choose another picture
     */
    public Image checkImage(File image) throws InvalidArgumentException;

    /**
     * Calls a method from the persistence to populate the ObservableList with Vehicle objects to display them in the TableView
     * @return ObservableList with DTOVehicle objects to fill the TableView
     */
    public ObservableList populateTableViewVehicles();

    /**
     Calls a method from the persistence to populate the ObservableList with Order objects to display them in the TableView
     * @return ObservableList with DTOOrder objects to fill the TableView
     */
    public ObservableList populateTableViewOrders();

    /**
     * Takes the order from the UI Controller and validates all the field filled in by the user
     * @param o DTOOrder Object from the UI Controller class
     * @throws InvalidArgumentException is thrown if one entry does not comply with the requirements
     */
    public void checkOrder(DTOOrder o) throws InvalidArgumentException;

    /**
     * Takes the param deleteVehicles and extracts the ID's into a String Array from the objects which are given to the persistence class
     * @param deleteVehicles ObservableList with all the Vehicle objects which are selected by the user for deleting
     */
    public void deleteVehicle(ObservableList<DTOVehicle> deleteVehicles);

    /**
     * Takes a vehicle object and checks if further license info is needed for the order
     * @param vehicle DTOVehicle object which is given by the UI Controller class chosen from the user
     * @return True if vehicle needs further license info, false otherwise
     */
    public boolean checkIfLicesenceInfoNeeded(DTOVehicle vehicle);

    /**
     * Takes an Order object an extracts the ID which is then given to the persistence
     * @param order DTOOrder object which is selected for deletion
     */
    public void deleteOrderCheck(DTOOrder order);

    /**
     * Takes the parameters and calls a method from the persistence to check if vehicles are available in this period
     * @param dateStart String Date from which the period starts
     * @param dateEnd String Date from to which the period ends
     * @return List which contains all the ID's of the vehicles which are available in this period
     */
    public List checkDatesforVehicle(String dateStart, String dateEnd);

    /**
     * Checks first the fields entered by the user and validtes them for correct entries and calls then a method from the persistence
     * @param query String Array (length = 8) with all the values for the search query
     * @return ObservableList<DTOVehicle> with all vehicles which comply to the search query and are displayed in the TableView
     * @throws InvalidArgumentException is thrown if one field does not comply with the requirements
     */
    public ObservableList<DTOVehicle> searchVehicles(String[] query) throws InvalidArgumentException;

    /**
     * Calls method from persistence to save vehicles so that the finished/canceled order is not affected by changes
     * @param order DTOOrder object of order which should be finished/canceled
     */
    public void finishOrder(DTOOrder order) throws DatabaseException, InvalidArgumentException;
}

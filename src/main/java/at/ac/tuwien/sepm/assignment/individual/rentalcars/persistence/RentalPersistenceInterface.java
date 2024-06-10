package at.ac.tuwien.sepm.assignment.individual.rentalcars.persistence;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOOrder;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import javafx.collections.ObservableList;
import java.util.List;

public interface RentalPersistenceInterface {

    /**
     * Adds a vehicle to the DB
     * @param v DTOVehicle which is processed and added to the DB
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void addVehicle(DTOVehicle v) throws DatabaseException;

    /**
     * Fills an ObservableList<DTOVehicle> with DTOVehicle objects to populate a TableView and displays them
     * @return ObservableList<DTOVehicle> with all vehicles from the DB where the isDeleted flag is 'false'
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public ObservableList fillTableVehicles() throws DatabaseException;

    /**
     * Fills an ObservableList<DTOOrder> with DTOOrder objects to populate a TableView and displays them
     * @return ObservableList<DTOOrder> with all orders (open, canceled and finished) from the DB
     * @throws DatabaseException Fills an ObservableList<DTOVehicle> with DTOVehicle objects to populate a TableView and displays them
     */
    public ObservableList fillTableOrders() throws DatabaseException;

    /**
     * Updates an existing vehile in the DB with the new info from the object in the DB
     * @param v DTOVehicle with all the info to update its entry in the DB
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void updateVehicle(DTOVehicle v) throws DatabaseException;

    /**
     * Changes the isDeleted flag for vehicle in the DB from 'false' to 'true' so it wont appear in the tables, but is still in the receipts of the orders
     * @param vehicleArray String Array with all ID's from the vehicles which should be 'deleted'
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void deleteVehicle(String[] vehicleArray)throws DatabaseException;

    /**
     * Adds an order to the DB
     * @param o DTOOrder which contains also the DTOLicenseInfo for the License information about the vehicles
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void addOrder(DTOOrder o) throws DatabaseException;

    /**
     * Updates an existing order in the DB with the new info from the object in the DB
     * @param o DTOOrder with all the info to update its entry in the DB
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void updateOrder(DTOOrder o) throws DatabaseException;

    /**
     * Deletes an order and its corresponding associations in the BIND-table from the DB
     * @param id String with the ID from the order which should be deleted
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public void deleteOrder(String id) throws DatabaseException;

    /**
     * Checks if a vehicle is in a specific period available for ordering or not
     * @param dateStart String Date from which an order starts (yyyy-MM-dd HH:mm:ss format)
     * @param dateEnd String Date to which an order ends (yyyy-MM-dd HH:mm:ss format)
     * @return List with all the ID's from the vehicles which are available in this specific period
     * @throws DatabaseException is thrown in case the query has errors or the connection fails
     */
    public List checkIfAvailable(String dateStart, String dateEnd) throws DatabaseException;

    /**
     * Searches vehicles which fit to the specific search values given by the String Array
     * @param query String Array (length = 8) with all parameters from which a vehicle can be searched by
     * @return ObservableList<DTOVehicle> which fit to the criteria and are then presented in the tableview as search results
     * @throws DatabaseException is thrown in case the query has error or the conncetion fails
     */
    public ObservableList<DTOVehicle> filterVehicles(String[] query) throws DatabaseException;

    /**
     * Takes the vehicles from order and copies their entries so that the order is not affected by changes
     * @param order DTOOrder object which should be finished/canceled
     */
    public void copyEntriesforFinishing(DTOOrder order) throws DatabaseException;

}

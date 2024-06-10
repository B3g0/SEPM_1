package at.ac.tuwien.sepm.assignment.individual.rentalcars.service;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.persistence.RentalPersistence;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOLicenseInfo;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOOrder;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.IBANValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RentalService extends RentalPersistence implements RentalServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void checkVehicle(DTOVehicle v) throws InvalidArgumentException {

        if (v.getLicenseType().contentEquals("")) {
            LOG.error("Vehicle does not have a license type specified!");
            throw new InvalidArgumentException("Please specify the type of license needed for this vehicle.");
        } else if (v.getVehicleName().isEmpty()) {
            LOG.error("Vehicle does not have a name specified!");
            throw new InvalidArgumentException("Vehicle does not have a name specified!");
        } else if (v.getProduction().isEmpty()) {
            LOG.error("Vehicle does not have a production date specified!");
            throw new InvalidArgumentException("Vehicle does not have a production date specified!");
        } else if (!checkYear(v.getProduction())) {
            throw new InvalidArgumentException("Year not a valid number!");
        } else if (!checkIfNaN(v.getSeats())) {
            LOG.error("Seats are not a positive number!");
            throw new InvalidArgumentException("Seats have to be a positive number!");
        } else if (!v.getLicenseType().contentEquals("No License needed") && v.getRegistration().isEmpty()) {
            LOG.error("Vehicles with A, B or C license have to have a registration specified!");
            throw new InvalidArgumentException("Vehicles with A, B or C license have to have a registration specified!");
        } else if (v.getDrivetrain().contentEquals("")) {
            LOG.error("Vehicle does not have a drivetrain specified!");
            throw new InvalidArgumentException("DTOVehicle does not have a drivetrain specified!");
        } else if (v.getDrivetrain().contentEquals("Combustion Engine") && v.getPower().isEmpty()) {
            LOG.error("Vehicles with a combustion engine must have a power specified!");
            throw new InvalidArgumentException("Vehicles with a combustion engine must have a power specified!");
        } else if (!checkIfNaN(v.getPower()) && v.getDrivetrain().contentEquals("combustion")) {
            LOG.error("Power is not a positive number!");
            throw new InvalidArgumentException("Power has to be a positive number!");
        } else if (v.getPrice().isEmpty()) {
            LOG.error("Vehicle does not have a price specified!");
            throw new InvalidArgumentException("DTOVehicle does not have a price specified!");
        } else if (!checkIfNaN(v.getPrice())) {
            LOG.error("Price is not a positive number!");
            throw new InvalidArgumentException("Price has to be a positive number!");
        } else if (v.getVehicleName().length() > 256 || v.getDescription().length() > 256 || v.getRegistration().length() > 256) {
            LOG.error("One or more text entries exceeded maximum length of 256 characters!");
            throw new InvalidArgumentException("One or more text entries exceeded maximum length of 256 characters!");
        } else {
            addToDB(v);
        }
    }

    /**
     * After the DTOVehicle object has been validated successfully it is added into the DB by calling a method from the persistence
     * @param vehicle DTOVehicle object which is added into the DB
     */
    private void addToDB(DTOVehicle vehicle) {
        if (vehicle.getID() == null) {
            try {
                addVehicle(vehicle);
            } catch (DatabaseException e) {
                LOG.error("Database error");
                LOG.error(e.getMessage());
            }
            LOG.debug("Vehicle added to Database!");
        } else {
            try {
                updateVehicle(vehicle);
            } catch (DatabaseException e) {
                LOG.error("Database error!");
                LOG.error(e.getMessage());
            }
        }
    }

    /**
     * Helper method to check if a number is a number
     * @param number String, because the UI controller hands it over as a String and is parsed adequately
     * @return True if number is a positive number, otherwise a NFE is catched and false returned because then it is not a complete number
     */
    private boolean checkIfNaN(String number) {
        try {
            if (checkIfNotNegative(Integer.parseInt(number))) {
                return true;
            }
        } catch (NumberFormatException n) {
            return false;
        }
        return false;
    }

    /**
     * Checks if a number is negative
     * @param number int type
     * @return True if number is greater or equal to zero, false otherwise
     */
    private boolean checkIfNotNegative(int number) {
        if (number >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a entered year is a valid year by its length, as 5-figure years are not realistic
     * @param year String type
     * @return True if year is of valid length
     */
    private boolean checkYear(String year) {
        if (checkIfNaN(year)) {
            if (year.length() > 4) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Image checkImage(File fileImage) throws InvalidArgumentException {

        if (checkFileSize(fileImage)) {
            LOG.error("Image File too large! Only up to 5MB images are allowed!");
            throw new InvalidArgumentException("Image File too large! Only up to 5MB images are allowed!");
        }
        Image image = new Image(fileImage.toURI().toString());
        if (image.getWidth() < 500 || image.getHeight() < 500) {
            LOG.error("Image too small");
            throw new InvalidArgumentException("Image too small");
        }
        return image;
    }

    /**
     * Helper method to check the file size of given file by splitting it into its sizes
     * @param file File type of file which is checked
     * @return True if file is not larger than 5 megabytes, false otherwise
     */
    private boolean checkFileSize(File file) {

        double bytes = file.length();
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);

        if (megabytes > 5) {
            return true;
        }
        return false;
    }

    @Override
    public ObservableList<DTOVehicle> populateTableViewVehicles() {
        ObservableList<DTOVehicle> data = null;
        try {
            data = fillTableVehicles();
        } catch (DatabaseException e) {
            LOG.error("Error while populating ObservableList with data");
            LOG.error(e.getMessage());
        }
        return data;
    }

    @Override
    public ObservableList<DTOOrder> populateTableViewOrders(){
        ObservableList<DTOOrder> data = null;
        try {
            data = fillTableOrders();
        } catch (DatabaseException e) {
            LOG.error("Error while populating ObservableList with data");
            LOG.error(e.getMessage());
        }
        return data;
    }

    @Override
    public void checkOrder(DTOOrder o) throws InvalidArgumentException {
        if (o.getVehicles().isEmpty()) {
            LOG.error("No vehicles selected, order adding process stopped");
            throw new InvalidArgumentException("No vehicles selected!");
        }
        ObservableList<DTOVehicle> vehicles = o.getVehicles();
        ObservableList<DTOLicenseInfo> licenseInfos = o.getLicenseInfo();
        CreditCardValidator creditCardValidator = new CreditCardValidator();
        IBANValidator ibanValidator = new IBANValidator();
        if (o.getPaymentMethod().contentEquals("IBAN")) {
            if (!ibanValidator.isValid(o.getPaymentNumber())) {
                throw new InvalidArgumentException("The IBAN is not correct");
            }
        } else {
            if (!creditCardValidator.isValid(o.getPaymentNumber())) {
                throw new InvalidArgumentException("The Credit Card Number is not correct");
            }
        }
            for (DTOVehicle vehicle : vehicles
                ) {
                for (DTOLicenseInfo info : licenseInfos
                    ) {
                    if (vehicle.getID().equals(info.getVehicleID()) && (vehicle.getLicenseType().contentEquals("A") || vehicle.getLicenseType().contentEquals("C"))) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String dateNow = LocalDate.now().toString();
                        LocalDate now = LocalDate.parse(dateNow, formatter);
                        LocalDate license = LocalDate.parse(info.getLicenseDate());
                        Duration duration = Duration.between(now.atStartOfDay(), license.atStartOfDay());
                        long diff = Math.abs(duration.toDays());
                        long years = diff / 365;
                        if (years < 3) {
                            throw new InvalidArgumentException("License too young for vehicle " + vehicle.getVehicleName());
                        }
                    }
                }
            }
        try {
            if (o.getDateOrderAdded() == null) {
                addOrder(o);
            } else {
                updateOrder(o);
            }
        } catch (DatabaseException e) {
            LOG.error("Database error");
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteVehicle(ObservableList<DTOVehicle> deleteVehicles) {
        String[] deletedVehiclesID = new String[deleteVehicles.size()];
        for (int i = 0; i < deleteVehicles.size(); i++) {
            deletedVehiclesID[i] = deleteVehicles.get(i).getID();
        }
        try {
            deleteVehicle(deletedVehiclesID);
        } catch (DatabaseException e) {
            LOG.error("Database error");
            LOG.error(e.getMessage());
        }
    }

    @Override
    public boolean checkIfLicesenceInfoNeeded(DTOVehicle vehicle) {
        if (!vehicle.getLicenseType().equals("No License needed")) {
            return true;
        }
        return false;
    }

    @Override
    public void deleteOrderCheck(DTOOrder order) {
        try {
            deleteOrder(order.getID());
        } catch (DatabaseException e) {
            LOG.error("Error while deleting order " + order.getCustomerName());
            LOG.error(e.getMessage());
        }
    }

    @Override
    public List checkDatesforVehicle(String dateStart, String dateEnd){
        List<String> list = new ArrayList<>();
        try {
            list = checkIfAvailable(dateStart, dateEnd);
        } catch (DatabaseException e) {
            LOG.error("Database error");
            LOG.error(e.getMessage());
        }
        return list;
    }

    @Override
    public ObservableList<DTOVehicle> searchVehicles(String[] query) throws InvalidArgumentException {

        if (!query[5].isEmpty() && !checkIfNaN(query[5])) {
            LOG.error("Seats are not a valid number");
            throw new InvalidArgumentException("Seats are not a valid number");
        } else if (!query[6].isEmpty() && !checkIfNaN(query[6])) {
            LOG.error("Price(min) is not a positive number");
            throw new InvalidArgumentException("Price(min) is not a valid number");
        } else if (!query[7].isEmpty() && !checkIfNaN(query[7])) {
            LOG.error("Price(max) is not a valid number");
            throw new InvalidArgumentException("Price(max) is not a valid number");
        }

        ObservableList<DTOVehicle> searchedVehicles = FXCollections.observableArrayList();
        try {
            searchedVehicles = filterVehicles(query);
        } catch (DatabaseException e) {
            LOG.error("Error while filtering vehicles");
            LOG.error(e.getMessage());
        }
        return searchedVehicles;
    }

    @Override
    public void finishOrder(DTOOrder order) throws InvalidArgumentException {
        try {
            LocalDate now = LocalDate.now();
            LocalDate orderDate = LocalDate.parse(order.getDateOrderedStart().substring(0, 10));
        if (orderDate.isBefore(now)) {
            order.setState("finished");
            order.setDateBilled(LocalDate.now().toString());
            order.setBillNumber(
                order.getCustomerName() + " " + LocalDate.now()
            );
            updateOrder(order);
        } else {
            throw new InvalidArgumentException("This order cant be finished because it hasnt started yet");
        }
            copyEntriesforFinishing(order);
        } catch (DatabaseException e) {
            LOG.error("Error while finishing order");
            LOG.error(e.getMessage());
        }
    }
}

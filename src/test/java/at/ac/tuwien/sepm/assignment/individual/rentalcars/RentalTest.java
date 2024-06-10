package at.ac.tuwien.sepm.assignment.individual.rentalcars;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.persistence.RentalPersistence;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.persistence.RentalPersistenceInterface;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalService;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOLicenseInfo;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOOrder;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.junit.After;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RentalTest{

    private final RentalServiceInterface rentalServiceInterface = new RentalService();
    private final RentalPersistenceInterface rentalPersistenceInterface = new RentalPersistence();
    private final RentalPersistence rentalPersistence = new RentalPersistence();
    private static final TestLogger LOG = TestLoggerFactory.getTestLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testVehicleAddWithNoLicense(){

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle1 = new DTOVehicle(
            "1",
            "No License needed",
            "Vehicle 1",
            "1885",
            "",
            "3",
            "",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Try to add to DB, should cause no problems*/
        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(),is(""));
        }
        assertThat(LOG.getLoggingEvents(),is(new ArrayList<>()));

    }

    @Test
    public void testVehicleAddWithLicenseAndMissingValues() {

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle1 = new DTOVehicle(
            "2",
            "A, B",
            "Vehicle 1",
            "1885",
            "",
            "3",
            "",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Try to add to DB with missing registration, shoudl throw Exception*/
        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is("Vehicles with A, B or C license have to have a registration specified!"));
        }

        /*Add registration*/
        vehicle1.setRegistration("VEHI-1");

        /*Try to add to DB, should cause no problems*/
        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(),is(""));
        }
        assertThat(LOG.getLoggingEvents(),is(new ArrayList<>()));
    }

    @Test
    public void testEditVehicle() {

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle1 = new DTOVehicle(
            "3",
            "No License needed",
            "Vehicle 1",
            "2009",
            "",
            "6",
            "",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Try to add to DB, should not throw exception*/
        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(),is(""));
        }

        /*Edit Drivetrain*/
        vehicle1.setDrivetrain("Combustion Engine");

        /*Try to save, should throw Exception*/
        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is("Vehicles with a combustion engine must have a power specified!"));
        }
    }

    @Test
    public void imageHandling() {
        JFXPanel jfxPanel = new JFXPanel();
        DTOVehicle vehicle2 = new DTOVehicle(
            "4",
            "A, B",
            "Vehicle 2",
            "1885",
            "",
            "4",
            "VEHI-1",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        File imageFile = new File("pictures/Picture 1.jpg");
        String fileLocation = imageFile.toURI().toString();
        Image fxImage = new Image(fileLocation);
        vehicle2.setImage(fxImage);

        try {
            rentalServiceInterface.checkVehicle(vehicle2);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is("Image too small"));
        }

        imageFile = new File("pictures/Picture 2.jpg");
        fileLocation = imageFile.toURI().toString();
        fxImage = new Image(fileLocation);
        vehicle2.setImage(fxImage);

        try {
            rentalServiceInterface.checkVehicle(vehicle2);
        } catch (InvalidArgumentException e) {
            assertThat(LOG.getLoggingEvents(), is(new ArrayList<>()));
        }
    }

    @Test
    public void addNewOrderWithIncorrectLicenseInfo() {

        /*Generate lists for Vehicles and License Infos*/
        ObservableList<DTOVehicle> vehicles = FXCollections.observableArrayList();
        ObservableList<DTOLicenseInfo> licenseInfos = FXCollections.observableArrayList();

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle2 = new DTOVehicle(
            "5",
            "A",
            "Vehicle 5",
            "1885",
            "",
            "4",
            "VEHI-1",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Add vehicle into List*/
        vehicles.add(vehicle2);

        /*Check if additional info is needed for vehicle*/
        assertThat(rentalServiceInterface.checkIfLicesenceInfoNeeded(vehicle2), is(true));

        /*Generate license info for Vehicle 5*/
        DTOLicenseInfo info1 = new DTOLicenseInfo(
            "5",
            "123456",
            "2015-05-04"
        );

        /*Add info1 to list*/
        licenseInfos.add(info1);

        /*Generate new DTOOrder object*/
        DTOOrder order1 = new DTOOrder(
            "1",
            "open",
            "Order 1",
            "IBAN",
            "AT022081500000698597",
            "2018-04-01T00:00:00",
            "2018-04-02T00:00:00",
            vehicles,
            licenseInfos,
            120,
            "2018-03-30",
            null,
            null
        );

        /*Try to add, should throw exception*/
        try {
            rentalServiceInterface.checkOrder(order1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is("License too young for vehicle Vehicle 5"));
        }

        /*Correct licenso info in obejct and reresh in list*/
        info1.setLicenseDate("2013-01-01");
        licenseInfos.remove(info1);
        licenseInfos.add(info1);

        /*Renew lisence info in DTOOrder obejct*/
        order1.setLicenseInfo(licenseInfos);

        /*Try to add order to DB, should not throw exception*/
        try {
            rentalServiceInterface.checkOrder(order1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is(""));
        }
    }

    @Test
    public void addNewOrderWithLicenseInfoNeeded() {

        /*Generate lists for Vehicles and License Infos*/
        ObservableList<DTOVehicle> vehicles = FXCollections.observableArrayList();
        ObservableList<DTOLicenseInfo> licenseInfos = FXCollections.observableArrayList();

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle2 = new DTOVehicle(
            "6",
            "A",
            "Vehicle 6",
            "1885",
            "",
            "4",
            "VEHI-1",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Add vehicle into List*/
        vehicles.add(vehicle2);

        /*Check if additional info is needed for vehicle*/
        assertThat(rentalServiceInterface.checkIfLicesenceInfoNeeded(vehicle2), is(true));

        /*Generate license info for Vehicle 5*/
        DTOLicenseInfo info1 = new DTOLicenseInfo(
            "6",
            "123456",
            "2012-05-04"
        );

        /*Add info1 to list*/
        licenseInfos.add(info1);

        /*Generate new DTOOrder object*/
        DTOOrder order1 = new DTOOrder(
            "2",
            "open",
            "Order 2",
            "IBAN",
            "AT022081500000698597",
            "2018-04-01T00:00:00",
            "2018-04-02T00:00:00",
            vehicles,
            licenseInfos,
            120,
            "2018-03-30",
            null,
            null
        );

        /*Try to add order to DB, should not throw exception*/
        try {
            rentalServiceInterface.checkOrder(order1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is(""));
        }

    }

    @Test
    public void testFinishOrder() {
        /*Generate lists for Vehicles and License Infos*/
        ObservableList<DTOVehicle> vehicles = FXCollections.observableArrayList();
        ObservableList<DTOLicenseInfo> licenseInfos = FXCollections.observableArrayList();

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle2 = new DTOVehicle(
            "7",
            "A",
            "Vehicle 7",
            "1885",
            "",
            "4",
            "VEHI-1",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Add vehicle into List*/
        vehicles.add(vehicle2);

        /*Check if additional info is needed for vehicle*/
        assertThat(rentalServiceInterface.checkIfLicesenceInfoNeeded(vehicle2), is(true));

        /*Generate license info for Vehicle 5*/
        DTOLicenseInfo info1 = new DTOLicenseInfo(
            "7",
            "123456",
            "2012-05-04"
        );

        /*Add info1 to list*/
        licenseInfos.add(info1);

        /*Generate new DTOOrder object*/
        DTOOrder order1 = new DTOOrder(
            "3",
            "open",
            "Order 3",
            "IBAN",
            "AT022081500000698597",
            "2018-04-01T00:00:00",
            "2018-04-02T00:00:00",
            vehicles,
            licenseInfos,
            120,
            "2018-03-30",
            null,
            null
        );

        /*Try to add order to DB, should not throw exception*/
        try {
            rentalServiceInterface.checkOrder(order1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is(""));
        }

        /*Try to finish, should not throw exception*/
        try {
            rentalServiceInterface.finishOrder(order1);
        } catch (DatabaseException e) {
            assertThat(e.getMessage(), is(""));
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is(""));
        }
    }

    @Test
    public void testFinishOrderBeforeStarted() {
        /*Generate lists for Vehicles and License Infos*/
        ObservableList<DTOVehicle> vehicles = FXCollections.observableArrayList();
        ObservableList<DTOLicenseInfo> licenseInfos = FXCollections.observableArrayList();

        /*Generate new DTOVehicle object*/
        DTOVehicle vehicle2 = new DTOVehicle(
            "8",
            "A",
            "Vehicle 8",
            "1885",
            "",
            "4",
            "VEHI-8",
            "Operated by human",
            "",
            "5",
            "2018-09-10 18:00:00",
            null,
            "false",
            null
        );

        /*Add vehicle into List*/
        vehicles.add(vehicle2);

        /*Check if additional info is needed for vehicle*/
        assertThat(rentalServiceInterface.checkIfLicesenceInfoNeeded(vehicle2), is(true));

        /*Generate license info for Vehicle 5*/
        DTOLicenseInfo info1 = new DTOLicenseInfo(
            "8",
            "123456",
            "2012-05-04"
        );

        /*Add info1 to list*/
        licenseInfos.add(info1);

        /*Generate new DTOOrder object*/
        DTOOrder order1 = new DTOOrder(
            "4",
            "open",
            "Order 3",
            "IBAN",
            "AT022081500000698597",
            "2018-04-10T00:00:00",
            "2018-04-11T00:00:00",
            vehicles,
            licenseInfos,
            120,
            "2018-03-30",
            null,
            null
        );

        /*Try to add order to DB, should not throw exception*/
        try {
            rentalServiceInterface.checkOrder(order1);
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is(""));
        }

        /*Try to finish, should throw exception*/
        try {
            rentalServiceInterface.finishOrder(order1);
        } catch (DatabaseException e) {
            assertThat(e.getMessage(), is(""));
        } catch (InvalidArgumentException e) {
            assertThat(e.getMessage(), is("This order cant be finished because it hasnt started yet"));
        }
    }

    /*Cleanup after tests finished*/
    @After
    public void clear() {
        TestLoggerFactory.clear();
        try {
            clearDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /*Cleanup of the DB after testing is finished*/
    private void clearDatabase() throws SQLException, DatabaseException {
        Connection c = rentalPersistence.getConnection();
        Statement s = c.createStatement();

        // Disable FK
        s.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Find all tables and truncate them
        Set<String> tables = new HashSet<String>();
        ResultSet rs = s.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            tables.add(rs.getString(1));
        }
        rs.close();
        for (String table : tables) {
            s.executeUpdate("TRUNCATE TABLE " + table);
        }

        // Idem for sequences
        Set<String> sequences = new HashSet<String>();
        rs = s.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            sequences.add(rs.getString(1));
        }
        rs.close();
        for (String seq : sequences) {
            s.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 1");
        }

        // Enable FK
        s.execute("SET REFERENTIAL_INTEGRITY TRUE");
        s.close();
    }
}

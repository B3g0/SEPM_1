package at.ac.tuwien.sepm.assignment.individual.rentalcars.persistence;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.DatabaseException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOLicenseInfo;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOOrder;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalPersistence implements RentalPersistenceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Connection connection = null;
    private String selectSQL = "SELECT * FROM VEHICLE WHERE ISDELETED = 'false'";

    /**
     * Getter for the connection to the database
     * @return connection to DB
     * @throws DatabaseException in case the connection fails
     */
    public static Connection getConnection() throws DatabaseException {
        if (connection == null) {
            try {
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection("jdbc:h2:~/test;INIT=RUNSCRIPT FROM 'classpath:sql/create.sql'", "SA", "");
            } catch (SQLException ex) {
                LOG.debug("Failed to establish connection to database!");
                throw new DatabaseException("Failed to establish connection to database!");
            } catch (ClassNotFoundException e) {
                LOG.error("Class not found!");
                LOG.error(e.getMessage());
            }
        }
        LOG.debug("Connection to database successfully established!");
        return connection;
    }

    /**
     * Closes the connection to DB
     * @throws DatabaseException in case something goes wrong
     */
    public static void closeConnection() throws DatabaseException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DatabaseException(e.getSQLState());
            }
        }
    }

    /**
     * main method of the persistence class
     * @param args
     * @throws DatabaseException
     */
    public static void main(String[] args) throws DatabaseException {
        getConnection();
    }

    @Override
    public void addVehicle(DTOVehicle v) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }

        PreparedStatement prepstmnt;
        try {
            prepstmnt = con.prepareStatement(
                "INSERT INTO VEHICLE(license,vehicle,year,description,seats,registration,drivetrain,power,price,created, isdeleted) " +
                    "VALUES ('" + v.getLicenseType() + "','" + v.getVehicleName() + "','" + v.getProduction() + "','" + v.getDescription() + "','" + v.getSeats() + "','" + v.getRegistration() + "','" + v.getDrivetrain() + "','" + v.getPower() + "','" + v.getPrice() + "','" + v.getDateAdded() + "','"+v.isDeleted()+"');");
            prepstmnt.executeUpdate();
            prepstmnt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(e.getSQLState()+" "+e.getMessage());
        }
        saveImage(v);
    }

    @Override
    public void addOrder(DTOOrder o) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }

        PreparedStatement prepstmntOrder;
        PreparedStatement prepstmntBind = null;
        PreparedStatement prepstmntGetID;
        LocalDateTime now = LocalDateTime.now();
        try {
            prepstmntOrder = con.prepareStatement(
                "INSERT INTO BOOKING(state, customer, paymentmethod, paymentnumber, orderstart, orderend, totalprice, created)" +
                    "VALUES ('" + o.getState() + "','" + o.getCustomerName() + "','" + o.getPaymentMethod() + "','" + o.getPaymentNumber() + "','" + o.getDateOrderedStart() + "','" + o.getDateOrderedEnd() + "','" + o.getTotalOrder() + "','" + now + "');"
            );
            prepstmntOrder.executeUpdate();
            LOG.debug("Order added into table booking successfully");
            prepstmntGetID = con.prepareStatement("SELECT TOP 1 * FROM BOOKING ORDER BY ID DESC");
            ResultSet rs = prepstmntGetID.executeQuery();
            rs.next();
            LOG.debug("Got row from table booking");
            ObservableList<DTOVehicle> vehicles = o.getVehicles();
            ObservableList<DTOLicenseInfo> licenseInfos = o.getLicenseInfo();
            ObservableList<DTOVehicle> copy = FXCollections.observableArrayList();
            copy.addAll(o.getVehicles());
            for (DTOVehicle vehicle: o.getVehicles()
                ) {
                for (DTOLicenseInfo info: o.getLicenseInfo()
                    ) {
                    if (vehicle.getID().equals(info.getVehicleID())&& copy.contains(vehicle)) {
                        prepstmntBind = con.prepareStatement(
                            "INSERT INTO BIND(orderid, vehicleid, licensenumber, licensedate)" +
                                "VALUES ('" + rs.getString(1) + "','" + vehicle.getID() + "','" + info.getLicenseNumber() + "','" + info.getLicenseDate() + "')"
                        );
                        prepstmntBind.executeUpdate();
                        copy.remove(vehicle);
                        LOG.debug("Vehicle and License info added into table bind successfully");
                    }
                }
            }
            for (DTOVehicle vehi: copy
                ) {
                prepstmntBind = con.prepareStatement(
                    "INSERT INTO BIND(orderid, vehicleid, licensenumber, licensedate)" +
                        "VALUES ('" + rs.getString(1) + "','" + vehi.getID() + "','null','null')"
                );
                prepstmntBind.executeUpdate();
                LOG.debug("Vehicle and order info added into table bind successfully");
            }
            prepstmntBind.close();
            prepstmntOrder.close();
            prepstmntGetID.close();
            LOG.debug("Order added into table booking successfully");
        } catch (SQLException s) {
            LOG.error("Error while adding order into table");
            throw new DatabaseException(s.getMessage()+""+s.getSQLState());
        }
    }

    @Override
    public ObservableList<DTOVehicle> fillTableVehicles() throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        ObservableList<DTOVehicle> data = FXCollections.observableArrayList();
        try {
            PreparedStatement prepstmnt = con.prepareStatement(selectSQL);
            ResultSet rs = prepstmnt.executeQuery();
            FileInputStream input = null;
            while (rs.next()) {
                Image image = null;
                try {
                    input = new FileInputStream(new File("pictures\\" + (rs.getString(11).substring(0,19).replace(':','-')) +".jpg"));
                    image = new Image(input);
                } catch (FileNotFoundException f) {
                    LOG.error("Error while loading picture!");
                    LOG.error(f.getMessage());
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            LOG.error("Error while closing stream!");
                            LOG.error(e.getMessage());
                        }
                    }
                }
                data.add(new DTOVehicle(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7),
                    rs.getString(8),
                    rs.getString(9),
                    rs.getString(10),
                    rs.getString(11),
                    rs.getString(12),
                    rs.getString(13),
                    image
                ));
            }
            prepstmnt.close();
        } catch (SQLException s) {
            LOG.error("Error while getting data for table!");
            throw new DatabaseException(s.getMessage()+""+s.getSQLState());
        }
        LOG.debug("Vehicles added into TableView successfully!");
        return data;
    }

    @Override
    public ObservableList<DTOOrder> fillTableOrders() throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        ObservableList<DTOOrder> data = FXCollections.observableArrayList();
        try {
            PreparedStatement prepstmnt = con.prepareStatement("SELECT * FROM BOOKING");
            ResultSet rs = prepstmnt.executeQuery();
            while (rs.next()) {
                selectSQL = "SELECT * FROM VEHICLE INNER JOIN BIND ON VEHICLE.ID = BIND.VEHICLEID WHERE BIND.ORDERID=" + rs.getString(1);
                ObservableList<DTOVehicle> dataVehicles = fillTableVehicles();
                ObservableList<DTOLicenseInfo> dataLicenses = fillLicenseInfo();
                data.add(new DTOOrder(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7),
                    dataVehicles,
                    dataLicenses,
                    Integer.parseInt(rs.getString(8)),
                    rs.getString(9),
                    rs.getString(10),
                    rs.getString(11)
                ));
            }
            prepstmnt.close();
        } catch (SQLException s) {
            LOG.error("Error while getting data for table!");
            throw new DatabaseException(s.getSQLState()+" "+s.getMessage());
        }
        selectSQL = "SELECT * FROM VEHICLE WHERE ISDELETED = 'false' ";
        LOG.debug("Orders added into TableView successfully!");
        return data;
    }

    /**
     * Fills an ObservableList<DTOLicenseInfo> with DTOLicenseInfo objects to bind the correct license info with its corresponding vehicle
     * @return ObservableList<DTOLicenseInfo> with all the license info from the DB
     * @throws DatabaseException in case the query has errors or the connection fails
     */
    public ObservableList<DTOLicenseInfo> fillLicenseInfo() throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        ObservableList<DTOLicenseInfo> data = FXCollections.observableArrayList();
        try {
            PreparedStatement prepstmnt = con.prepareStatement(selectSQL);
            ResultSet rs = prepstmnt.executeQuery();
            while (rs.next()) {
                data.add(new DTOLicenseInfo(
                    rs.getString(1),
                    rs.getString(17),
                    rs.getString(18)
                ));
            }
            prepstmnt.close();
        } catch (SQLException s) {
            LOG.error("Error while getting data for licenses!");
            throw new DatabaseException(s.getMessage() + "" + s.getSQLState());
        }
        LOG.debug("Licenses added into list successfully!");
        return data;
    }

    @Override
    public void updateVehicle(DTOVehicle v) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        try {
        LocalDateTime localDate = LocalDateTime.now();
        PreparedStatement prepstmnt = con.prepareStatement(selectSQL);
        ResultSet rs = prepstmnt.executeQuery();
        while (rs.next()) {
            File image = new File("pictures\\" + (rs.getString(11).substring(0, 19).replace(':', '-')) + ".jpg");
            if (v.getImage() == null) {
                if (image.delete()) {
                    LOG.debug("Image for vehicle " + v.getVehicleName() + " deleted successfully");
                } else {
                    LOG.debug("Error while deleting picture for vehicle " + v.getVehicleName());
                }
            } else {
                saveImage(v);
            }
        }
            prepstmnt = con.prepareStatement(
                "UPDATE VEHICLE SET " +
                    "LICENSE = '" + v.getLicenseType() +
                    "', VEHICLE = '" + v.getVehicleName() +
                    "', YEAR = '" + v.getProduction() +
                    "', DESCRIPTION = '" + v.getDescription() +
                    "', SEATS = '" + v.getSeats() +
                    "', REGISTRATION = '" + v.getRegistration() +
                    "', DRIVETRAIN = '" + v.getDrivetrain() +
                    "', POWER = '" + v.getPower() +
                    "', PRICE = '" + v.getPrice() +
                    "', UPDATED = '" + localDate +
                    "' WHERE ID = " + v.getID()
            );
            prepstmnt.executeUpdate();
            prepstmnt.close();
        } catch (SQLException e) {
            LOG.error("Error while updating vehicle");
            throw new DatabaseException(e.getMessage() + "" + e.getSQLState());
        }
    }

    @Override
    public void updateOrder(DTOOrder o) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        PreparedStatement prepstmnt;
        try {
            if (o.getDateBilled() != null) {
                prepstmnt = con.prepareStatement(
                    "UPDATE BOOKING SET " +
                        "STATE = '" + o.getState() +
                        "', CUSTOMER = '" + o.getCustomerName() +
                        "', PAYMENTMETHOD = '" + o.getPaymentMethod() +
                        "', PAYMENTNUMBER = '" + o.getPaymentNumber() +
                        "', ORDERSTART = '" + o.getDateOrderedStart() +
                        "', ORDEREND = '" + o.getDateOrderedEnd() +
                        "', TOTALPRICE = '" + o.getTotalOrder() +
                        "', BILLED = '" + o.getDateBilled() +
                        "', BILLNUMBER = '" + o.getBillNumber() +
                        "' WHERE ID = " + o.getID()
                );
            } else {
                prepstmnt = con.prepareStatement(
                    "UPDATE BOOKING SET " +
                        "STATE = '" + o.getState() +
                        "', CUSTOMER = '" + o.getCustomerName() +
                        "', PAYMENTMETHOD = '" + o.getPaymentMethod() +
                        "', PAYMENTNUMBER = '" + o.getPaymentNumber() +
                        "', ORDERSTART = '" + o.getDateOrderedStart() +
                        "', ORDEREND = '" + o.getDateOrderedEnd() +
                        "', TOTALPRICE = '" + o.getTotalOrder() +
                        "', BILLNUMBER = '" + o.getBillNumber() +
                        "' WHERE ID = " + o.getID()
                );
            }
            prepstmnt.executeUpdate();
            prepstmnt = con.prepareStatement("DELETE FROM BIND WHERE ORDERID=" + o.getID());
            LOG.debug("Bind row deleted");
            prepstmnt.executeUpdate();
            ObservableList<DTOVehicle> copy = FXCollections.observableArrayList();
            copy.addAll(o.getVehicles());
            for (DTOVehicle vehicle: o.getVehicles()
                ) {
                for (DTOLicenseInfo info: o.getLicenseInfo()
                    ) {
                    if (vehicle.getID().equals(info.getVehicleID())&& copy.contains(vehicle)) {
                        prepstmnt = con.prepareStatement(
                            "INSERT INTO BIND(orderid, vehicleid, licensenumber, licensedate)" +
                                "VALUES ('" + o.getID() + "','" + vehicle.getID() + "','" + info.getLicenseNumber() + "','" + info.getLicenseDate() + "')"
                        );
                        prepstmnt.executeUpdate();
                        copy.remove(vehicle);
                        LOG.debug("Vehicle and License info added into table bind successfully");
                    }
                }
                }
            for (DTOVehicle vehi: copy
                ) {
                prepstmnt = con.prepareStatement(
                    "INSERT INTO BIND(orderid, vehicleid, licensenumber, licensedate)" +
                        "VALUES ('" + o.getID() + "','" + vehi.getID() + "',null,null)"
                );
                prepstmnt.executeUpdate();
                LOG.debug("Vehicle and order info added into table bind successfully");
            }
            prepstmnt.close();
        } catch (SQLException e) {
            LOG.error("Error while updating order " + o.getCustomerName());
            throw new DatabaseException(e.getMessage() + "" + e.getSQLState());
        }
    }

    @Override
    public void deleteVehicle(String[] vehicleArray) throws DatabaseException{
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        PreparedStatement prepstmnt;
        PreparedStatement prepstmntOrderUpdate;
        try {
            for (int i = 0; i < vehicleArray.length; i++) {
                prepstmnt = con.prepareStatement("UPDATE VEHICLE SET ISDELETED = 'true' WHERE ID=" + vehicleArray[i]);
                prepstmntOrderUpdate = con.prepareStatement("DELETE FROM BIND WHERE VEHICLEID =" + vehicleArray[i]);
                prepstmnt.executeUpdate();
                prepstmntOrderUpdate.executeUpdate();
                prepstmnt.close();
                prepstmntOrderUpdate.close();
            }
            LOG.debug("Vehicles deleted successfully!");
        } catch (SQLException s) {
            LOG.error("Error while deleting Vehicles from DB!");
            throw new DatabaseException(s.getMessage() + "" + s.getSQLState());
        }
    }

    @Override
    public void deleteOrder(String id) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        PreparedStatement prepstmnt;
        try {
            prepstmnt = con.prepareStatement("DELETE FROM BOOKING WHERE ID=" + id);
            prepstmnt.executeUpdate();
            prepstmnt = con.prepareStatement("DELETE FROM BIND WHERE BOOKINGID=" + id);
            prepstmnt.executeUpdate();
            prepstmnt.close();
            LOG.debug("Order deleted successfully");
        } catch (SQLException s) {
            LOG.error("Error while deleting order");
            throw new DatabaseException(s.getMessage() + "" + s.getSQLState());
        }
    }

    /**
     * Saves an image from the DTOVehicle to a local path where it can be read later easily
     * @param v DTOVehicle with the Image File
     */
    private void saveImage(DTOVehicle v) {
        try {
            if (v.getImage() != null) {
                FileInputStream input;
                FileOutputStream output;
                int placeholder;

                String imagePath = URLDecoder.decode(v.getImage().getUrl().substring(6), "UTF-8");
                input = new FileInputStream(imagePath);
                output = new FileOutputStream("pictures\\" + (v.getDateAdded().substring(0,19).replace(':','-')) + ".jpg");
                while ((placeholder = input.read()) != -1) {
                    output.write(placeholder);
                }
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                LOG.debug("Picture of vehicle " + v.getVehicleName() + " saved successfully!");
            }
        } catch (IOException i) {
            LOG.error("Error while saving picture of vehicle "+v.getVehicleName());
            i.printStackTrace();
        }
    }

    @Override
    public List<String> checkIfAvailable(String dateStart, String dateEnd) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        PreparedStatement prepstmnt;
        List<String> list = new ArrayList<>();
        try {
            prepstmnt = con.prepareStatement("SELECT * FROM VEHICLE WHERE ID NOT IN " +
                "(SELECT VEHICLEID FROM BIND INNER JOIN BOOKING WHERE BOOKING.STATE = 'open' AND ('" + dateStart + "' BETWEEN ORDERSTART AND ORDEREND) " +
                "OR ('" + dateEnd + "' BETWEEN ORDERSTART AND ORDEREND)" +
                "OR ('" + dateStart + "' <= ORDERSTART AND ORDEREND >= '" + dateEnd + "'))");
            ResultSet rs = prepstmnt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            prepstmnt.close();
        } catch (SQLException s) {
            LOG.error("Error while checking if vehicles are available");
            throw new DatabaseException(s.getMessage() + "" + s.getSQLState());
        }
        return list;
    }

    @Override
    public ObservableList<DTOVehicle> filterVehicles(String[] query) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        ObservableList<DTOVehicle> selectedVehicles = FXCollections.observableArrayList();
        selectSQL = "SELECT * FROM VEHICLE WHERE ISDELETED='false' ";
        if (query[0] != null) {
            selectSQL += "AND LICENSE LIKE '%" + query[0] + "%' ";
        }
        if (!query[1].isEmpty()) {
            selectSQL += "AND VEHICLE LIKE '%" + query[1] + "%' ";
        }
        if (query[2] != null && query[3] != null) {
            selectSQL += "AND ID NOT IN " +
                "(SELECT VEHICLEID FROM BIND INNER JOIN BOOKING WHERE BOOKING.STATE = 'open' AND ('" + query[2] + "' BETWEEN ORDERSTART AND ORDEREND) " +
                "OR ('" + query[3] + "' BETWEEN ORDERSTART AND ORDEREND)" +
                "OR ('" + query[2] + "' <= ORDERSTART AND ORDEREND >= '" + query[3] + "')) ";
        }
        if (query[4] != null) {
            selectSQL += "AND DRIVETRAIN = '" + query[4] + "' ";
        }
        if (!query[5].isEmpty()) {
            selectSQL += "AND SEATS = '" + query[5] + "' ";
        }
        if (!query[6].isEmpty()) {
            selectSQL += "AND PRICE > '" + query[6] + "' ";
        }
        if (!query[7].isEmpty()) {
            selectSQL += "AND PRICE < '" + query[7] + "' ";
        }
        selectedVehicles = fillTableVehicles();
        selectSQL = "SELECT * FROM VEHICLE WHERE ISDELETED ='false' ";
        return selectedVehicles;
    }

    @Override
    public void copyEntriesforFinishing(DTOOrder order) throws DatabaseException {
        Connection con = null;
        try {
            con = getConnection();
        } catch (DatabaseException e) {
            LOG.error("Failed to establish connection to database!");
            throw new DatabaseException(e.getMessage());
        }
        try {
            PreparedStatement prepstmntInsert;
            PreparedStatement prepstmntGet;
            PreparedStatement prepstmntWrite1;
            PreparedStatement prepstmntWrite2;
            ResultSet rs;
            for (DTOVehicle v : order.getVehicles()
                ) {
                prepstmntInsert = con.prepareStatement("INSERT INTO VEHICLE (LICENSE, VEHICLE, YEAR, DESCRIPTION, SEATS, REGISTRATION, DRIVETRAIN, POWER, PRICE, CREATED, UPDATED, ISDELETED) " +
                        "SELECT LICENSE, VEHICLE, YEAR, DESCRIPTION, SEATS, REGISTRATION, DRIVETRAIN, POWER, PRICE, CREATED, UPDATED, ISDELETED from VEHICLE " +
                        "WHERE ID=" + v.getID());
                prepstmntGet = con.prepareStatement("SELECT TOP 1 * FROM VEHICLE ORDER BY ID DESC");

                prepstmntInsert.executeUpdate();
                rs = prepstmntGet.executeQuery();

                while (rs.next()) {
                    String ID = rs.getString(1);
                    prepstmntWrite1 = con.prepareStatement(
                        "UPDATE BIND SET " +
                            "VEHICLEID = '" + ID +
                            "' WHERE VEHICLEID = '" + v.getID() + "' AND ORDERID ='" + order.getID() + "'");
                    prepstmntWrite2 = con.prepareStatement("UPDATE VEHICLE SET ISDELETED = 'true' WHERE ID= '" + ID + "'");
                    prepstmntWrite1.executeUpdate();
                    prepstmntWrite2.executeUpdate();
                    prepstmntWrite1.close();
                    prepstmntWrite2.close();
                }
                prepstmntGet.close();
                prepstmntInsert.close();
            }
            LOG.debug("Order finished successfully!");
        } catch (SQLException e) {
            LOG.error("Error while finishing order");
            LOG.error(e.getMessage() + " " + e.getSQLState());
        }
    }
}

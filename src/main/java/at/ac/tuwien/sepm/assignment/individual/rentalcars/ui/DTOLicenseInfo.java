package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;


public class DTOLicenseInfo {

    private String ID;
    private String vehicleID;
    private String licenseNumber;
    private String licenseDate;

    public DTOLicenseInfo(String vehicleID, String licenseNumber, String licenseDate) {
        this.vehicleID = vehicleID;
        this.licenseNumber = licenseNumber;
        this.licenseDate = licenseDate;
    }

    public String getID() {
        return ID;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getLicenseDate() {
        return licenseDate;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setLicenseDate(String licenseDate) {
        this.licenseDate = licenseDate;
    }
}

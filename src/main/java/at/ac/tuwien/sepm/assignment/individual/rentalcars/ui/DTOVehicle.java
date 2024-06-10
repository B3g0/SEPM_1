package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import javafx.scene.image.Image;

public class DTOVehicle {

    private String ID;
    private String licenseType;
    private String vehicleName;
    private String production;
    private String description;
    private String seats;
    private String registration;
    private String drivetrain;
    private String power;
    private String price;
    private String dateAdded;
    private String dateEdited;
    private String isDeleted;
    private Image image;

    public DTOVehicle(String ID, String licenseType, String fieldName, String production, String description, String seats, String registration, String drivetrain, String power, String price, String dateAdded, String dateEdited, String isDeleted, Image image) {
        this.ID = ID;
        this.licenseType = licenseType;
        this.vehicleName = fieldName;
        this.production = production;
        this.description = description;
        this.seats = seats;
        this.registration = registration;
        this.drivetrain = drivetrain;
        this.power = power;
        this.price = price;
        this.dateAdded = dateAdded;
        this.dateEdited = dateEdited;
        this.isDeleted = isDeleted;
        this.image = image;
    }

    public String getID() {
        return ID;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getProduction() {
        return production;
    }

    public String getDescription() {
        return description;
    }

    public String getSeats() {
        return seats;
    }

    public String getRegistration() {
        return registration;
    }

    public String getDrivetrain() {
        return drivetrain;
    }

    public String getPower() {
        return power;
    }

    public String getPrice() {
        return price;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getDateEdited() {
        return dateEdited;
    }

    public String isDeleted() {
        return isDeleted;
    }

    public Image getImage() {
        return image;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public void setDrivetrain(String drivetrain) {
        this.drivetrain = drivetrain;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setDateEdited(String dateEdited) {
        this.dateEdited = dateEdited;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}

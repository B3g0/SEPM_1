package at.ac.tuwien.sepm.assignment.individual.rentalcars.ui;

import javafx.collections.ObservableList;

public class DTOOrder {

    private String state;
    private String ID;
    private String paymentMethod;
    private String paymentNumber;
    private String customerName;
    private String dateOrderedStart;
    private String dateOrderedEnd;
    private String dateOrderAdded;
    private ObservableList<DTOVehicle> vehicles;
    private int totalOrder;
    private ObservableList<DTOLicenseInfo> licenseInfo;
    private String dateBilled;
    private String billNumber;

    public DTOOrder(String ID, String state, String customerName, String paymentMethod, String paymentNumber, String dateOrderedStart, String dateOrderedEnd, ObservableList<DTOVehicle> vehicles, ObservableList<DTOLicenseInfo> licenseInfo, int totalOrder, String dateOrderAdded, String dateBilled, String billNumber) {
        this.ID = ID;
        this.state = state;
        this.customerName = customerName;
        this.paymentMethod = paymentMethod;
        this.paymentNumber = paymentNumber;
        this.dateOrderedStart = dateOrderedStart;
        this.dateOrderedEnd = dateOrderedEnd;
        this.vehicles = vehicles;
        this.licenseInfo = licenseInfo;
        this.totalOrder = totalOrder;
        this.dateOrderAdded = dateOrderAdded;
        this.dateBilled = dateBilled;
        this.billNumber = billNumber;
    }

    public String getState() {
        return state;
    }

    public String getID() {
        return ID;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDateOrderedStart() {
        return dateOrderedStart;
    }

    public String getDateOrderedEnd() {
        return dateOrderedEnd;
    }

    public String getDateOrderAdded() {
        return dateOrderAdded;
    }

    public String getDateBilled() {
        return dateBilled;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public ObservableList<DTOVehicle> getVehicles() {
        return vehicles;
    }

    public ObservableList<DTOLicenseInfo> getLicenseInfo() {
        return licenseInfo;
    }

    public int getTotalOrder() {
        return totalOrder;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTotalOrder(int totalOrder) {
        this.totalOrder = totalOrder;
    }

    public void setDateOrderAdded(String dateOrderAdded) {
        this.dateOrderAdded = dateOrderAdded;
    }

    public void setDateBilled(String dateBilled) {
        this.dateBilled = dateBilled;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setDateOrderedStart(String dateOrderedStart) {
        this.dateOrderedStart = dateOrderedStart;
    }

    public void setDateOrderedEnd(String dateOrderedEnd) {
        this.dateOrderedEnd = dateOrderedEnd;
    }

    public void setVehicles(ObservableList<DTOVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void setLicenseInfo(ObservableList<DTOLicenseInfo> licenseInfo) {
        this.licenseInfo = licenseInfo;
    }
}

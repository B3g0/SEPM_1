package at.ac.tuwien.sepm.assignment.individual.rentalcars;

import at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions.InvalidArgumentException;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalService;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.service.RentalServiceInterface;
import at.ac.tuwien.sepm.assignment.individual.rentalcars.ui.DTOVehicle;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class Test {

    private final RentalServiceInterface rentalServiceInterface = new RentalService();

    @org.junit.Test
    public void testAddNewVehicle() {

        DTOVehicle vehicle1 = new DTOVehicle(
            "1",
            "No License needed",
            "Vehicle 1",
            "product",
            "",
            "4",
            "",
            "Operated by human",
            "",
            "6",
            "2018-04-05",
            null,
            "false",
            null
        );

        try {
            rentalServiceInterface.checkVehicle(vehicle1);
        } catch (InvalidArgumentException e) {
            Assert.assertThat(e.getMessage(), is("Year not a valid number!"));
        }


    }

}

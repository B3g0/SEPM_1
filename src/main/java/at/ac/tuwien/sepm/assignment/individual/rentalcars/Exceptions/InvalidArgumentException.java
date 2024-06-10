package at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions;

public class InvalidArgumentException extends Exception{

    /**
     * Exeption which is thrown if checked argument does not comply to the needs of the varoius fields
     * @param msg String message defined by the developer for each case individually
     */
    public InvalidArgumentException(String msg) {
        super(msg);
    }
}

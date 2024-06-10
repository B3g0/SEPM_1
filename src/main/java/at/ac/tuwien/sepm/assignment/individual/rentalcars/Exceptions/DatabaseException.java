package at.ac.tuwien.sepm.assignment.individual.rentalcars.Exceptions;

public class DatabaseException extends Exception {
    /**
     * Exception which is thrown when a SQL-Exception is catched for the outer layers
     * @param msg String message defined by the developer for each case individually
     */
    public DatabaseException (String msg){
        super(msg);
    }
}

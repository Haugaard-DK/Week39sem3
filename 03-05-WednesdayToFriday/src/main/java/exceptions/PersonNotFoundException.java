package exceptions;

/**
 *
 * @author Mathias
 */
public class PersonNotFoundException extends Exception {
    public PersonNotFoundException(String message) {
        super(message);
    }
}

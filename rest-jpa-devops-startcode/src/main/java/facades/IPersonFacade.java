package facades;

import exceptions.PersonNotFoundException;
import dtos.PersonDTO;
import dtos.PersonsDTO;
import exceptions.MissingInputException;

/**
 *
 * @author Mathias
 */
public interface IPersonFacade {
  public PersonDTO addPerson(String fName, String lName, String phone, String street, String city, int zip) throws MissingInputException;  
  public PersonDTO deletePerson(int id) throws PersonNotFoundException;  
  public PersonDTO getPerson(int id) throws PersonNotFoundException;  
  public PersonsDTO getAllPersons();  
  public PersonDTO editPerson(PersonDTO p) throws PersonNotFoundException, MissingInputException;  


}

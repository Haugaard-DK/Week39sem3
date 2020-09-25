package facades;

import dtos.PersonDTO;
import dtos.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import utils.EMF_Creator;

/**
 *
 * @author Mathias
 */
public class PersonFacadeTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    private static List<Person> persons;
    private static List<PersonDTO> personDTOs;
    private static PersonsDTO personsDTO;

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = PersonFacade.getPersonFacade(emf);
        persons = new ArrayList<>();
        personDTOs = new ArrayList<>();
    }

    @AfterAll
    public static void tearDownClass() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        // Add test data here
        Address address1 = new Address("Gade1", "By1", 1000);
        Address address2 = new Address("Gade2", "By2", 2000);
        Address address3 = new Address("Gade3", "By3", 3000);
        persons.add(new Person("Nicklas", "Nielsen", "11111111", address1));
        persons.add(new Person("Mathias", "Nielsen", "22222222", address2));
        persons.add(new Person("Nikolaj", "Larsen", "11223344", address3));

        try {
            em.getTransaction().begin();
            for (Person person : persons) {
                em.persist(person);
                em.flush();
                em.clear();
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        persons.forEach(person -> {
            personDTOs.add(new PersonDTO(person));
        });

        personsDTO = new PersonsDTO(persons);
    }

    @AfterEach
    public void tearDown() {
        persons.clear();
        personDTOs.clear();
        personsDTO = null;

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testAddPerson_success() throws MissingInputException {
        // Arrange
        PersonDTO expected = personDTOs.get(0);

        // Act
        PersonDTO actual = facade.addPerson(expected.getfName(), expected.getlName(), expected.getPhone(),
                expected.getStreet(), expected.getCity(), expected.getZip());

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testAddPerson_invalid_firstName() throws MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setfName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.addPerson(person.getfName(), person.getlName(), person.getPhone(),
                        person.getStreet(), person.getCity(), person.getZip())
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testAddPerson_invalid_lastName() throws MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setlName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.addPerson(person.getfName(), person.getlName(), person.getPhone(),
                        person.getStreet(), person.getCity(), person.getZip())
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testAddPerson_invalid_firstName_and_lastName() throws MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setfName("");
        person.setlName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.addPerson(person.getfName(), person.getlName(), person.getPhone(),
                        person.getStreet(), person.getCity(), person.getZip())
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testDeletePerson_success() throws PersonNotFoundException {
        // Arrange
        PersonDTO expected = personDTOs.get(0);

        // Act
        PersonDTO actual = facade.deletePerson(expected.getId());

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testDeletePerson_invalid_id() throws PersonNotFoundException {
        // Arrange
        PersonDTO person = personDTOs.get(personDTOs.size() - 1);
        person.setId(person.getId() + 1);

        // Act
        PersonNotFoundException exception = assertThrows(PersonNotFoundException.class, ()
                -> facade.deletePerson(person.getId())
        );

        // Assert
        assertTrue(exception.getMessage().equals("Could not delete, provided id does not exist"));
    }

    @Test
    public void testGetPerson_success() throws PersonNotFoundException {
        // Arrange
        PersonDTO expected = personDTOs.get(0);

        // Act
        PersonDTO actual = facade.getPerson(expected.getId());

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPerson_invalid_id() throws PersonNotFoundException {
        // Arrange
        PersonDTO person = personDTOs.get(personDTOs.size() - 1);
        person.setId(person.getId() + 1);

        // Act
        PersonNotFoundException exception = assertThrows(PersonNotFoundException.class, ()
                -> facade.getPerson(person.getId())
        );

        // Assert
        assertTrue(exception.getMessage().equals("Provided id does not exist"));
    }

    @Test
    public void testGetAllPersons_success() {
        // Arrange
        PersonsDTO expected = personsDTO;

        // Act
        PersonsDTO actual = facade.getAllPersons();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testEditPerson_success() throws PersonNotFoundException, MissingInputException {
        // Arrange
        PersonDTO expected = personDTOs.get(0);
        expected.setfName("Lars");
        expected.setlName("Larsen");
        expected.setPhone("00000000");

        // Act
        PersonDTO actual = facade.editPerson(expected);

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testEditPerson_invalid_firstName() throws PersonNotFoundException, MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setfName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.editPerson(person)
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testEditPerson_invalid_lastName() throws PersonNotFoundException, MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setlName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.editPerson(person)
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testEditPerson_invalid_firstName_and_lastName() throws PersonNotFoundException, MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(0);
        person.setfName("");
        person.setlName("");

        // Act
        MissingInputException exception = assertThrows(MissingInputException.class, ()
                -> facade.editPerson(person)
        );

        // Assert
        assertTrue(exception.getMessage().equals("First Name and/or Last Name is missing"));
    }

    @Test
    public void testEditPerson_invalid_id() throws PersonNotFoundException, MissingInputException {
        // Arrange
        PersonDTO person = personDTOs.get(personDTOs.size() - 1);
        person.setId(person.getId() + 1);

        // Act
        PersonNotFoundException exception = assertThrows(PersonNotFoundException.class, ()
                -> facade.editPerson(person)
        );

        // Assert
        assertTrue(exception.getMessage().equals("Could not edit, provided id does not exist"));
    }
}

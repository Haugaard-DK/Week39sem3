package facades;

import exceptions.PersonNotFoundException;
import dtos.PersonDTO;
import dtos.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * Rename Class to a relevant name Add add relevant facade methods
 */
public class PersonFacade implements IPersonFacade{

    private static PersonFacade instance;
    private static EntityManagerFactory emf;
    
    //Private Constructor to ensure Singleton
    private PersonFacade() {}
    
    
    /**
     * 
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getPersonFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    //TODO Remove/Change this before use
    public long getPersonCount(){
        EntityManager em = emf.createEntityManager();
        try{
            long renameMeCount = (long)em.createQuery("SELECT COUNT(r) FROM RenameMe r").getSingleResult();
            return renameMeCount;
        }finally{  
            em.close();
        }
        
    }

    @Override
    public PersonDTO addPerson(String fName, String lName, String phone, String street, String city, int zip) throws MissingInputException {
        EntityManager em = getEntityManager();
        Address address = getAddress(street, city, zip);
        Person person = new Person(fName, lName, phone);
        
        if(fName.isEmpty() || lName.isEmpty()){
            throw new MissingInputException("First Name and/or Last Name is missing");
        } else if(street.isEmpty() || city.isEmpty() || zip <= 0){
            throw new MissingInputException("");
        }
        
        try{
            em.getTransaction().begin();
            em.persist(person);
            person.setAddress(address);
            em.merge(person);
            em.getTransaction().commit();
            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO deletePerson(int id) throws PersonNotFoundException{
        EntityManager em = getEntityManager();
        
        try{
            Person person = em.find(Person.class, id);
            
            if(person == null){
                throw new PersonNotFoundException("Could not delete, provided id does not exist");
            }
            
            Address address = person.getAddress();
            address.removePerson(person);
            
            boolean deleteAddress = false;
            if(address.getPersons().isEmpty()){
                deleteAddress = true;
            }
            
            em.getTransaction().begin();
            em.remove(person);
            if(deleteAddress){
                em.remove(address); 
            }
            em.getTransaction().commit();
            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO getPerson(int id) throws PersonNotFoundException{
        EntityManager em = getEntityManager();
        try{
            Person person = em.find(Person.class, id);
            
            if(person == null){
                throw new PersonNotFoundException("Provided id does not exist");
            }
            
            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }

    @Override
    public PersonsDTO getAllPersons() {
        EntityManager em = getEntityManager();
        try{
            Query query = em.createNamedQuery("Person.getAll");
            List<Person> persons = query.getResultList();
            
            return new PersonsDTO(persons);
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO editPerson(PersonDTO p) throws PersonNotFoundException, MissingInputException{
        EntityManager em = getEntityManager();
        
        if(p.getfName().isEmpty() || p.getlName().isEmpty()){
            throw new MissingInputException("First Name and/or Last Name is missing");
        } else if (p.getStreet().isEmpty() || p.getZip() <= 0 || p.getCity().isEmpty()) {
            throw new MissingInputException("Street and/or city is missing");
        }
        
        try {
            Person person = em.find(Person.class, p.getId());
            
            if(person == null){
                throw new PersonNotFoundException("Could not edit, provided id does not exist");
            }
            
            Address address = person.getAddress();
            
            em.getTransaction().begin();
            
            if(address.getPersons().contains(person) && address.getPersons().size() == 1){
                em.remove(address);
            }
            
            address.removePerson(person);
            address = getAddress(p.getStreet(), p.getCity(), p.getZip());
            
            person.setfName(p.getfName());
            person.setlName(p.getlName());
            person.setPhone(p.getPhone());
            person.setLastEdited(new Date());
            person.setAddress(address);
            
            em.merge(person);
            em.getTransaction().commit();
            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }
    
    private Address getAddress(String street, String city, int zip){
        EntityManager em = getEntityManager();
        
        try {
            Query query = em.createNamedQuery("Address.getAddress");
            query.setParameter("street", street);
            query.setParameter("city", city);
            query.setParameter("zip", zip);
            Address address;
            
            List<Address> addresses = query.getResultList();
            
            if(addresses.isEmpty()){
                address = new Address(street, city, zip);
            } else {
                int id = addresses.get(0).getId();
                address = em.find(Address.class, id);

                query = em.createNamedQuery("Person.getByAddress");
                query.setParameter("id", address.getId());

                List<Person> persons = query.getResultList();
                address.setPersons(persons);

            }
            
            return address;
        } finally{
            em.close();
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import dtos.PersonDTO;
import dtos.PersonsDTO;
import entities.Address;
import entities.Person;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

/**
 *
 * @author Mathias
 */
public class PersonResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api/person";
    private static List<Person> persons;
    private static List<PersonDTO> personDTOs;
    private static PersonsDTO personsDTO;
    private static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        persons = new ArrayList<>();
        personDTOs = new ArrayList<>();
        httpServer = startServer();
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterAll
    public static void tearDownClass() {
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
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
    public void testAPI_online() {
        given().when().get("/person").then().statusCode(200);
    }

    @Test
    public void testGetAllPersons_size() {
        given()
                .contentType(ContentType.JSON)
                .get("/person/all")
                .then()
                .assertThat()
                .body("all.size()", is(personDTOs.size()));
    }

    @Test
    public void testGetAllPersons_content() {
        List<PersonDTO> expected = personDTOs;
        List<PersonDTO> actual = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/person/all")
                .then()
                .extract().body().jsonPath().getList("all", PersonDTO.class);
        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void testGetPersonById_found() {
        PersonDTO expected = personDTOs.get(0);
        PersonDTO actual = given()
                .when()
                .get("/person/id/" + expected.getId())
                .then()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .extract().body().as(PersonDTO.class);
        assertThat(actual, is(expected));
    }

    @Test
    public void testGetPersonById_not_found() {
        int id = personDTOs.get(personDTOs.size() - 1).getId() + 1;
        given().when().get("/person/id/" + id).then().statusCode(HttpStatus.NOT_FOUND_404.getStatusCode());
    }

    @Test
    public void testAddPerson_added() {
        Address address = new Address("Gade69", "By420", 1337);
        Person person = new Person("TestMand", "Tester", "694201337", address);
        PersonDTO expected = new PersonDTO(person);
        
        PersonDTO actual = given()
                .contentType(ContentType.JSON)
                .body(expected)
                .when()
                .post("/person")
                .then()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .extract().body().as(PersonDTO.class);
        
        assertThat(actual, is(expected));
    }

    @Test
    public void testAddPerson_invalid_firstName() {
        Address address = new Address("Gade69", "By420", 1337);
        Person person = new Person("", "Tester", "694201337", address);
        PersonDTO personDTO = new PersonDTO(person);
        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .post("/person")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void testAddPerson_invalid_lastName() {
        Address address = new Address("Gade69", "By420", 1337);
        Person person = new Person("TestMand", "", "694201337", address);
        PersonDTO personDTO = new PersonDTO(person);
        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .post("/person")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void testAddPerson_server_error() {
        given().contentType(ContentType.JSON).when().post("/person").then().statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
    }

    @Test
    public void testEditPerson_edited() {
        PersonDTO expected = personDTOs.get(0);
        expected.setfName("Lars");

        PersonDTO actual = given()
                .contentType(ContentType.JSON)
                .body(expected)
                .when()
                .put("/person/id/" + expected.getId())
                .then()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .extract().body().as(PersonDTO.class);
        
        assertThat(actual, is(expected));
    }

    @Test
    public void testEditPerson_not_found() {
        PersonDTO personDTO = personDTOs.get(personDTOs.size() - 1);
        personDTO.setId(personDTO.getId() + 1);
        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .when()
                .put("/person/id/" + personDTO.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode());
    }

    @Test
    public void testEditPerson_invalid_firstName() {
        PersonDTO personDTO = personDTOs.get(0);
        personDTO.setfName("");
        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .put("/person/id/" + personDTO.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void testEditPerson_invalid_lastName() {
        PersonDTO personDTO = personDTOs.get(0);
        personDTO.setlName("");
        given()
                .contentType(ContentType.JSON)
                .body(personDTO)
                .put("/person/id/" + personDTO.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void testEditPerson_server_error() {
        given().when().put("/person/id/1").then().statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
    }

    @Test
    public void testDeletePerson_deleted() {
        int id = personDTOs.get(0).getId();
        given().when().delete("/person/id/" + id).then().statusCode(HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void testDeletePerson_invalid_id() {
        int id = personDTOs.get(personDTOs.size() - 1).getId() + 1;
        given().when().delete("/person/id/" + id).then().statusCode(HttpStatus.NOT_FOUND_404.getStatusCode());
    }
}

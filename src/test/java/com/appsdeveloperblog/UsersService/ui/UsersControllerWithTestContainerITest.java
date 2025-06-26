package com.appsdeveloperblog.UsersService.ui;

import com.appsdeveloperblog.UsersService.ui.model.User;
import com.appsdeveloperblog.UsersService.ui.model.UserRest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.responseSpecification;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ActiveProfiles("test")
public class UsersControllerWithTestContainerITest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    @LocalServerPort
    private int port;

    private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
    private final ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter();

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.filters(requestLoggingFilter, responseLoggingFilter);
    }

    @Order(1)
    @Test
    void testContainerIsRunning() {
        assertTrue(mysqlContainer.isRunning());
    }

    @Order(2)
    @Test
    void testCreateUser_whenValidDetailsProvided_returnsCreatedUser() {
        //Arrange
        Headers headers = new Headers(new Header("Content-Type", "application/json"),
                new Header("Accept", "application/json")
        );
        User newUser = new User("Sergey", "Kargopolov", "test@test.com", "123456789");
//        Map<String, Object> newUser = new HashMap<>(); We can also use the HashMap to create a user
//        newUser.put("firstName", "Sergey");
//        newUser.put("lastName", "Kargopolov");
//        newUser.put("email", "test@test.com");
//        newUser.put("password","123456789");
        //Act
                given()
                        .headers(headers)
                        .body(newUser)
                .when()
                        .post("/users")
                .then()
                        .log().all()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("firstName", equalTo(newUser.getFirstName()))
                        .body("lastName", equalTo(newUser.getLastName()))
                        .body("email", equalTo(newUser.getEmail()));
        //Assert

        }

}

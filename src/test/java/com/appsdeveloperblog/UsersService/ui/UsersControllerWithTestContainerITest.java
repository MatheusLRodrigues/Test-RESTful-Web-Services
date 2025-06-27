package com.appsdeveloperblog.UsersService.ui;

import com.appsdeveloperblog.UsersService.ui.model.User;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
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

    private final String TEST_EMAIL = "test@test.com";
    private final String TEST_PASSWORD = "123456789";
    private String userId;
    private String token;
    //private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter(); Commented because there is the .addFilter at Setup Method
    private final ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter();

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
       // RestAssured.filters(requestLoggingFilter, responseLoggingFilter);

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

       RestAssured.responseSpecification = new ResponseSpecBuilder()
               //.expectStatusCode(200)
               .expectResponseTime(lessThan(2000L))
               //.expectBody("id", notNullValue())
               .build();
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
//        Headers headers = new Headers(new Header("Content-Type", "application/json"),
//                new Header("Accept", "application/json")
//        );
        User newUser = new User("Sergey", "Kargopolov", "test@test.com", "123456789");
//        Map<String, Object> newUser = new HashMap<>(); We can also use the HashMap to create a user
//        newUser.put("firstName", "Sergey");
//        newUser.put("lastName", "Kargopolov");
//        newUser.put("email", "test@test.com");
//        newUser.put("password","123456789");
        //Act
                given()
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
    @Test
    @Order(3)
    void testLogin_whenValidCredentialsProvided_returnsTokenAndUserIdHeaders(){
        //Arrange
        Map<String,String> crendentials = new HashMap<>();
        crendentials.put("email", "test@test.com");
        crendentials.put("password", "123456789");
        //Act
            Response response = given()
                    .body(crendentials)
            .when()
                    .post("/login");

            this.userId = response.header("userId");
            this.token = response.header("token");
        //Assert
        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(userId);
        assertNotNull(token);
    }

    @Test
    @Order(4)
    void testGetUser_withValidAuthenticationToken_returnsUser(){
        given() //Arrange
                .pathParam("userId", this.userId)
                .header("Authorization", "Bearer " + this.token)
                //.auth().oauth2(this.token)
        .when() //Act
                .get("/users/{userId}")
        .then() //Assert
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(this.userId))
                .body("email", equalTo(TEST_EMAIL))
                .body("firstName", notNullValue())
                .body("lastName", notNullValue());
    }

    @Test
    @Order(5)
    void testGetUser_withMissingAuthHeader_returnsForbidden(){
    given()
            .pathParam("userId", this.userId)
    .when()
            .get("/users/{userId}")
    .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Order(6)
    void testGetUser_withValidTokenAndQueryParams_returnsPaginatedUsersList(){
        given()
                .header("Authorization", "Bearer " + this.token)
                .queryParam("page",1)
                .queryParam("limit",10)
        .when()
                .get("/users")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1));
    }

}

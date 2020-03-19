package de.borisskert.springrequestvalidation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersEndpointTest {
    private static final User USER_123 = new User("my_username", "my@fakemail.com", LocalDate.of(1990, 10, 3));
    private static final User USER_TO_CREATE = new User("my_other_username", "my_other@fakemail.com", LocalDate.of(1945, 5, 8));
    private static final User USER_TO_INSERT = new User("user_to_insert", "user_to_insert@fakemail.com", LocalDate.of(1948, 6, 21));

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() throws Exception {
        when(userService.getUserById("123")).thenReturn(Optional.of(USER_123));
        when(userService.findByUsername("my_username")).thenReturn(Optional.of(USER_123));

        when(userService.getUserById("666")).thenReturn(Optional.empty());

        when(userService.create(USER_TO_CREATE)).thenReturn("777");
    }

    @Test
    public void shouldRetrieveUserById() throws Exception {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/123", User.class);

        assertThat(response.getStatusCode(), is(equalTo(OK)));
        assertThat(response.getBody(), is(equalTo(USER_123)));
    }

    @Test
    public void shouldNotFindWithUnknownId() throws Exception {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/666", User.class);

        assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
    }

    @Test
    public void shouldFindUserByUsername() throws Exception {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=my_username", User.class);

        assertThat(response.getStatusCode(), is(equalTo(OK)));
        assertThat(response.getBody(), is(equalTo(USER_123)));
    }

    @Test
    public void shouldNotFindUserByUnknownUsername() throws Exception {
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=h4xx0r", User.class);

        assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
    }

    @Test
    public void shouldCreateUser() throws Exception {
        ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", USER_TO_CREATE, Void.class);

        assertThat(response.getStatusCode(), is(equalTo(CREATED)));
        assertThat(response.getHeaders().get("Location").get(0), is(equalTo("/api/users/777")));
    }

    @Test
    public void shouldInsertUser() throws Exception {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/444",
                HttpMethod.PUT,
                new HttpEntity<>(USER_TO_INSERT),
                Void.class
        );

        assertThat(response.getStatusCode(), is(equalTo(OK)));
    }
}

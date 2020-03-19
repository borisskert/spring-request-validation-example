package de.borisskert.springrequestvalidation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersEndpointTest {
    private static final String USER_ONE_ID = "6e9f59fa-cc85-4096-9165-7a3661fd6bc0";
    private static final User USER_ONE = new User("my_username", "my@fakemail.com", LocalDate.of(1990, 10, 3));
    private static final User USER_TO_CREATE = new User("my_other_username", "my_other@fakemail.com", LocalDate.of(1945, 5, 8));

    private static final String USER_ID_TO_INSERT = "2884a717-5a17-49fa-84cc-d4321207c7f9";
    private static final User USER_TO_INSERT = new User("user_to_insert", "user_to_insert@fakemail.com", LocalDate.of(1948, 6, 21));

    private static final String NOT_EXISTING_ID = "9b686071-2973-4001-b0f9-6267422d45f7";

    private static final User USER_WITH_DUPLICATE_USERNAME = new User("duplicate", "my@fakemail.com", LocalDate.of(1962, 7, 8));

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() throws Exception {
        when(userService.getUserById(USER_ONE_ID)).thenReturn(Optional.of(USER_ONE));
        when(userService.findByUsername("my_username")).thenReturn(Optional.of(USER_ONE));

        when(userService.getUserById(NOT_EXISTING_ID)).thenReturn(Optional.empty());

        when(userService.create(USER_TO_CREATE)).thenReturn("777");

        when(userService.create(eq(USER_WITH_DUPLICATE_USERNAME))).thenThrow(new UsernameAlreadyExistsException("Username 'duplicate' already exists"));
        doThrow(new UsernameAlreadyExistsException("Username 'duplicate' already exists"))
                .when(userService).insert(any(), eq(USER_WITH_DUPLICATE_USERNAME));
    }

    @Nested
    class GetById {
        @Test
        public void shouldRetrieveUserById() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + USER_ONE_ID, User.class);

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(USER_ONE)));
        }

        @Test
        public void shouldNotFindWithUnknownId() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + NOT_EXISTING_ID, User.class);

            assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
        }

        @Test
        public void shouldNotAcceptEmptyId() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users/", User.class);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }
    }

    @Nested
    class GetByUsername {
        @Test
        public void shouldFindUserByUsername() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=my_username", User.class);

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(USER_ONE)));
        }

        @Test
        public void shouldNotFindUserByUnknownUsername() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=h4xx0r", User.class);

            assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
        }

        @Test
        public void shouldAcceptTooShortUsername() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=ccc", User.class);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldAcceptTooLongUsername() throws Exception {
            ResponseEntity<User> response = restTemplate.getForEntity("/api/users?username=mycrazyusernamewhichistolong", User.class);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }
    }

    @Nested
    class Post {
        @Test
        public void shouldCreateUser() throws Exception {
            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", USER_TO_CREATE, Void.class);

            assertThat(response.getStatusCode(), is(equalTo(CREATED)));
            assertThat(response.getHeaders().get("Location").get(0), is(equalTo("/api/users/777")));
        }

        @Test
        public void shouldNotAllowUserWithoutUsername() throws Exception {
            User userToCreate = new User(null, "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", userToCreate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutEmail() throws Exception {
            User userToCreate = new User("my_username", null, LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", userToCreate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalEmail() throws Exception {
            User userToCreate = new User("my_username", "not_a_email", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", userToCreate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutBirthDate() throws Exception {
            User userToCreate = new User("my_username", "my@fakemail.com", null);

            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", userToCreate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
            User userToCreate = new User("my_username", "my@fakemail.com", LocalDate.now().plusYears(1));

            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", userToCreate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
            ResponseEntity<Void> response = restTemplate.postForEntity("/api/users", USER_WITH_DUPLICATE_USERNAME, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
        }
    }

    @Nested
    class Put {
        @Test
        public void shouldInsertUser() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(USER_TO_INSERT),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(OK)));
        }

        @Test
        public void shouldNotAllowToInsertUserWithIllegalId() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/444",
                    HttpMethod.PUT,
                    new HttpEntity<>(USER_TO_INSERT),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutUsername() throws Exception {
            User userToCreate = new User(null, "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToCreate),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutEmail() throws Exception {
            User userToCreate = new User("my_username", null, LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToCreate),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalEmail() throws Exception {
            User userToCreate = new User("my_username", "not_a_email", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToCreate),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutBirthDate() throws Exception {
            User userToCreate = new User("my_username", "my@fakemail.com", null);

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToCreate),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
            User userToCreate = new User("my_username", "my@fakemail.com", LocalDate.now().plusYears(1));

            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToCreate),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/users/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(USER_WITH_DUPLICATE_USERNAME),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
        }
    }
}

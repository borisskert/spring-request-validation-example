package de.borisskert.features.steps;

import de.borisskert.features.model.User;
import de.borisskert.features.model.UserWithId;
import de.borisskert.features.world.CucumberHttpClient;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;


public class UsersSteps {

    @Autowired
    private CucumberHttpClient httpClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @When("I ask for a user with Id {string}")
    public void iAskForAUserWithId(String userId) {
        httpClient.get("/api/users/" + userId);
    }

    @Then("I should return no users")
    public void shouldReturnNoUsers() {
        httpClient.verifyLatestStatus(NOT_FOUND);
    }

    @Given("A user has been created")
    public void aUserHasBeenCreatedWithId(User dataTable) {
        httpClient.post("/api/users/", dataTable);
    }

    @Then("I should get following user")
    public void iShouldGetFollowingUser(User dataTable) {
        httpClient.verifyLatestStatus(OK);
        httpClient.verifyLatestBody(dataTable, User.class);
    }

    @And("A user has been created with ID")
    public void aUserHasBeenCreatedWithID(UserWithId dataTable) {
        httpClient.put("/api/users/" + dataTable.id, dataTable.user);
    }

    @DataTableType
    public User defineUser(Map<String, String> entry) {
        return User.from(entry);
    }

    @DataTableType
    public UserWithId defineUserWithId(Map<String, String> entry) {
        return UserWithId.from(entry);
    }
}

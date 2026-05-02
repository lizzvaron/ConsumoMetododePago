package payment.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import payment.questions.ResponseCodeQuestion;
import payment.task.CreateTransactionTask;
import payment.task.GetMerchantTask;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PaymentStepDefinition {
    Actor user = Actor.named("Tester");
    @Given("that the user has access to the Wompi API")
    public void that_the_user_has_access_to_the_wompi_api() {
        user.can(CallAnApi.at("https://api-sandbox.co.uat.wompi.dev/v1"));
    }
    @When("querying the merchant information")
    public void querying_the_merchant_information() {
        user.attemptsTo(GetMerchantTask.execute());
    }
    @When("creating a PSE transaction")
    public void creating_a_pse_transaction() {
        user.attemptsTo(CreateTransactionTask.execute());
    }
    @Then("the response should be successful")
    public void the_response_should_be_successful() {
        assertThat(
                user.asksFor(ResponseCodeQuestion.value()),
                equalTo(201)
        );
    }


}

package payment.task;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.rest.interactions.Get;

public class GetMerchantTask implements Task {

    public static GetMerchantTask execute() {
        return Tasks.instrumented(GetMerchantTask.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        actor.attemptsTo(
                Get.resource("/merchants/pub_stagtest_g2u0HQd3ZMh05hsSgTS2lUV8t3s4mOt7")
                        .with(req -> req.log().all()) // LOG REQUEST
        );

        // LOG RESPONSE
        System.out.println("======== GET MERCHANT RESPONSE ========");
        SerenityRest.lastResponse().prettyPrint();

        // EXTRAER TOKEN
        String token = SerenityRest.lastResponse()
                .jsonPath()
                .getString("data.presigned_acceptance.acceptance_token");

        // LOG TOKEN
        System.out.println("ACCEPTANCE TOKEN: " + token);

        // GUARDAR EN EL ACTOR
        actor.remember("ACCEPTANCE_TOKEN", token);
    }
}
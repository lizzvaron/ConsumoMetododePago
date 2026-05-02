package payment.task;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.rest.interactions.Post;
import payment.utils.SignatureUtil;

import java.util.HashMap;
import java.util.Map;

public class CreateTransactionTask implements Task {

    public static CreateTransactionTask execute() {
        return Tasks.instrumented(CreateTransactionTask.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        // Datos base
        String token = actor.recall("ACCEPTANCE_TOKEN");
        String reference = "TEST_" + System.currentTimeMillis(); // ✅ UNA sola referencia
        int amount = 500000;
        String currency = "COP";
        String integrityKey = "stagtest_integrity_nAIBuqayW70XpUqJS4qf4STYiISd89Fp";

        // Generar firma correctamente
        String signature = SignatureUtil.generateSignature(
                reference,
                amount,
                currency,
                integrityKey
        );

        // Payment method
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("type", "PSE");
        paymentMethod.put("user_type", 0);
        paymentMethod.put("user_legal_id_type", "CC");
        paymentMethod.put("user_legal_id", "123456789");
        paymentMethod.put("financial_institution_code", "1");
        paymentMethod.put("payment_description", "Pago de prueba");

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("reference", reference); // ✅ mismo reference
        body.put("amount_in_cents", amount);
        body.put("currency", currency);
        body.put("customer_email", "test@test.com");
        body.put("payment_method", paymentMethod);
        body.put("acceptance_token", token);
        body.put("signature", signature); // ✅ STRING (no objeto)

        // LOG REQUEST
        System.out.println("======== REQUEST ========");
        System.out.println("REFERENCE: " + reference);
        System.out.println("SIGNATURE: " + signature);
        System.out.println(body);

        actor.attemptsTo(
                Post.to("/transactions")
                        .with(req -> req
                                .header("Authorization", "Bearer prv_stagtest_5i0ZGIGiFcDQifYsXxvsny7Y37tKqFWg")
                                .header("Content-Type", "application/json")
                                .body(body)
                                .log().all()
                        )
        );

        // LOG RESPONSE
        System.out.println("======== RESPONSE ========");
        SerenityRest.lastResponse().prettyPrint();
        System.out.println("Status Code: " + SerenityRest.lastResponse().statusCode());
    }
}
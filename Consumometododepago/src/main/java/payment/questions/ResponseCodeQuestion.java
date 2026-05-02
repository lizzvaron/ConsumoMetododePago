package payment.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.rest.SerenityRest;

public class ResponseCodeQuestion implements Question<Integer> {

    public static ResponseCodeQuestion value() {
        return new ResponseCodeQuestion();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().statusCode();
    }
}
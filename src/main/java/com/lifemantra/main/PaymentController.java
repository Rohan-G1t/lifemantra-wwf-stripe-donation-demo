package com.lifemantra.main;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @PostMapping
    public Map<String, Object> createPayment(@RequestBody Map<String, Object> request) throws StripeException {
        Long amount = Long.valueOf(request.get("amount").toString());
        String currency = request.get("currency").toString();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Payment intent created...");

        return Map.of("clientSecret", intent.getClientSecret());

    }
}

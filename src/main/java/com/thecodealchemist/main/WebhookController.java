package com.thecodealchemist.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/stripe")
public class WebhookController {

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String webhookSecret;


    @PostMapping
    public String handleWebhookEvent(
            HttpServletRequest request,
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String header) {


        System.out.println("WEBHOOK RECEIVED");


        Event event;

        try {

            event = Webhook.constructEvent(
                    payload,
                    header,
                    webhookSecret
            );

        } catch (SignatureVerificationException e) {

            System.out.println("Invalid webhook signature");
            return "Signature verification failed";

        }


        System.out.println("Event type = " + event.getType());


        switch (event.getType()) {


            case "checkout.session.completed":


                System.out.println("=================================");
                System.out.println("DONATION SUCCESSFUL!");


                try {

                    String rawJson =
                            event.getDataObjectDeserializer()
                                    .getRawJson();


                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode json =
                            mapper.readTree(rawJson);


                    String sessionId =
                            json.get("id").asText();


                    double amount =
                            json.get("amount_total")
                                    .asDouble() / 100.0;


                    String email =
                            json.get("customer_details")
                                    .get("email")
                                    .asText();


                    System.out.println("Session ID: " + sessionId);
                    System.out.println("Amount: $" + amount);
                    System.out.println("Customer Email: " + email);


                } catch (Exception e) {

                    System.out.println(
                            "Error reading checkout session: "
                                    + e.getMessage()
                    );

                }


                System.out.println("=================================");


                break;



            case "payment_intent.succeeded":


                PaymentIntent intent =
                        (PaymentIntent) event
                                .getDataObjectDeserializer()
                                .getObject()
                                .orElse(null);


                if (intent != null) {

                    System.out.println("PAYMENT SUCCESSFUL");
                    System.out.println(
                            "Payment ID: " + intent.getId()
                    );

                    System.out.println(
                            "Amount: $"
                                    + (intent.getAmount() / 100.0)
                    );

                }


                break;



            case "payment_intent.payment_failed":


                PaymentIntent failedIntent =
                        (PaymentIntent) event
                                .getDataObjectDeserializer()
                                .getObject()
                                .orElse(null);


                if (failedIntent != null) {

                    System.out.println("PAYMENT FAILED");
                    System.out.println(
                            "Payment ID: "
                                    + failedIntent.getId()
                    );

                }


                break;



            default:


                System.out.println(
                        "Unhandled event type: "
                                + event.getType()
                );


                break;

        }


        return "ok";

    }

}
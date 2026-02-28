package com.PlaNova.services;

import com.PlaNova.utils.EnvConfig;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripeService {

        public StripeService() {
                Stripe.apiKey = EnvConfig.get("STRIPE_SECRET_KEY");
        }

        public String createCheckoutSession(String destinationName, double amount) throws Exception {
                SessionCreateParams params = SessionCreateParams.builder()
                                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                                .setMode(SessionCreateParams.Mode.PAYMENT)
                                .setSuccessUrl("http://127.0.0.1:9090/success")
                                .setCancelUrl("http://127.0.0.1:9090/cancel")
                                .addLineItem(
                                                SessionCreateParams.LineItem.builder()
                                                                .setQuantity(1L)
                                                                .setPriceData(
                                                                                SessionCreateParams.LineItem.PriceData
                                                                                                .builder()
                                                                                                .setCurrency("eur")
                                                                                                .setUnitAmount((long) (amount
                                                                                                                * 100))
                                                                                                .setProductData(
                                                                                                                SessionCreateParams.LineItem.PriceData.ProductData
                                                                                                                                .builder()
                                                                                                                                .setName("Booking for "
                                                                                                                                                + destinationName)
                                                                                                                                .setDescription("Travel reservation via PlaNova")
                                                                                                                                .build())
                                                                                                .build())
                                                                .build())
                                .build();

                Session session = Session.create(params);
                return session.getUrl();
        }
}

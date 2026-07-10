package com.flare;

/**
 * Encapsulates the rules that decide a user's credit limit from their client.
 *
 * New client tiers can be added here without touching the user-creation flow.
 */
public class CreditLimitPolicy {
    private static final String VERY_IMPORTANT_CLIENT = "VeryImportantClient";
    private static final String IMPORTANT_CLIENT = "ImportantClient";

    private static final double STANDARD_CREDIT_LIMIT = 10_000;
    private static final double IMPORTANT_CLIENT_CREDIT_LIMIT = STANDARD_CREDIT_LIMIT * 2;

    public void applyTo(User user, Client client) {
        String clientName = client.getName();
        if (VERY_IMPORTANT_CLIENT.equals(clientName)) {
            user.setHasCreditLimit(false);
        } else if (IMPORTANT_CLIENT.equals(clientName)) {
            user.setHasCreditLimit(true);
            user.setCreditLimit(IMPORTANT_CLIENT_CREDIT_LIMIT);
        } else {
            user.setHasCreditLimit(true);
            user.setCreditLimit(STANDARD_CREDIT_LIMIT);
        }
    }
}

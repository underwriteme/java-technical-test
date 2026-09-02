package com.example.insurance.port;

import com.example.insurance.domain.Customer;

public interface EmailService {

    /**
     * Shares link to customer to submit details required.
     *
     * @param customer Customer to send email to
     */
    void sendInvitation(Customer customer);

    /**
     * Send Buy Now for cover email to customer.
     *
     * @param customer Customer to send email to
     */
    void sendBuyNow(Customer customer);

    /**
     * Send unable to offer cover to customer.
     *
     * @param customer Customer to send email to
     */
    void sendUnableToOffer(Customer customer);

}

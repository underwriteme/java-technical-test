package com.example.insurance.service;

import com.example.insurance.domain.Customer;
import com.example.insurance.port.EmailService;

import java.util.ArrayList;
import java.util.List;

public class EmailServiceMock implements EmailService {

    private final List<String> invitationEmailsSent = new ArrayList<>();

    @Override
    public void sendInvitation(Customer customer) {
        invitationEmailsSent.add(customer.email());
    }

    @Override
    public void sendBuyNow(Customer customer) {

    }

    @Override
    public void sendUnableToOffer(Customer customer) {

    }

    public List<String> getInvitationEmailsSent() {
        return invitationEmailsSent;
    }
}

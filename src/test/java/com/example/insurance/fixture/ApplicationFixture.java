package com.example.insurance.fixture;

import com.example.insurance.domain.Application;

import java.time.LocalDate;

public final class ApplicationFixture {

    private ApplicationFixture() {
        throw new IllegalStateException("Utility class");
    }

    public static Application validApplicationWithOneCustomer(final LocalDate createdAt,
                                                              final LocalDate policyStartDate) {
        final Application application = new Application(createdAt, policyStartDate);
        application.addCustomer(CustomerFixture.validCustomerAlice(policyStartDate));
        return application;
    }

    public static Application validApplicationWithTwoCustomer(final LocalDate createdAt,
                                                              final LocalDate policyStartDate) {
        final Application application = new Application(createdAt, policyStartDate);
        application.addCustomer(CustomerFixture.validCustomerAlice(policyStartDate));
        application.addCustomer(CustomerFixture.validCustomerBob(policyStartDate));
        return application;
    }

}

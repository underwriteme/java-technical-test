package com.example.insurance.fixture;

import com.example.insurance.domain.Customer;
import com.example.insurance.domain.Product;

import java.time.LocalDate;

public final class CustomerFixture {

    private CustomerFixture() {
        throw new IllegalStateException("Utility class");
    }

    public static Customer validCustomerAlice(final LocalDate policyStartDate) {
        return Customer.of("Alice", "alice@example.com", policyStartDate.minusYears(42))
                .withProduct(Product.LIFE);
    }

    public static Customer validCustomerBob(final LocalDate policyStartDate) {
        return Customer.of("Bob", "bob@example.com", policyStartDate.minusYears(35))
                .withProduct(Product.CRITICAL_ILLNESS);
    }
}

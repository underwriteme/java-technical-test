package com.example.insurance.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Application {

    private final List<Customer> customers;
    private final LocalDate createdAt;
    private final LocalDate policyStartDate;
    private ApplicationStatus status;

    public Application(final LocalDate createdAt,
                       final LocalDate policyStartDate) {

        this.createdAt = Objects.requireNonNull(createdAt);
        this.policyStartDate =  Objects.requireNonNull(policyStartDate);
        this.customers = new ArrayList<>();
        this.status = ApplicationStatus.DRAFT;
    }

    public void addCustomer(final Customer customer) {
        if (status != ApplicationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Customers can only be modified while the application is in DRAFT, was: " + status);
        }
        this.customers.add(customer);
    }

    public void updateCustomer(final Customer old, final Customer updated) {

        Objects.requireNonNull(updated, "updated cannot be null");
        Objects.requireNonNull(old, "old cannot be null");

        final int index = customers.indexOf(old);
        if (index < 0) {
            throw new IllegalArgumentException("Customer not found in application: " + old.name());
        }
        customers.set(index, updated);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getPolicyStartDate() {
        return policyStartDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void markAsShared() {
        transitionTo(ApplicationStatus.SHARED);
    }

    private void transitionTo(final ApplicationStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + next);
        }
        this.status = next;
    }
}

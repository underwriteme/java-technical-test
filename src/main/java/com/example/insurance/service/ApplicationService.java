package com.example.insurance.service;

import com.example.insurance.domain.Application;
import com.example.insurance.domain.ApplicationStatus;
import com.example.insurance.domain.Customer;
import com.example.insurance.port.Decision;
import com.example.insurance.port.EmailService;
import com.example.insurance.port.UnderwritingService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ApplicationService {

    private static final int MAX_AGE_AT_POLICY_START = 80;
    private static final int MAX_APPLICATION_AGE_DAYS = 30;
    private static final String EMAIL_REGEX = "^[^@]+@[^@]+\\.[^@]+$";

    private final Clock clock;

    private final EmailService emailService;
    private final UnderwritingService underwritingService;

    public ApplicationService(final Clock clock,
                              final EmailService emailService,
                              final UnderwritingService underwritingService) {
        this.clock = clock;
        this.emailService = Objects.requireNonNull(emailService);
        this.underwritingService = Objects.requireNonNull(underwritingService);
    }

    /**
     * Shares the application with all customers.
     * <p>
     * Validates that:
     * - The application was created within the last 30 days
     * - Each customer has at least one product selected
     * - Each customer has a valid email address
     * - All customers are under 80 years old at the policy start date
     * <p>
     * On success, sends an invitation email to each customer and
     * transitions the application to SHARED status.
     */
    public ShareResult shareApplication(final Application application) {

        Objects.requireNonNull(application, "application cannot be null");

        final List<ValidationError> errors = new ArrayList<>();

        if (!application.getStatus().canTransitionTo(ApplicationStatus.SHARED)) {
            return ShareResult.Failure.of("status",  "Cannot transition from " + application.getStatus() + " to " + ApplicationStatus.SHARED);
        }

        validateApplicationAge(application).ifPresent(errors::add);

        for (Customer customer : application.getCustomers()) {
            validateCustomerProducts(customer)
                    .ifPresent(errors::add);
            validateCustomerEmail(customer)
                    .ifPresent(errors::add);
            validateCustomerAge(customer, application.getPolicyStartDate())
                    .ifPresent(errors::add);
        }

        if (!errors.isEmpty()) {
            return new ShareResult.Failure(errors);
        }

        application.markAsShared();

        for (Customer customer : application.getCustomers()) {
            emailService.sendInvitation(customer);
        }

        return new ShareResult.Success();
    }

    /**
     * Accepts details submitted by a customer after the application has been shared.
     */
    public void submitCustomerDetails(final Application application,
                                      final Customer customer,
                                      final double height,
                                      final double weight,
                                      final String profession) {

        Objects.requireNonNull(application);
        Objects.requireNonNull(customer);

        Customer updated = customer.withDetails(height, weight, profession);
        application.updateCustomer(customer, updated);

        Decision decision = underwritingService.requestDecision(application);
        handleDecision(application, decision);
    }

    private void handleDecision(final Application application,
                                final Decision decision) {

        Objects.requireNonNull(application);
        Objects.requireNonNull(decision);

        final Consumer<Customer> action = switch (decision) {
            case QUOTE_AVAILABLE -> emailService::sendBuyNow;
            case UNABLE_TO_OFFER -> emailService::sendUnableToOffer;
        };

        application.getCustomers().forEach(action);
    }

    private Optional<ValidationError> validateApplicationAge(final Application application) {

        final LocalDate maxAppAgeCutoff = LocalDate.now(clock).minusDays(MAX_APPLICATION_AGE_DAYS);
        if (!application.getCreatedAt().isAfter(maxAppAgeCutoff)) {
            return Optional.of(new ValidationError("createdAt",
                    "Application was created more than " + MAX_APPLICATION_AGE_DAYS + " days ago and can no longer be shared."));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> validateCustomerProducts(final Customer customer) {

        if (customer.products().isEmpty()) {
            return Optional.of(new ValidationError("products",
                    "Customer " + customer.name() + " must have at least one product selected."));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> validateCustomerEmail(final Customer customer) {

        final String email = customer.email();
        if (!email.matches(EMAIL_REGEX)) {
            return Optional.of(new ValidationError("email",
                    "Customer " + customer.name() + " does not have a valid email address."));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> validateCustomerAge(final Customer customer,
                                                          final LocalDate policyStartDate) {

        final int ageAtPolicyStart = Period.between(customer.dateOfBirth(), policyStartDate).getYears();

        if (ageAtPolicyStart >= MAX_AGE_AT_POLICY_START) {
            return Optional.of(new ValidationError("dateOfBirth",
                    "Customer " + customer.name() + " is " + ageAtPolicyStart +
                            " years old at the policy start date and is not eligible."));
        }
        return Optional.empty();
    }
}

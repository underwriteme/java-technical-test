package com.example.insurance.service;

import com.example.insurance.domain.Application;
import com.example.insurance.domain.ApplicationStatus;
import com.example.insurance.domain.Customer;
import com.example.insurance.domain.Product;
import com.example.insurance.port.UnderwritingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static com.example.insurance.fixture.ApplicationFixture.validApplicationWithOneCustomer;
import static com.example.insurance.fixture.ApplicationFixture.validApplicationWithTwoCustomer;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationServiceTest {

    private static final Clock clock = Clock.fixed(LocalDateTime.of(2026, 6, 1, 12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final LocalDate TODAY_JUNE_FIRST = LocalDate.of(2026, 6, 1);
    private EmailServiceMock emailService;
    private UnderwritingService underwritingService;
    private ApplicationService applicationService;

    private static final LocalDate POLICY_START_JULY_FIRST = LocalDate.now().plusMonths(1);

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceMock();
        underwritingService = new UnderwritingServiceMock();
        applicationService = new ApplicationService(clock, emailService, underwritingService);
    }

    @Nested
    class ShareApplicationTest {

        @Test
        void shouldShareApplicationSuccessfully() {

            Application application = validApplicationWithOneCustomer(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.com");
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
        }

        @Test
        void shouldSendInvitationToEveryCustomer() {

            Application application = validApplicationWithTwoCustomer(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.com", "bob@example.com");
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
        }

        @Test
        void shouldRejectApplicationCreatedMoreThan30DaysAgo() {
            LocalDate createdAt = TODAY_JUNE_FIRST.minusDays(31);
            Application application = new Application(createdAt, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", "alice@example.com", LocalDate.of(1985, 6, 15))
                    .withProduct(Product.LIFE));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result).isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .anyMatch(e -> e.message().contains("30 days"));
        }

        @Test
        void shouldRejectCustomerWithNoProducts() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", "alice@example.com", LocalDate.of(1985, 6, 15)));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result).isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .anyMatch(e -> e.message().contains("product"));
        }

        @Test
        void shouldRejectCustomerWithEmailMissingAtSign() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", "notanemail.com", LocalDate.of(1985, 6, 15))
                    .withProduct(Product.LIFE));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .hasSize(1)
                    .anyMatch(e -> e.message().contains("email"));
        }

        @Test
        void shouldAcceptValidEmailWithNonComTld() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(
                    Customer.of("Alice", "alice@example.co.uk", LocalDate.of(1985, 6, 15))
                            .withProduct(Product.LIFE)
            );

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.co.uk");
        }

        @Test
        void shouldAllowCustomerWhoIs79AtPolicyStart() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(
                    Customer.of("Alice", "alice@example.com", POLICY_START_JULY_FIRST.minusYears(79))
                            .withProduct(Product.LIFE)
            );

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.com");
        }

        @Test
        void shouldRejectCustomerWhoIsExactly80AtPolicyStart() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", "alice@example.com", POLICY_START_JULY_FIRST.minusYears(80))
                    .withProduct(Product.LIFE));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .hasSize(1)
                    .anyMatch(e -> e.field().equals("dateOfBirth") && e.message().contains("80"));
        }

        @Test
        void shouldRejectCustomerWhoIs81AtPolicyStart() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", "alice@example.com", POLICY_START_JULY_FIRST.minusYears(81))
                    .withProduct(Product.LIFE));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Failure.class);
        }

        @Test
        void shouldAllowApplicationCreatedExactly30DaysAgo() {
            LocalDate createdAt = TODAY_JUNE_FIRST.minusDays(30);

            Application application = new Application(createdAt, POLICY_START_JULY_FIRST);
            application.addCustomer(
                    Customer.of("Alice", "alice@example.com", LocalDate.of(1985, 6, 15))
                            .withProduct(Product.LIFE)
            );

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.com");
        }

        @Test
        void shouldAllowApplicationCreatedLessThan30DaysAgo() {
            LocalDate createdAt = TODAY_JUNE_FIRST.minusDays(29);

            Application application = new Application(createdAt, POLICY_START_JULY_FIRST);
            application.addCustomer(
                    Customer.of("Alice", "alice@example.com", LocalDate.of(1985, 6, 15))
                            .withProduct(Product.LIFE)
            );

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Success.class);
            assertThat(application.getStatus())
                    .isEqualTo(ApplicationStatus.SHARED);
            assertThat(emailService.getInvitationEmailsSent())
                    .containsExactly("alice@example.com");
        }

        @Test
        void shouldRejectCustomerWithInvalidEmail() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            application.addCustomer(Customer.of("Alice", null, LocalDate.of(1985, 6, 15))
                    .withProduct(Product.LIFE));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result).isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .hasSize(1)
                    .anyMatch(e -> e.message().contains("email"));
        }

        @Test
        void shouldRejectApplicationNotInDraftStatus() {

            Application application = validApplicationWithOneCustomer(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            applicationService.shareApplication(application); // moves to SHARED

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .hasSize(1)
                    .anyMatch(e -> e.field().equals("status"));
        }

        @Test
        void shouldCollectMultipleValidationErrors() {
            Application application = new Application(TODAY_JUNE_FIRST, POLICY_START_JULY_FIRST);
            // Customer with no products AND invalid email
            application.addCustomer(Customer.of("Alice", "invalid-email", LocalDate.of(1985, 6, 15)));

            ShareResult result = applicationService.shareApplication(application);

            assertThat(result)
                    .isInstanceOf(ShareResult.Failure.class);
            ShareResult.Failure failure = (ShareResult.Failure) result;
            assertThat(failure.errors())
                    .hasSize(2)
                    .anyMatch(e -> e.field().equals("products"))
                    .anyMatch(e -> e.field().equals("email"));
        }
    }

    // ---------- Validation fails END (Task 1) ----------

    // ---------- Customer details submission (Task 2) ----------

    @Nested
    class SubmitCustomerDetailsTest {


    }
}

package com.example.insurance.service;

import java.util.List;
import java.util.Objects;

public sealed interface ShareResult {

    record Success() implements ShareResult { }

    record Failure(List<ValidationError> errors) implements ShareResult {
        public Failure {
            Objects.requireNonNull(errors, "errors");
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("errors is empty");
            }
            errors = List.copyOf(errors);
        }

        public static Failure of(final ValidationError error) {
            return new Failure(List.of(error));
        }

        public static Failure of(final String field, final String message) {
            return of(new ValidationError(field, message));
        }
    }
}

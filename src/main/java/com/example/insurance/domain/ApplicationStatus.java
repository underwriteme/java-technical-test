package com.example.insurance.domain;

public enum ApplicationStatus {
    DRAFT,
    SHARED;

    public boolean canTransitionTo(final ApplicationStatus next) {
        return switch (this) {
            case DRAFT  -> next == SHARED;
            case SHARED -> false;
        };
    }
}

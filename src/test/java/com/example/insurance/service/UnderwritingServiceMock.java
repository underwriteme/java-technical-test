package com.example.insurance.service;

import com.example.insurance.domain.Application;
import com.example.insurance.port.Decision;
import com.example.insurance.port.UnderwritingService;

public class UnderwritingServiceMock implements UnderwritingService {
    @Override
    public Decision requestDecision(Application application) {
        return Decision.QUOTE_AVAILABLE;
    }
}

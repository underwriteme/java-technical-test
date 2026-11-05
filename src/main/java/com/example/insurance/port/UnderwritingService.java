package com.example.insurance.port;

import com.example.insurance.domain.Application;

public interface UnderwritingService {

    Decision requestDecision(Application application);

}

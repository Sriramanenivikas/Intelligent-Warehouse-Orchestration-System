package com.iwos.returns.domain;

import java.util.UUID;

public class ReturnRequestNotFoundException extends RuntimeException {

    public ReturnRequestNotFoundException(UUID returnRequestId) {
        super("Return request not found for id " + returnRequestId);
    }
}

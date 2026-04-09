package com.iwos.payment.application;

public record PaymentCommandResult<T>(T response, boolean replayed) {
}

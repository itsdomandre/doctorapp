package com.domandre.controllers.request;

import com.domandre.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordPaymentRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}

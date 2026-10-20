package com.sanedge.paymentgateway.merchant.domain.requests.statsbyid;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class MonthYearPaymentMethodMerchant {
    @Min(value = 1, message = "merchant_id minimal 1")
    private Long merchantId;

    @Min(2000)
    @Max(2100)
    private Long year;
}
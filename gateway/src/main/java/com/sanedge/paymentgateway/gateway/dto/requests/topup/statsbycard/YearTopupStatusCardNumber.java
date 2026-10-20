package com.sanedge.paymentgateway.gateway.dto.requests.topup.statsbycard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request status topup per tahun untuk card number tertentu")
public class YearTopupStatusCardNumber {

    @NotBlank(message = "Card number wajib diisi")
    private String cardNumber;

    @Min(value = 2000, message = "Tahun tidak valid")
    @Max(value = 2100, message = "Tahun tidak valid")
    private Long year;
}

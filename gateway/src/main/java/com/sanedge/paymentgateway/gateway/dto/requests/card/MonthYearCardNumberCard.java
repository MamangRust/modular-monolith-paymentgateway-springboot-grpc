package com.sanedge.paymentgateway.gateway.dto.requests.card;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthYearCardNumberCard {

    @Parameter(description = "Nomor kartu")
    @NotBlank(message = "Card number wajib diisi")
    private String cardNumber;

    @Parameter(description = "Tahun")
    @Min(value = 2000, message = "Tahun harus antara 2000 dan 2100")
    @Max(value = 2100, message = "Tahun harus antara 2000 dan 2100")
    private Long year;
}
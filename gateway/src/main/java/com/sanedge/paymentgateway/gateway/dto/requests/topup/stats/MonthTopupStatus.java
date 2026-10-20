package com.sanedge.paymentgateway.gateway.dto.requests.topup.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Request untuk status topup per bulan")
public class MonthTopupStatus {

    @Min(value = 2000, message = "Tahun tidak valid")
    @Max(value = 2100, message = "Tahun tidak valid")
    private Long year;

    @Min(value = 1, message = "Bulan harus >= 1")
    @Max(value = 12, message = "Bulan harus <= 12")
    private int month;
}
package com.sanedge.paymentgateway.gateway.dto.requests.saldo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Request untuk total saldo per bulan & tahun")
public class MonthTotalSaldoBalance {

    @Min(value = 1900, message = "Tahun tidak valid")
    @Max(value = 2100, message = "Tahun tidak valid")
    private Long year;

    @Min(value = 1, message = "Bulan harus >= 1")
    @Max(value = 12, message = "Bulan harus <= 12")
    private int month;
}
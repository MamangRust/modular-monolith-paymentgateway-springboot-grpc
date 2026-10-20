package com.sanedge.paymentgateway.gateway.dto.requests.transaction;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindAllTransactionCardNumber {

    @NotBlank(message = "Card number wajib diisi")
    private String cardNumber;

    @Parameter(description = "Nomor halaman", example = "1")
    @Min(value = 1, message = "Page minimal 1")
    private Integer page = 1;

    @Parameter(description = "Jumlah data per halaman", example = "10")
    @Min(value = 1, message = "Page size minimal 1")
    private Integer pageSize = 10;

    private String search = "";
}
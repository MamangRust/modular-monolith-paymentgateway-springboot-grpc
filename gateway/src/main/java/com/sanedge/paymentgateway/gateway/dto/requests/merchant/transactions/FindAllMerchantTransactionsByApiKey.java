package com.sanedge.paymentgateway.gateway.dto.requests.merchant.transactions;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllMerchantTransactionsByApiKey {
    @NotBlank(message = "api_key wajib diisi")
    private String apiKey;

    @Parameter(description = "Halaman data", example = "1")
    @Min(value = 1)
    private Integer page = 1;

    @Parameter(description = "Jumlah data per halaman", example = "10")
    @Min(value = 1)
    private Integer pageSize = 10;

    @Parameter(description = "Pencarian transaksi")
    private String search = "";
}
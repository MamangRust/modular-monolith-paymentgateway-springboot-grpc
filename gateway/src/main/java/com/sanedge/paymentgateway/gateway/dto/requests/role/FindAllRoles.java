package com.sanedge.paymentgateway.gateway.dto.requests.role;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllRoles {
    @Parameter(description = "Nomor halaman", example = "1")
    @Min(value = 1, message = "Page minimal 1")
    private Integer page = 1;

    @Parameter(description = "Jumlah data per halaman", example = "10")
    @Min(value = 1, message = "Page size minimal 1")
    private Integer pageSize = 10;

    @Parameter(description = "Pencarian berdasarkan nama role")
    private String search = "";
}
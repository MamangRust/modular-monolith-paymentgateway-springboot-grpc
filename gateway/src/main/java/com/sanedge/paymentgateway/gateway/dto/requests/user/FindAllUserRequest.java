package com.sanedge.paymentgateway.gateway.dto.requests.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAllUserRequest {

    @Schema(description = "Halaman yang akan diambil", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Page harus minimal 1")
    private Integer page = 1;

    @Schema(description = "Jumlah data per halaman", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Page size harus minimal 1")
    private Integer pageSize = 10;

    @Schema(description = "Pencarian berdasarkan nama/email/field lain", example = "john")
    private String search = "";
}
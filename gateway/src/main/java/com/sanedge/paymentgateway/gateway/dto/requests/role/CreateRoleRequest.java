package com.sanedge.paymentgateway.gateway.dto.requests.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request untuk membuat role baru")
public class CreateRoleRequest {

    @NotBlank(message = "Nama role wajib diisi")
    private String name;
}
package com.sanedge.paymentgateway.gateway.dto.requests.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request untuk update kartu")
public class UpdateCardRequest {

    private Long cardId;

    @Min(value = 1, message = "User ID minimal 1")
    private Long userId;

    @NotBlank(message = "Card type wajib diisi")
    private String cardType;

    @Schema(description = "Tanggal expired", example = "2025-12-31")
    private LocalDate expireDate;

    @NotBlank(message = "CVV wajib diisi")
    private String cvv;

    @NotBlank(message = "Card provider wajib diisi")
    private String cardProvider;
}
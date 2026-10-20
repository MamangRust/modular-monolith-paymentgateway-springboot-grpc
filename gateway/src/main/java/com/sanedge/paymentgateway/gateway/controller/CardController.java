package com.sanedge.paymentgateway.gateway.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.requests.card.CreateCardRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.card.FindAllCards;
import com.sanedge.paymentgateway.gateway.dto.requests.card.UpdateCardRequest;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboard;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboardCard;
import com.sanedge.paymentgateway.gateway.dto.responses.card.stats.amount.CardResponseMonthAmount;
import com.sanedge.paymentgateway.gateway.dto.responses.card.stats.amount.CardResponseYearAmount;
import com.sanedge.paymentgateway.gateway.dto.responses.card.stats.balance.CardResponseMonthBalance;
import com.sanedge.paymentgateway.gateway.dto.responses.card.stats.balance.CardResponseYearBalance;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardDashboardServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.card.stats.CardStatsBalanceServiceGrpc;
import com.sanedge.paymentgateway.pb.card.stats.CardStatsTopupServiceGrpc;
import com.sanedge.paymentgateway.pb.card.stats.CardStatsTransactionServiceGrpc;
import com.sanedge.paymentgateway.pb.card.stats.CardStatsTransferServiceGrpc;
import com.sanedge.paymentgateway.pb.card.stats.CardStatsWithdrawServiceGrpc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@PreAuthorize("isAuthenticated()")
public class CardController {

    private final CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryStub;
    private final CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandStub;
    private final CardStatsBalanceServiceGrpc.CardStatsBalanceServiceBlockingStub cardStatsBalanceStub;
    private final CardStatsTopupServiceGrpc.CardStatsTopupServiceBlockingStub cardStatsTopupStub;
    private final CardStatsTransactionServiceGrpc.CardStatsTransactionServiceBlockingStub cardStatsTransactionStub;
    private final CardStatsTransferServiceGrpc.CardStatsTransferServiceBlockingStub cardStatsTransferStub;
    private final CardStatsWithdrawServiceGrpc.CardStatsWithdrawServiceBlockingStub cardStatsWithdrawStub;
    private final CardDashboardServiceGrpc.CardDashboardServiceBlockingStub cardDashboardStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<CardResponse>>> findCards(@Valid @ModelAttribute FindAllCards req) {
        var resp = cardQueryStub.findAllCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<CardResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cards(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<CardResponseDeleteAt>>> findActiveCards(
            @Valid @ModelAttribute FindAllCards req) {
        var resp = cardQueryStub.findByActiveCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<CardResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponsePagination<List<CardResponseDeleteAt>>> findTrashedCards(
            @Valid @ModelAttribute FindAllCards req) {
        var resp = cardQueryStub.findByTrashedCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<CardResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CardResponse>> findCard(@PathVariable Long id) {
        var resp = cardQueryStub.findByIdCard(com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                .setCardId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.card(resp.getData()))
                .build());
    }

    @GetMapping("/by-user/{user_id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CardResponse>> findCardByUser(@PathVariable Long user_id) {
        var resp = cardQueryStub.findByUserIdCard(com.sanedge.paymentgateway.pb.card.FindByUserIdCardRequest.newBuilder()
                .setUserId(user_id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.card(resp.getData()))
                .build());
    }

    @GetMapping("/by-number/{card_number}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CardResponse>> findCardByNumber(@PathVariable String card_number) {
        var resp = cardQueryStub.findByCardNumber(com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                .setCardNumber(card_number)
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.card(resp.getData()))
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@Valid @RequestBody CreateCardRequest request) {
        var resp = cardCommandStub.createCard(com.sanedge.paymentgateway.pb.card.CreateCardRequest.newBuilder()
                .setUserId(nvl(request.getUserId()))
                .setCardType(nvl(request.getCardType()))
                .setExpireDate(ProtoMapper.toTimestamp(request.getExpireDate()))
                .setCvv(nvl(request.getCvv()))
                .setCardProvider(nvl(request.getCardProvider()))
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.card(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CardResponse>> updateCard(@Valid @RequestBody UpdateCardRequest request) {
        var resp = cardCommandStub.updateCard(com.sanedge.paymentgateway.pb.card.UpdateCardRequest.newBuilder()
                .setCardId(nvl(request.getCardId()))
                .setUserId(nvl(request.getUserId()))
                .setCardType(nvl(request.getCardType()))
                .setExpireDate(ProtoMapper.toTimestamp(request.getExpireDate()))
                .setCvv(nvl(request.getCvv()))
                .setCardProvider(nvl(request.getCardProvider()))
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.card(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CardResponseDeleteAt>> trashCard(@PathVariable Long id) {
        var resp = cardCommandStub.trashedCard(com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                .setCardId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CardResponseDeleteAt>> restoreCard(@PathVariable Long id) {
        var resp = cardCommandStub.restoreCard(com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                .setCardId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<CardResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteCard(@PathVariable Long id) {
        var resp = cardCommandStub.deleteCardPermanent(com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                .setCardId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/restore-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> restoreAllCards() {
        var resp = cardCommandStub.restoreAllCard(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAllCards() {
        var resp = cardCommandStub.deleteAllCardPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- stats: balance ----

    @GetMapping("/stats/balance/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthBalance>>> findMonthlyBalance(@RequestParam Long year) {
        var resp = cardStatsBalanceStub
                .findMonthlyBalance(com.sanedge.paymentgateway.pb.card.stats.FindYearBalance.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthBalance>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthBalances(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/balance/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearBalance>>> findYearlyBalance(@RequestParam Long year) {
        var resp = cardStatsBalanceStub
                .findYearlyBalance(com.sanedge.paymentgateway.pb.card.stats.FindYearBalance.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearBalance>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearBalances(resp.getDataList()))
                .build());
    }

    // ---- stats: topup ----

    @GetMapping("/stats/topup/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> findMonthlyTopupAmount(@RequestParam Long year) {
        var resp = cardStatsTopupStub.findMonthlyTopupAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                .setYear(year.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/topup/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> findYearlyTopupAmount(@RequestParam Long year) {
        var resp = cardStatsTopupStub.findYearlyTopupAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                .setYear(year.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: transaction ----

    @GetMapping("/stats/transaction/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> findMonthlyTransactionAmount(
            @RequestParam Long year) {
        var resp = cardStatsTransactionStub
                .findMonthlyTransactionAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transaction/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> findYearlyTransactionAmount(
            @RequestParam Long year) {
        var resp = cardStatsTransactionStub
                .findYearlyTransactionAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: transfer ----

    @GetMapping("/stats/transfer/monthly/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> findMonthlyTransferAmountSender(
            @RequestParam Long year) {
        var resp = cardStatsTransferStub
                .findMonthlyTransferSenderAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/monthly/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> findMonthlyTransferAmountReceiver(
            @RequestParam Long year) {
        var resp = cardStatsTransferStub
                .findMonthlyTransferReceiverAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/yearly/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> findYearlyTransferAmountSender(
            @RequestParam Long year) {
        var resp = cardStatsTransferStub
                .findYearlyTransferSenderAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/yearly/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> findYearlyTransferAmountReceiver(
            @RequestParam Long year) {
        var resp = cardStatsTransferStub
                .findYearlyTransferReceiverAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: withdraw ----

    @GetMapping("/stats/withdraw/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> findMonthlyWithdrawAmount(
            @RequestParam Long year) {
        var resp = cardStatsWithdrawStub
                .findMonthlyWithdrawAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/withdraw/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> findYearlyWithdrawAmount(
            @RequestParam Long year) {
        var resp = cardStatsWithdrawStub
                .findYearlyWithdrawAmount(com.sanedge.paymentgateway.pb.card.FindYearAmount.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: by card ----

    @GetMapping("/stats/balance/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthBalance>>> getMonthlyBalanceByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsBalanceStub
                .findMonthlyBalanceByCardNumber(com.sanedge.paymentgateway.pb.card.stats.FindYearBalanceCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthBalance>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthBalances(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/balance/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearBalance>>> getYearlyBalanceByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsBalanceStub
                .findYearlyBalanceByCardNumber(com.sanedge.paymentgateway.pb.card.stats.FindYearBalanceCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearBalance>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearBalances(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/topup/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> getMonthlyTopupAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTopupStub
                .findMonthlyTopupAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/topup/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> getYearlyTopupAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTopupStub
                .findYearlyTopupAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transaction/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> getMonthlyTransactionAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTransactionStub
                .findMonthlyTransactionAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transaction/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> getYearlyTransactionAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTransactionStub
                .findYearlyTransactionAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/monthly/by-card/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> getMonthlyTransferAmountByCardSender(
            @RequestParam Long year, @RequestParam(required = false) Integer month,
            @RequestParam String cardNumber) {
        var resp = cardStatsTransferStub
                .findMonthlyTransferSenderAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/monthly/by-card/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> getMonthlyTransferAmountByCardReceiver(
            @RequestParam Long year, @RequestParam(required = false) Integer month,
            @RequestParam String cardNumber) {
        var resp = cardStatsTransferStub
                .findMonthlyTransferReceiverAmountByCardNumber(
                        com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                                .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/yearly/by-card/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> getYearlyTransferAmountByCardSender(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTransferStub
                .findYearlyTransferSenderAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/transfer/yearly/by-card/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> getYearlyTransferAmountByCardReceiver(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsTransferStub
                .findYearlyTransferReceiverAmountByCardNumber(
                        com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                                .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/withdraw/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseMonthAmount>>> getMonthlyWithdrawAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsWithdrawStub
                .findMonthlyWithdrawAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseMonthAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/withdraw/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<CardResponseYearAmount>>> getYearlyWithdrawAmountByCard(
            @RequestParam Long year, @RequestParam String cardNumber) {
        var resp = cardStatsWithdrawStub
                .findYearlyWithdrawAmountByCardNumber(com.sanedge.paymentgateway.pb.card.FindYearAmountCardNumber
                        .newBuilder().setYear(year.intValue()).setCardNumber(nvl(cardNumber)).build());
        return ResponseEntity.ok(ApiResponse.<List<CardResponseYearAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- dashboard ----

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CardDashboard>> findCardDashboard() {
        var resp = cardDashboardStub.dashboardCard(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<CardDashboard>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardDashboard(resp.getData()))
                .build());
    }

    @GetMapping("/dashboard/{card_number}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<CardDashboardCard>> findCardDashboardByCardNumber(
            @PathVariable String card_number) {
        var resp = cardDashboardStub
                .dashboardCardNumber(com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(card_number)
                        .build());
        return ResponseEntity.ok(ApiResponse.<CardDashboardCard>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.cardDashboardCard(resp.getData()))
                .build());
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static int nvl(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private static Boolean success(String status) {
        return "success".equalsIgnoreCase(status);
    }
}

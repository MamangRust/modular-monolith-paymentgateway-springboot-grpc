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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.CreateTransactionRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.FindAllTransactionCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.FindAllTransactions;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.UpdateTransactionRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.stats.MonthStatusTransaction;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.stats.MonthYearPaymentMethod;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.statsbycard.MonthStatusTransactionCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.transaction.statsbycard.YearStatusTransactionCardNumber;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.amount.TransactionYearlyAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.method.TransactionMonthMethodResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.method.TransactionYearMethodResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.stats.status.TransactionResponseYearStatusSuccess;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.stats.TransactionStatsAmountServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.stats.TransactionStatsMethodServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.stats.TransactionStatsStatusServiceGrpc;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub transactionQueryStub;
    private final TransactionCommandServiceGrpc.TransactionCommandServiceBlockingStub transactionCommandStub;
    private final TransactionStatsAmountServiceGrpc.TransactionStatsAmountServiceBlockingStub transactionStatsAmountStub;
    private final TransactionStatsMethodServiceGrpc.TransactionStatsMethodServiceBlockingStub transactionStatsMethodStub;
    private final TransactionStatsStatusServiceGrpc.TransactionStatsStatusServiceBlockingStub transactionStatsStatusStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransactionResponse>>> findAll(
            @ModelAttribute FindAllTransactions req) {
        var resp = transactionQueryStub.findAllTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransactionResponse>>> findAllByCardNumber(
            @ModelAttribute FindAllTransactionCardNumber req) {
        var resp = transactionQueryStub.findAllTransactionByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionCardNumberRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransactionResponseDeleteAt>>> findActive(
            @ModelAttribute FindAllTransactions req) {
        var resp = transactionQueryStub.findByActiveTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransactionResponseDeleteAt>>> findTrashed(
            @ModelAttribute FindAllTransactions req) {
        var resp = transactionQueryStub.findByTrashedTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(@PathVariable Long id) {
        var resp = transactionQueryStub.findByIdTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build());
    }

    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> findByMerchantId(@PathVariable Long merchantId) {
        var resp = transactionQueryStub.findTransactionByMerchantId(
                com.sanedge.paymentgateway.pb.transaction.FindTransactionByMerchantIdRequest.newBuilder()
                        .setMerchantId(merchantId.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactions(resp.getDataList()))
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @RequestHeader("X-api-Key") String apiKey,
            @RequestBody CreateTransactionRequest req) {
        var resp = transactionCommandStub.createTransaction(
                com.sanedge.paymentgateway.pb.transaction.CreateTransactionRequest.newBuilder()
                        .setApiKey(nvl(apiKey))
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setAmount(nvl(req.getAmount()))
                        .setPaymentMethod(nvl(req.getPaymentMethod()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .setTransactionTime(ProtoMapper.toTimestamp(req.getTransactionTime()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @RequestHeader("X-api-Key") String apiKey,
            @RequestBody UpdateTransactionRequest req) {
        var resp = transactionCommandStub.updateTransaction(
                com.sanedge.paymentgateway.pb.transaction.UpdateTransactionRequest.newBuilder()
                        .setTransactionId(nvl(req.getTransactionId()))
                        .setApiKey(nvl(apiKey))
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setAmount(nvl(req.getAmount()))
                        .setPaymentMethod(nvl(req.getPaymentMethod()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .setTransactionTime(ProtoMapper.toTimestamp(req.getTransactionTime()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transaction(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponseDeleteAt>> trash(@PathVariable Long id) {
        var resp = transactionCommandStub.trashedTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransactionResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponseDeleteAt>> restore(@PathVariable Long id) {
        var resp = transactionCommandStub.restoreTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransactionResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deletePermanent(@PathVariable Long id) {
        var resp = transactionCommandStub.deleteTransactionPermanent(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/restore-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> restoreAll() {
        var resp = transactionCommandStub.restoreAllTransaction(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAll() {
        var resp = transactionCommandStub.deleteAllTransactionPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- stats: amount ----

    @GetMapping("/stats/amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionMonthAmountResponse>>> findMonthlyAmounts(
            @RequestParam Long year) {
        var resp = transactionStatsAmountStub
                .findMonthlyAmounts(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionYearlyAmountResponse>>> findYearlyAmounts(
            @RequestParam Long year) {
        var resp = transactionStatsAmountStub
                .findYearlyAmounts(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionMonthAmountResponse>>> findMonthlyAmountsByCard(
            @ModelAttribute MonthYearPaymentMethod req) {
        var resp = transactionStatsAmountStub.findMonthlyAmountsByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionYearlyAmountResponse>>> findYearlyAmountsByCard(
            @ModelAttribute MonthYearPaymentMethod req) {
        var resp = transactionStatsAmountStub.findYearlyAmountsByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: method ----

    @GetMapping("/stats/method/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionMonthMethodResponse>>> findMonthlyMethods(
            @RequestParam Long year) {
        var resp = transactionStatsMethodStub
                .findMonthlyPaymentMethods(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionMonthMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionYearMethodResponse>>> findYearlyMethods(
            @RequestParam Long year) {
        var resp = transactionStatsMethodStub
                .findYearlyPaymentMethods(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionYearMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionMonthMethodResponse>>> findMonthlyMethodsByCard(
            @ModelAttribute MonthYearPaymentMethod req) {
        var resp = transactionStatsMethodStub.findMonthlyPaymentMethodsByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionMonthMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionYearMethodResponse>>> findYearlyMethodsByCard(
            @ModelAttribute MonthYearPaymentMethod req) {
        var resp = transactionStatsMethodStub.findYearlyPaymentMethodsByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionYearMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearMethods(resp.getDataList()))
                .build());
    }

    // ---- stats: status ----

    @GetMapping("/stats/status/monthly/success")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseMonthStatusSuccess>>> findMonthlyStatusSuccess(
            @ModelAttribute MonthStatusTransaction req) {
        var resp = transactionStatsStatusStub.findMonthlyTransactionStatusSuccess(
                com.sanedge.paymentgateway.pb.transaction.FindMonthlyTransactionStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseYearStatusSuccess>>> findYearlyStatusSuccess(
            @RequestParam Long year) {
        var resp = transactionStatsStatusStub
                .findYearlyTransactionStatusSuccess(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus
                        .newBuilder().setYear(year.intValue()).build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseMonthStatusFailed>>> findMonthlyStatusFailed(
            @ModelAttribute MonthStatusTransaction req) {
        var resp = transactionStatsStatusStub.findMonthlyTransactionStatusFailed(
                com.sanedge.paymentgateway.pb.transaction.FindMonthlyTransactionStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseYearStatusFailed>>> findYearlyStatusFailed(
            @RequestParam Long year) {
        var resp = transactionStatsStatusStub
                .findYearlyTransactionStatusFailed(com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatus
                        .newBuilder().setYear(year.intValue()).build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseMonthStatusSuccess>>> findMonthlyStatusSuccessByCard(
            @ModelAttribute MonthStatusTransactionCardNumber req) {
        var resp = transactionStatsStatusStub.findMonthlyTransactionStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindMonthlyTransactionStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(
            @ModelAttribute YearStatusTransactionCardNumber req) {
        var resp = transactionStatsStatusStub.findYearlyTransactionStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseMonthStatusFailed>>> findMonthlyStatusFailedByCard(
            @ModelAttribute MonthStatusTransactionCardNumber req) {
        var resp = transactionStatsStatusStub.findMonthlyTransactionStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindMonthlyTransactionStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransactionResponseYearStatusFailed>>> findYearlyStatusFailedByCard(
            @ModelAttribute YearStatusTransactionCardNumber req) {
        var resp = transactionStatsStatusStub.findYearlyTransactionStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindYearTransactionStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransactionResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transactionYearStatusFailures(resp.getDataList()))
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

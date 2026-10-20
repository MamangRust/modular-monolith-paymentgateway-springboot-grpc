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
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.CreateMerchantRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.FindAllMerchants;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.UpdateMerchantRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyapikey.MonthYearAmountApiKey;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyapikey.MonthYearPaymentMethodApiKey;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyapikey.MonthYearTotalAmountApiKey;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyid.MonthYearAmountMerchant;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyid.MonthYearPaymentMethodMerchant;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.statsbyid.MonthYearTotalAmountMerchant;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.paymentgateway.gateway.dto.requests.merchant.transactions.FindAllMerchantTransactionsById;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantTransactionResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.method.MerchantResponseYearlyPaymentMethod;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.stats.total_amount.MerchantResponseYearlyTotalAmount;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantTransactionServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.stats.MerchantStatsAmountServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.stats.MerchantStatsMethodServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.stats.MerchantStatsTotalAmountServiceGrpc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;
    private final MerchantCommandServiceGrpc.MerchantCommandServiceBlockingStub merchantCommandStub;
    private final MerchantTransactionServiceGrpc.MerchantTransactionServiceBlockingStub merchantTransactionStub;
    private final MerchantStatsAmountServiceGrpc.MerchantStatsAmountServiceBlockingStub merchantStatsAmountStub;
    private final MerchantStatsMethodServiceGrpc.MerchantStatsMethodServiceBlockingStub merchantStatsMethodStub;
    private final MerchantStatsTotalAmountServiceGrpc.MerchantStatsTotalAmountServiceBlockingStub merchantStatsTotalAmountStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantResponse>>> findAllMerchants(
            @ModelAttribute FindAllMerchants req) {
        var resp = merchantQueryStub.findAllMerchant(com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchants(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantResponseDeleteAt>>> findActiveMerchants(
            @ModelAttribute FindAllMerchants req) {
        var resp = merchantQueryStub.findByActive(com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantResponseDeleteAt>>> findTrashedMerchants(
            @ModelAttribute FindAllMerchants req) {
        var resp = merchantQueryStub.findByTrashed(com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<MerchantResponse>> findMerchantById(@PathVariable Long id) {
        var resp = merchantQueryStub.findByIdMerchant(com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                .setMerchantId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build());
    }

    @GetMapping("/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<MerchantResponse>> findMerchantByApiKey(@RequestParam String apiKey) {
        var resp = merchantQueryStub.findByApiKey(com.sanedge.paymentgateway.pb.merchant.FindByApiKeyRequest.newBuilder()
                .setApiKey(nvl(apiKey))
                .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build());
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> findMerchantByUserId(@PathVariable Long userId) {
        var resp = merchantQueryStub.findByMerchantUserId(
                com.sanedge.paymentgateway.pb.merchant.FindByMerchantUserIdRequest.newBuilder()
                        .setUserId(userId.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchants(resp.getDataList()))
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(@Valid @RequestBody CreateMerchantRequest req) {
        var resp = merchantCommandStub.createMerchant(com.sanedge.paymentgateway.pb.merchant.CreateMerchantRequest.newBuilder()
                .setName(nvl(req.getName()))
                .setUserId(nvl(req.getUserId()))
                .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(@Valid @RequestBody UpdateMerchantRequest req) {
        var resp = merchantCommandStub.updateMerchant(com.sanedge.paymentgateway.pb.merchant.UpdateMerchantRequest.newBuilder()
                .setMerchantId(nvl(req.getMerchantId()))
                .setName(nvl(req.getName()))
                .setUserId(nvl(req.getUserId()))
                .setStatus(nvl(req.getStatus()))
                .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantResponseDeleteAt>> trashMerchant(@PathVariable Long id) {
        var resp = merchantCommandStub.trashedMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantResponseDeleteAt>> restoreMerchant(@PathVariable Long id) {
        var resp = merchantCommandStub.restoreMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<MerchantResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteMerchant(@PathVariable Long id) {
        var resp = merchantCommandStub.deleteMerchantPermanent(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/restore-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> restoreAllMerchants() {
        var resp = merchantCommandStub.restoreAllMerchant(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAllMerchants() {
        var resp = merchantCommandStub.deleteAllMerchantPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- transactions ----

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantTransactionResponse>>> findAllTransactions(
            @ModelAttribute FindAllMerchants req) {
        var resp = merchantTransactionStub.findAllTransactionMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransaction.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantTransactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/transactions/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantTransactionResponse>>> findTransactionsById(
            @ModelAttribute FindAllMerchantTransactionsById req) {
        var resp = merchantTransactionStub.findAllTransactionByMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionId.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .setId(req.getMerchantId() == null ? "" : String.valueOf(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantTransactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/transactions/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<MerchantTransactionResponse>>> findTransactionsByApiKey(
            @ModelAttribute FindAllMerchantTransactionsByApiKey req) {
        var resp = merchantTransactionStub.findAllTransactionByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionApikey.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantTransactions(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    // ---- stats: amount ----

    @GetMapping("/stats/amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyAmount>>> getMonthlyAmount(@RequestParam Long year) {
        var resp = merchantStatsAmountStub
                .findMonthlyAmountMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyAmount>>> getYearlyAmount(@RequestParam Long year) {
        var resp = merchantStatsAmountStub
                .findYearlyAmountMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyAmount>>> getMonthlyAmountById(
            @ModelAttribute MonthYearAmountMerchant req) {
        var resp = merchantStatsAmountStub.findMonthlyAmountByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyAmount>>> getYearlyAmountById(
            @ModelAttribute MonthYearAmountMerchant req) {
        var resp = merchantStatsAmountStub.findYearlyAmountByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyAmount>>> getMonthlyAmountByApiKey(
            @ModelAttribute MonthYearAmountApiKey req) {
        var resp = merchantStatsAmountStub.findMonthlyAmountByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyAmount>>> getYearlyAmountByApiKey(
            @ModelAttribute MonthYearAmountApiKey req) {
        var resp = merchantStatsAmountStub.findYearlyAmountByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: method ----

    @GetMapping("/stats/method/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> getMonthlyMethod(
            @RequestParam Long year) {
        var resp = merchantStatsMethodStub
                .findMonthlyPaymentMethodsMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> getYearlyMethod(
            @RequestParam Long year) {
        var resp = merchantStatsMethodStub
                .findYearlyPaymentMethodMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/monthly/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> getMonthlyMethodById(
            @ModelAttribute MonthYearPaymentMethodMerchant req) {
        var resp = merchantStatsMethodStub.findMonthlyPaymentMethodByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> getYearlyMethodById(
            @ModelAttribute MonthYearPaymentMethodMerchant req) {
        var resp = merchantStatsMethodStub.findYearlyPaymentMethodByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/monthly/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> getMonthlyMethodByApiKey(
            @ModelAttribute MonthYearPaymentMethodApiKey req) {
        var resp = merchantStatsMethodStub.findMonthlyPaymentMethodByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> getYearlyMethodByApiKey(
            @ModelAttribute MonthYearPaymentMethodApiKey req) {
        var resp = merchantStatsMethodStub.findYearlyPaymentMethodByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyPaymentMethod>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearMethods(resp.getDataList()))
                .build());
    }

    // ---- stats: total amount ----

    @GetMapping("/stats/total-amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> getMonthlyTotalAmount(
            @RequestParam Long year) {
        var resp = merchantStatsTotalAmountStub
                .findMonthlyTotalAmountMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthTotalAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/total-amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> getYearlyTotalAmount(
            @RequestParam Long year) {
        var resp = merchantStatsTotalAmountStub
                .findYearlyTotalAmountMerchant(com.sanedge.paymentgateway.pb.merchant.FindYearMerchant.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearTotalAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/total-amount/monthly/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> getMonthlyTotalAmountById(
            @ModelAttribute MonthYearTotalAmountMerchant req) {
        var resp = merchantStatsTotalAmountStub.findMonthlyTotalAmountByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthTotalAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/total-amount/yearly/by-id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> getYearlyTotalAmountById(
            @ModelAttribute MonthYearTotalAmountMerchant req) {
        var resp = merchantStatsTotalAmountStub.findYearlyTotalAmountByMerchants(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantById.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMerchantId(nvl(req.getMerchantId()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearTotalAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/total-amount/monthly/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> getMonthlyTotalAmountByApiKey(
            @ModelAttribute MonthYearTotalAmountApiKey req) {
        var resp = merchantStatsTotalAmountStub.findMonthlyTotalAmountByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseMonthlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantMonthTotalAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/total-amount/yearly/by-api-key")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> getYearlyTotalAmountByApiKey(
            @ModelAttribute MonthYearTotalAmountApiKey req) {
        var resp = merchantStatsTotalAmountStub.findYearlyTotalAmountByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindYearMerchantByApikey.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setApiKey(nvl(req.getApiKey()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<MerchantResponseYearlyTotalAmount>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantYearTotalAmounts(resp.getDataList()))
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

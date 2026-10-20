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
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.CreateWithdrawRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.FindAllWithdraws;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.MonthStatusWithdraw;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.UpdateWithdrawRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.statsbycard.MonthStatusWithdrawCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.statsbycard.YearMonthCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.withdraws.statsbycard.YearStatusWithdrawCardNumber;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.stats.WithdrawStatsAmountServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.stats.WithdrawStatsStatusServiceGrpc;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdraws")
public class WithdrawController {

    private final WithdrawQueryServiceGrpc.WithdrawQueryServiceBlockingStub withdrawQueryStub;
    private final WithdrawCommandServiceGrpc.WithdrawCommandServiceBlockingStub withdrawCommandStub;
    private final WithdrawStatsAmountServiceGrpc.WithdrawStatsAmountServiceBlockingStub withdrawStatsAmountStub;
    private final WithdrawStatsStatusServiceGrpc.WithdrawStatsStatusServiceBlockingStub withdrawStatsStatusStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<WithdrawResponse>>> findAll(
            @ModelAttribute FindAllWithdraws req) {
        var resp = withdrawQueryStub.findAllWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<WithdrawResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdraws(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<WithdrawResponse>> findById(@PathVariable Long id) {
        var resp = withdrawQueryStub.findByIdWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<WithdrawResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdraw(resp.getData()))
                .build());
    }

    @GetMapping("/by-card/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<WithdrawResponse>>> findByCard(@PathVariable String cardNumber) {
        var resp = withdrawQueryStub.findByCardNumber(
                com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(nvl(cardNumber))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdraws(resp.getDataList()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<WithdrawResponseDeleteAt>>> findActive(
            @ModelAttribute FindAllWithdraws req) {
        var resp = withdrawQueryStub.findByActive(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<WithdrawResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponsePagination<List<WithdrawResponseDeleteAt>>> findTrashed(
            @ModelAttribute FindAllWithdraws req) {
        var resp = withdrawQueryStub.findByTrashed(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<WithdrawResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<WithdrawResponse>> create(@RequestBody CreateWithdrawRequest req) {
        var resp = withdrawCommandStub.createWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.CreateWithdrawRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setWithdrawAmount(nvl(req.getWithdrawAmount()))
                        .setWithdrawTime(ProtoMapper.toTimestamp(req.getWithdrawTime()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<WithdrawResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdraw(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<WithdrawResponse>> update(@RequestBody UpdateWithdrawRequest req) {
        var resp = withdrawCommandStub.updateWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.UpdateWithdrawRequest.newBuilder()
                        .setWithdrawId(nvl(req.getWithdrawId()))
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setWithdrawAmount(nvl(req.getWithdrawAmount()))
                        .setWithdrawTime(ProtoMapper.toTimestamp(req.getWithdrawTime()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<WithdrawResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdraw(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<WithdrawResponseDeleteAt>> trash(@PathVariable Long id) {
        var resp = withdrawCommandStub.trashedWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<WithdrawResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<WithdrawResponseDeleteAt>> restore(@PathVariable Long id) {
        var resp = withdrawCommandStub.restoreWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<WithdrawResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deletePermanent(@PathVariable Long id) {
        var resp = withdrawCommandStub.deleteWithdrawPermanent(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id.intValue())
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
        var resp = withdrawCommandStub.restoreAllWithdraw(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAll() {
        var resp = withdrawCommandStub.deleteAllWithdrawPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- stats: amount ----

    @GetMapping("/stats/amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyAmounts(
            @RequestParam Long year) {
        var resp = withdrawStatsAmountStub
                .findMonthlyWithdraws(com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawMonthlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyAmounts(@RequestParam Long year) {
        var resp = withdrawStatsAmountStub
                .findYearlyWithdraws(com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyByCard(
            @ModelAttribute YearMonthCardNumber req) {
        var resp = withdrawStatsAmountStub.findMonthlyWithdrawsByCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawMonthlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyByCard(
            @ModelAttribute YearMonthCardNumber req) {
        var resp = withdrawStatsAmountStub.findYearlyWithdrawsByCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: status ----

    @GetMapping("/stats/status/monthly/success")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthlyStatusSuccess(
            @ModelAttribute MonthStatusWithdraw req) {
        var resp = withdrawStatsStatusStub.findMonthlyWithdrawStatusSuccess(
                com.sanedge.paymentgateway.pb.withdraw.FindMonthlyWithdrawStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccess(
            @RequestParam Long year) {
        var resp = withdrawStatsStatusStub
                .findYearlyWithdrawStatusSuccess(com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthlyStatusFailed(
            @ModelAttribute MonthStatusWithdraw req) {
        var resp = withdrawStatsStatusStub.findMonthlyWithdrawStatusFailed(
                com.sanedge.paymentgateway.pb.withdraw.FindMonthlyWithdrawStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailed(
            @RequestParam Long year) {
        var resp = withdrawStatsStatusStub
                .findYearlyWithdrawStatusFailed(com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthlyStatusSuccessByCard(
            @ModelAttribute MonthStatusWithdrawCardNumber req) {
        var resp = withdrawStatsStatusStub.findMonthlyWithdrawStatusSuccessCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindMonthlyWithdrawStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(
            @ModelAttribute YearStatusWithdrawCardNumber req) {
        var resp = withdrawStatsStatusStub.findYearlyWithdrawStatusSuccessCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthlyStatusFailedByCard(
            @ModelAttribute MonthStatusWithdrawCardNumber req) {
        var resp = withdrawStatsStatusStub.findMonthlyWithdrawStatusFailedCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindMonthlyWithdrawStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailedByCard(
            @ModelAttribute YearStatusWithdrawCardNumber req) {
        var resp = withdrawStatsStatusStub.findYearlyWithdrawStatusFailedCardNumber(
                com.sanedge.paymentgateway.pb.withdraw.FindYearWithdrawStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<WithdrawResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.withdrawYearStatusFailures(resp.getDataList()))
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

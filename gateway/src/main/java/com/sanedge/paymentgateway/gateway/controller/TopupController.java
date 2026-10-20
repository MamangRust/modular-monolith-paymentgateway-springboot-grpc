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
import com.sanedge.paymentgateway.gateway.dto.requests.topup.CreateTopupRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.FindAllTopups;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.FindAllTopupsByCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.UpdateTopupRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.stats.MonthTopupStatus;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.stats.YearMonthMethod;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.statsbycard.MonthTopupStatusCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.topup.statsbycard.YearTopupStatusCardNumber;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.amount.TopupMonthAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.amount.TopupYearlyAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.method.TopupMonthMethodResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.method.TopupYearlyMethodResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.topup.TopupCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.stats.TopupStatsAmountServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.stats.TopupStatsMethodServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.stats.TopupStatsStatusServiceGrpc;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topups")
public class TopupController {

    private final TopupQueryServiceGrpc.TopupQueryServiceBlockingStub topupQueryStub;
    private final TopupCommandServiceGrpc.TopupCommandServiceBlockingStub topupCommandStub;
    private final TopupStatsAmountServiceGrpc.TopupStatsAmountServiceBlockingStub topupStatsAmountStub;
    private final TopupStatsMethodServiceGrpc.TopupStatsMethodServiceBlockingStub topupStatsMethodStub;
    private final TopupStatsStatusServiceGrpc.TopupStatsStatusServiceBlockingStub topupStatsStatusStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TopupResponse>>> findAll(@ModelAttribute FindAllTopups req) {
        var resp = topupQueryStub.findAllTopup(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TopupResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topups(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TopupResponse>>> findAllByCardNumber(
            @ModelAttribute FindAllTopupsByCardNumber req) {
        var resp = topupQueryStub.findAllTopupByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindAllTopupByCardNumberRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TopupResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topups(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TopupResponseDeleteAt>>> findActive(
            @ModelAttribute FindAllTopups req) {
        var resp = topupQueryStub.findByActive(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TopupResponseDeleteAt>>> findTrashed(
            @ModelAttribute FindAllTopups req) {
        var resp = topupQueryStub.findByTrashed(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(nvl(req.getPage()))
                .setPageSize(nvl(req.getPageSize()))
                .setSearch(nvl(req.getSearch()))
                .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF', 'USER')")
    public ResponseEntity<ApiResponse<TopupResponse>> findById(@PathVariable Long id) {
        var resp = topupQueryStub.findByIdTopup(com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<TopupResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topup(resp.getData()))
                .build());
    }

    @GetMapping("/card/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<List<TopupResponse>>> findByCard(@PathVariable String cardNumber) {
        var resp = topupQueryStub.findByCardNumberTopup(
                com.sanedge.paymentgateway.pb.topup.FindByCardNumberTopupRequest.newBuilder()
                        .setCardNumber(nvl(cardNumber))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.hasData() ? List.of(ProtoMapper.topup(resp.getData())) : List.of())
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<TopupResponse>> create(@RequestBody CreateTopupRequest req) {
        var resp = topupCommandStub.createTopup(com.sanedge.paymentgateway.pb.topup.CreateTopupRequest.newBuilder()
                .setCardNumber(nvl(req.getCardNumber()))
                .setTopupNo("")
                .setTopupAmount(nvl(req.getTopupAmount()))
                .setTopupMethod(nvl(req.getTopupMethod()))
                .build());
        return ResponseEntity.ok(ApiResponse.<TopupResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topup(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<ApiResponse<TopupResponse>> update(@RequestBody UpdateTopupRequest req) {
        var resp = topupCommandStub.updateTopup(com.sanedge.paymentgateway.pb.topup.UpdateTopupRequest.newBuilder()
                .setTopupId(nvl(req.getTopupId()))
                .setCardNumber(nvl(req.getCardNumber()))
                .setTopupAmount(nvl(req.getTopupAmount()))
                .setTopupMethod(nvl(req.getTopupMethod()))
                .build());
        return ResponseEntity.ok(ApiResponse.<TopupResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topup(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopupResponseDeleteAt>> trash(@PathVariable Long id) {
        var resp = topupCommandStub.trashedTopup(com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<TopupResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopupResponseDeleteAt>> restore(@PathVariable Long id) {
        var resp = topupCommandStub.restoreTopup(com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id.intValue())
                .build());
        return ResponseEntity.ok(ApiResponse.<TopupResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deletePermanent(@PathVariable Long id) {
        var resp = topupCommandStub.deleteTopupPermanent(com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id.intValue())
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
        var resp = topupCommandStub.restoreAllTopup(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAll() {
        var resp = topupCommandStub.deleteAllTopupPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- stats: amount ----

    @GetMapping("/stats/amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupMonthAmountResponse>>> getMonthlyAmounts(@RequestParam Long year) {
        var resp = topupStatsAmountStub
                .findMonthlyTopupAmounts(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupYearlyAmountResponse>>> getYearlyAmounts(@RequestParam Long year) {
        var resp = topupStatsAmountStub
                .findYearlyTopupAmounts(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupMonthAmountResponse>>> getMonthlyAmountsByCard(
            @ModelAttribute YearMonthMethod req) {
        var resp = topupStatsAmountStub.findMonthlyTopupAmountsByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupYearlyAmountResponse>>> getYearlyAmountsByCard(
            @ModelAttribute YearMonthMethod req) {
        var resp = topupStatsAmountStub.findYearlyTopupAmountsByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupYearlyAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: method ----

    @GetMapping("/stats/method/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupMonthMethodResponse>>> getMonthlyMethods(@RequestParam Long year) {
        var resp = topupStatsMethodStub
                .findMonthlyTopupMethods(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupMonthMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupYearlyMethodResponse>>> getYearlyMethods(@RequestParam Long year) {
        var resp = topupStatsMethodStub
                .findYearlyTopupMethods(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/monthly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupMonthMethodResponse>>> getMonthlyMethodsByCard(
            @ModelAttribute YearMonthMethod req) {
        var resp = topupStatsMethodStub.findMonthlyTopupMethodsByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupMonthMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthMethods(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/method/yearly/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupYearlyMethodResponse>>> getYearlyMethodsByCard(
            @ModelAttribute YearMonthMethod req) {
        var resp = topupStatsMethodStub.findYearlyTopupMethodsByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupYearlyMethodResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearMethods(resp.getDataList()))
                .build());
    }

    // ---- stats: status ----

    @GetMapping("/stats/status/monthly/success")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseMonthStatusSuccess>>> getMonthlyStatusSuccess(
            @ModelAttribute MonthTopupStatus req) {
        var resp = topupStatsStatusStub.findMonthlyTopupStatusSuccess(
                com.sanedge.paymentgateway.pb.topup.FindMonthlyTopupStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseYearStatusSuccess>>> getYearlyStatusSuccess(
            @RequestParam Long year) {
        var resp = topupStatsStatusStub
                .findYearlyTopupStatusSuccess(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseMonthStatusFailed>>> getMonthlyStatusFailed(
            @ModelAttribute MonthTopupStatus req) {
        var resp = topupStatsStatusStub.findMonthlyTopupStatusFailed(
                com.sanedge.paymentgateway.pb.topup.FindMonthlyTopupStatus.newBuilder()
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseYearStatusFailed>>> getYearlyStatusFailed(
            @RequestParam Long year) {
        var resp = topupStatsStatusStub
                .findYearlyTopupStatusFailed(com.sanedge.paymentgateway.pb.topup.FindYearTopupStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseMonthStatusSuccess>>> getMonthlyStatusSuccessByCard(
            @ModelAttribute MonthTopupStatusCardNumber req) {
        var resp = topupStatsStatusStub.findMonthlyTopupStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindMonthlyTopupStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseYearStatusSuccess>>> getYearlyStatusSuccessByCard(
            @ModelAttribute YearTopupStatusCardNumber req) {
        var resp = topupStatsStatusStub.findYearlyTopupStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseMonthStatusFailed>>> getMonthlyStatusFailedByCard(
            @ModelAttribute MonthTopupStatusCardNumber req) {
        var resp = topupStatsStatusStub.findMonthlyTopupStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindMonthlyTopupStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<TopupResponseYearStatusFailed>>> getYearlyStatusFailedByCard(
            @ModelAttribute YearTopupStatusCardNumber req) {
        var resp = topupStatsStatusStub.findYearlyTopupStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindYearTopupStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(nvl(req.getYear()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TopupResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.topupYearStatusFailures(resp.getDataList()))
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

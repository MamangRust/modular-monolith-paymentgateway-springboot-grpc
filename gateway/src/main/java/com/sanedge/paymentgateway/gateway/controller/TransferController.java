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
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.CreateTransferRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.FindAllTransfers;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.UpdateTransferRequest;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.stats.MonthStatusTransfer;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.statsbycard.MonthYearCardNumber;
import com.sanedge.paymentgateway.gateway.dto.requests.transfers.statsbycard.YearStatusTransferCardNumber;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.amount.TransferMonthAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.amount.TransferYearAmountResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.transfer.TransferCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.stats.TransferStatsAmountServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.stats.TransferStatsStatusServiceGrpc;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferQueryServiceGrpc.TransferQueryServiceBlockingStub transferQueryStub;
    private final TransferCommandServiceGrpc.TransferCommandServiceBlockingStub transferCommandStub;
    private final TransferStatsAmountServiceGrpc.TransferStatsAmountServiceBlockingStub transferStatsAmountStub;
    private final TransferStatsStatusServiceGrpc.TransferStatsStatusServiceBlockingStub transferStatsStatusStub;

    // ---- CRUD ----

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransferResponse>>> findAll(
            @ModelAttribute FindAllTransfers req) {
        var resp = transferQueryStub.findAllTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransferResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfers(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<TransferResponse>> findById(@PathVariable Long id) {
        var resp = transferQueryStub.findByIdTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransferResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfer(resp.getData()))
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransferResponseDeleteAt>>> findActive(
            @ModelAttribute FindAllTransfers req) {
        var resp = transferQueryStub.findByActiveTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/trashed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponsePagination<List<TransferResponseDeleteAt>>> findTrashed(
            @ModelAttribute FindAllTransfers req) {
        var resp = transferQueryStub.findByTrashedTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(nvl(req.getPage()))
                        .setPageSize(nvl(req.getPageSize()))
                        .setSearch(nvl(req.getSearch()))
                        .build());
        return ResponseEntity.ok(ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build());
    }

    @GetMapping("/from/{transferFrom}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> findByTransferFrom(
            @PathVariable String transferFrom) {
        var resp = transferQueryStub.findTransferByTransferFrom(
                com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferFromRequest.newBuilder()
                        .setTransferFrom(nvl(transferFrom))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfers(resp.getDataList()))
                .build());
    }

    @GetMapping("/to/{transferTo}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> findByTransferTo(@PathVariable String transferTo) {
        var resp = transferQueryStub.findTransferByTransferTo(
                com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferToRequest.newBuilder()
                        .setTransferTo(nvl(transferTo))
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfers(resp.getDataList()))
                .build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF', 'USER')")
    public ResponseEntity<ApiResponse<TransferResponse>> create(@RequestBody CreateTransferRequest req) {
        var resp = transferCommandStub.createTransfer(
                com.sanedge.paymentgateway.pb.transfer.CreateTransferRequest.newBuilder()
                        .setTransferFrom(nvl(req.getTransferFrom()))
                        .setTransferTo(nvl(req.getTransferTo()))
                        .setTransferAmount(nvl(req.getTransferAmount()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransferResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfer(resp.getData()))
                .build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<TransferResponse>> update(@RequestBody UpdateTransferRequest req) {
        var resp = transferCommandStub.updateTransfer(
                com.sanedge.paymentgateway.pb.transfer.UpdateTransferRequest.newBuilder()
                        .setTransferId(nvl(req.getTransferId()))
                        .setTransferFrom(nvl(req.getTransferFrom()))
                        .setTransferTo(nvl(req.getTransferTo()))
                        .setTransferAmount(nvl(req.getTransferAmount()))
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransferResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transfer(resp.getData()))
                .build());
    }

    @PostMapping("/trash/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<TransferResponseDeleteAt>> trash(@PathVariable Long id) {
        var resp = transferCommandStub.trashedTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransferResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferDeleteAt(resp.getData()))
                .build());
    }

    @PostMapping("/restore/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<TransferResponseDeleteAt>> restore(@PathVariable Long id) {
        var resp = transferCommandStub.restoreTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<TransferResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferDeleteAt(resp.getData()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Boolean>> deletePermanent(@PathVariable Long id) {
        var resp = transferCommandStub.deleteTransferPermanent(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id.intValue())
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
        var resp = transferCommandStub.restoreAllTransfer(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    @PostMapping("/delete-all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAll() {
        var resp = transferCommandStub.deleteAllTransferPermanent(Empty.getDefaultInstance());
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(success(resp.getStatus()))
                .build());
    }

    // ---- stats: amount ----

    @GetMapping("/stats/amount/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmounts(
            @RequestParam Long year) {
        var resp = transferStatsAmountStub
                .findMonthlyTransferAmounts(com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmounts(@RequestParam Long year) {
        var resp = transferStatsAmountStub
                .findYearlyTransferAmounts(com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferYearAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/monthly/by-card/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsBySender(
            @ModelAttribute MonthYearCardNumber req) {
        var resp = transferStatsAmountStub.findMonthlyTransferAmountsBySenderCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/monthly/by-card/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsByReceiver(
            @ModelAttribute MonthYearCardNumber req) {
        var resp = transferStatsAmountStub.findMonthlyTransferAmountsByReceiverCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferMonthAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-card/sender")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsBySender(
            @ModelAttribute MonthYearCardNumber req) {
        var resp = transferStatsAmountStub.findYearlyTransferAmountsBySenderCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferYearAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearAmounts(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/amount/yearly/by-card/receiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsByReceiver(
            @ModelAttribute MonthYearCardNumber req) {
        var resp = transferStatsAmountStub.findYearlyTransferAmountsByReceiverCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferYearAmountResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearAmounts(resp.getDataList()))
                .build());
    }

    // ---- stats: status ----

    @GetMapping("/stats/status/monthly/success")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthlyStatusSuccess(
            @ModelAttribute MonthStatusTransfer req) {
        var resp = transferStatsStatusStub.findMonthlyTransferStatusSuccess(
                com.sanedge.paymentgateway.pb.transfer.FindMonthlyTransferStatus.newBuilder()
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccess(
            @RequestParam Long year) {
        var resp = transferStatsStatusStub
                .findYearlyTransferStatusSuccess(com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthlyStatusFailed(
            @ModelAttribute MonthStatusTransfer req) {
        var resp = transferStatsStatusStub.findMonthlyTransferStatusFailed(
                com.sanedge.paymentgateway.pb.transfer.FindMonthlyTransferStatus.newBuilder()
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailed(
            @RequestParam Long year) {
        var resp = transferStatsStatusStub
                .findYearlyTransferStatusFailed(com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year.intValue())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthlyStatusSuccessByCard(
            @ModelAttribute MonthStatusTransferCardNumber req) {
        var resp = transferStatsStatusStub.findMonthlyTransferStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindMonthlyTransferStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseMonthStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/success/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(
            @ModelAttribute YearStatusTransferCardNumber req) {
        var resp = transferStatsStatusStub.findYearlyTransferStatusSuccessByCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseYearStatusSuccess>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearStatusSuccesses(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/monthly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthlyStatusFailedByCard(
            @ModelAttribute MonthStatusTransferCardNumber req) {
        var resp = transferStatsStatusStub.findMonthlyTransferStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindMonthlyTransferStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .setMonth(req.getMonth())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseMonthStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferMonthStatusFailures(resp.getDataList()))
                .build());
    }

    @GetMapping("/stats/status/yearly/failed/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailedByCard(
            @ModelAttribute YearStatusTransferCardNumber req) {
        var resp = transferStatsStatusStub.findYearlyTransferStatusFailedByCardNumber(
                com.sanedge.paymentgateway.pb.transfer.FindYearTransferStatusCardNumber.newBuilder()
                        .setCardNumber(nvl(req.getCardNumber()))
                        .setYear(req.getYear())
                        .build());
        return ResponseEntity.ok(ApiResponse.<List<TransferResponseYearStatusFailed>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.transferYearStatusFailures(resp.getDataList()))
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

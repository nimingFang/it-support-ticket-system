package com.codelogium.ticketing.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import com.codelogium.ticketing.common.Result;
import com.codelogium.ticketing.dto.TicketInfoUpdateDTO;
import com.codelogium.ticketing.dto.TicketStatusUpdateDTO;
import com.codelogium.ticketing.dto.request.TicketCreateRequestDTO;
import com.codelogium.ticketing.dto.response.TicketResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;
import com.codelogium.ticketing.entity.enums.Status;
import com.codelogium.ticketing.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@EnableMethodSecurity
@AllArgsConstructor
@Tag(name = "Ticket Controller", description = "工单管理")
@RequestMapping(value = "/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "创建工单")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<TicketResponseDTO> createTicket(@RequestBody @Valid TicketCreateRequestDTO dto) {
        return Result.success(ticketService.createTicket(dto));
    }

    @Operation(summary = "查询工单详情")
    @GetMapping("/{ticketId}")
    public Result<TicketResponseDTO> retrieveTicket(@PathVariable Long ticketId) {
        return Result.success(ticketService.retrieveTicket(ticketId));
    }

    @Operation(summary = "更新工单信息")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PatchMapping("/{ticketId}/info")
    public Result<TicketResponseDTO> updateTicketInfo(@PathVariable Long ticketId,
                                                       @RequestBody @Valid TicketInfoUpdateDTO dto) {
        return Result.success(ticketService.updateTicketInfo(ticketId, dto));
    }

    @Operation(summary = "IT 接单（NEW → ASSIGNED）")
    @PreAuthorize("hasAuthority('IT_SUPPORT')")
    @PostMapping("/{ticketId}/assign")
    public Result<TicketResponseDTO> assignTicket(@PathVariable Long ticketId) {
        return Result.success(ticketService.assignTicket(ticketId));
    }

    @Operation(summary = "更新工单状态（严格 FSM 流转校验）")
    @PatchMapping("/{ticketId}/status")
    public Result<TicketResponseDTO> updateTicketStatus(@PathVariable Long ticketId,
                                                         @RequestBody @Valid TicketStatusUpdateDTO dto) {
        return Result.success(ticketService.updateTicketStatus(ticketId, dto));
    }

    @Operation(summary = "工单列表（动态筛选 + 分页 + RBAC数据隔离）")
    @GetMapping
    public Result<Page<TicketResponseDTO>> searchTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        return Result.success(ticketService.searchTickets(status, priority, category, keyword, pageable));
    }

    @Operation(summary = "按状态搜索工单")
    @GetMapping("/{ticketId}/search")
    public Result<TicketResponseDTO> searchByIdAndStatus(@PathVariable Long ticketId,
                                                          @RequestParam Status status) {
        return Result.success(ticketService.searchTicket(ticketId, status));
    }

    @Operation(summary = "工单操作日志")
    @PreAuthorize("hasAuthority('IT_SUPPORT')")
    @GetMapping("/{ticketId}/audit-logs")
    public Result<List<AuditLog>> retrieveAuditLogs(@PathVariable Long ticketId) {
        return Result.success(ticketService.retrieveAuditLogs(ticketId));
    }

    @Operation(summary = "删除工单")
    @DeleteMapping("/{ticketId}")
    public Result<?> removeTicket(@PathVariable Long ticketId) {
        ticketService.removeTicket(ticketId);
        return Result.success("删除成功");
    }
}

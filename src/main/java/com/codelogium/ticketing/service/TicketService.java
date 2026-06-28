package com.codelogium.ticketing.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.codelogium.ticketing.dto.TicketInfoUpdateDTO;
import com.codelogium.ticketing.dto.TicketStatusUpdateDTO;
import com.codelogium.ticketing.dto.request.TicketCreateRequestDTO;
import com.codelogium.ticketing.dto.response.TicketResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;
import com.codelogium.ticketing.entity.enums.Status;

public interface TicketService {
    TicketResponseDTO createTicket(TicketCreateRequestDTO dto);
    TicketResponseDTO updateTicketInfo(Long ticketId, TicketInfoUpdateDTO dto);
    TicketResponseDTO assignTicket(Long ticketId);
    TicketResponseDTO updateTicketStatus(Long ticketId, TicketStatusUpdateDTO dto);
    TicketResponseDTO retrieveTicket(Long ticketId);
    void removeTicket(Long ticketId);
    List<TicketResponseDTO> retrieveTicketsByCreator();

    /** 动态条件 + 分页 + 排序查询工单（自动按角色做数据隔离） */
    Page<TicketResponseDTO> searchTickets(String status, String priority,
                                           String category, String keyword, Pageable pageable);

    TicketResponseDTO searchTicket(Long ticketId, Status status);
    List<AuditLog> retrieveAuditLogs(Long ticketId);

}

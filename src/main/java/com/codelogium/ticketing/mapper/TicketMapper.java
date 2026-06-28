package com.codelogium.ticketing.mapper;

import org.springframework.stereotype.Component;

import com.codelogium.ticketing.dto.response.TicketResponseDTO;
import com.codelogium.ticketing.entity.Ticket;

@Component
public class TicketMapper {

    /** Ticket Entity → ResponseDTO（平铺 creator 信息，切断嵌套） */
    public TicketResponseDTO toResponseDTO(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setCategory(ticket.getCategory());
        dto.setCreationDate(ticket.getCreationDate());

        if (ticket.getCreator() != null) {
            dto.setCreatorId(ticket.getCreator().getId());
            dto.setCreatorUsername(ticket.getCreator().getUsername());
        }
        if (ticket.getAssignedTo() != null) {
            dto.setAssigneeId(ticket.getAssignedTo().getId());
            dto.setAssigneeUsername(ticket.getAssignedTo().getUsername());
        }
        return dto;
    }
}

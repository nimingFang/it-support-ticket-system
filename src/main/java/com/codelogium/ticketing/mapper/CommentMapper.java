package com.codelogium.ticketing.mapper;

import org.springframework.stereotype.Component;

import com.codelogium.ticketing.dto.response.CommentResponseDTO;
import com.codelogium.ticketing.entity.Comment;

@Component
public class CommentMapper {

    /** Comment Entity → ResponseDTO（平铺 author 和 ticket 信息） */
    public CommentResponseDTO toResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getAuthor() != null) {
            dto.setAuthorId(comment.getAuthor().getId());
            dto.setAuthorUsername(comment.getAuthor().getUsername());
        }
        if (comment.getTicket() != null) {
            dto.setTicketId(comment.getTicket().getId());
        }
        return dto;
    }
}

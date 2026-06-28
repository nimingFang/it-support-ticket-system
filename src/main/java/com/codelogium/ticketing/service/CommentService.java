package com.codelogium.ticketing.service;

import java.util.List;

import com.codelogium.ticketing.dto.request.CommentCreateRequestDTO;
import com.codelogium.ticketing.dto.response.CommentResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;

public interface CommentService {
    CommentResponseDTO createComment(Long ticketId, Long userId, CommentCreateRequestDTO dto);
    CommentResponseDTO updateComment(Long commentId, Long ticketId, Long userId, CommentCreateRequestDTO dto);
    CommentResponseDTO retrieveComment(Long userId, Long ticketId, Long commentId);
    void removeComment(Long commentId, Long ticketId, Long userId);
    List<AuditLog> retrieveAuditLogs(Long commentId, Long ticketId, Long userId);
}

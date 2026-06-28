package com.codelogium.ticketing.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.codelogium.ticketing.dto.request.CommentCreateRequestDTO;
import com.codelogium.ticketing.dto.response.CommentResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;
import com.codelogium.ticketing.entity.Comment;
import com.codelogium.ticketing.entity.Ticket;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.exception.ResourceNotFoundException;
import com.codelogium.ticketing.mapper.CommentMapper;
import com.codelogium.ticketing.repository.AuditLogRepository;
import com.codelogium.ticketing.repository.CommentRepository;
import com.codelogium.ticketing.repository.TicketRepository;
import com.codelogium.ticketing.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommentServiceImp implements CommentService {

    private CommentRepository commentRepository;
    private TicketRepository ticketRepository;
    private AuditLogRepository auditLogRepository;
    private UserRepository userRepository;
    private CommentMapper commentMapper;

    @Override
    public CommentResponseDTO createComment(Long ticketId, Long userId, CommentCreateRequestDTO dto) {
        User author = UserServiceImp.unwrapUser(userId, userRepository.findById(userId));
        Ticket ticket = TicketServiceImp.unwrapTicket(ticketId,
                ticketRepository.findByIdAndCreatorId(ticketId, userId));

        // DTO → Entity
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setCreatedAt(Instant.now());
        Comment saved = commentRepository.save(comment);

        auditLogRepository.save(new AuditLog(
                ticket.getId(), ticketId, saved.getId(), userId,
                "COMMENT_ADDED", null, saved.getContent(), Instant.now()));
        auditLogRepository.flush();

        return commentMapper.toResponseDTO(saved);
    }

    @Transactional
    @Override
    public CommentResponseDTO updateComment(Long commentId, Long ticketId, Long userId,
                                             CommentCreateRequestDTO dto) {
        validateUser(userId);
        Ticket ticket = TicketServiceImp.unwrapTicket(ticketId,
                ticketRepository.findByIdAndCreatorId(ticketId, userId));
        Comment retrievedComment = unwrapComment(commentId,
                commentRepository.findByIdAndTicketIdAndAuthorId(commentId, ticketId, userId));

        String oldContent = retrievedComment.getContent();
        // Bug 修复：只有内容真正变化时才更新
        if (!dto.getContent().equals(oldContent)) {
            retrievedComment.setContent(dto.getContent());
            retrievedComment.setCreatedAt(Instant.now());
            commentRepository.save(retrievedComment);

            auditLogRepository.save(new AuditLog(
                    null, ticket.getId(), retrievedComment.getId(), userId,
                    "COMMENT_UPDATED", oldContent, retrievedComment.getContent(), Instant.now()));
            auditLogRepository.flush();
        }
        return commentMapper.toResponseDTO(retrievedComment);
    }

    @Override
    public CommentResponseDTO retrieveComment(Long userId, Long ticketId, Long commentId) {
        validateUser(userId);
        TicketServiceImp.unwrapTicket(ticketId,
                ticketRepository.findByIdAndCreatorId(ticketId, userId));
        Comment comment = unwrapComment(commentId,
                commentRepository.findByIdAndTicketIdAndAuthorId(commentId, ticketId, userId));
        return commentMapper.toResponseDTO(comment);
    }

    @Override
    public List<AuditLog> retrieveAuditLogs(Long commentId, Long ticketId, Long userId) {
        validateUser(userId);
        TicketServiceImp.unwrapTicket(ticketId,
                ticketRepository.findByIdAndCreatorId(ticketId, userId));
        unwrapComment(commentId,
                commentRepository.findByIdAndTicketIdAndAuthorId(commentId, ticketId, userId));
        return auditLogRepository.findByCommentId(commentId);
    }

    @Override
    public void removeComment(Long commentId, Long ticketId, Long userId) {
        validateUser(userId);
        Ticket ticket = TicketServiceImp.unwrapTicket(ticketId,
                ticketRepository.findByIdAndCreatorId(ticketId, userId));
        Comment comment = unwrapComment(commentId,
                commentRepository.findByIdAndTicketIdAndAuthorId(commentId, ticketId, userId));

        ticket.getComments().remove(comment);
        ticketRepository.save(ticket);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException(userId, User.class);
    }

    public static Comment unwrapComment(Long commentId, Optional<Comment> optionalComment) {
        return optionalComment.orElseThrow(() -> new ResourceNotFoundException(commentId, Comment.class));
    }
}

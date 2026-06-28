package com.codelogium.ticketing.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.codelogium.ticketing.common.Result;
import com.codelogium.ticketing.dto.request.CommentCreateRequestDTO;
import com.codelogium.ticketing.dto.response.CommentResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;
import com.codelogium.ticketing.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Tag(name = "Comment Controller", description = "评论管理")
@RequestMapping(value = "/users/{userId}/tickets/{ticketId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CommentResponseDTO> createComment(@PathVariable Long ticketId,
                                                      @PathVariable Long userId,
                                                      @RequestBody @Valid CommentCreateRequestDTO dto) {
        return Result.success(commentService.createComment(ticketId, userId, dto));
    }

    @Operation(summary = "修改评论")
    @PatchMapping("/{commentId}")
    public Result<CommentResponseDTO> updateComment(@PathVariable Long commentId,
                                                      @PathVariable Long ticketId,
                                                      @PathVariable Long userId,
                                                      @RequestBody @Valid CommentCreateRequestDTO dto) {
        return Result.success(commentService.updateComment(commentId, ticketId, userId, dto));
    }

    @Operation(summary = "查询评论")
    @GetMapping("/{commentId}")
    public Result<CommentResponseDTO> retrieveComment(@PathVariable Long commentId,
                                                        @PathVariable Long ticketId,
                                                        @PathVariable Long userId) {
        return Result.success(commentService.retrieveComment(userId, ticketId, commentId));
    }

    @Operation(summary = "评论操作日志")
    @GetMapping("/{commentId}/audit-logs")
    public Result<List<AuditLog>> retrieveAuditLogs(@PathVariable Long commentId,
                                                      @PathVariable Long ticketId,
                                                      @PathVariable Long userId) {
        return Result.success(commentService.retrieveAuditLogs(commentId, ticketId, userId));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<?> removeComment(@PathVariable Long commentId,
                                     @PathVariable Long ticketId,
                                     @PathVariable Long userId) {
        commentService.removeComment(commentId, ticketId, userId);
        return Result.success("删除成功");
    }
}

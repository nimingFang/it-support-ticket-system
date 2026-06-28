package com.codelogium.ticketing.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CommentResponseDTO {

    private Long id;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    /** 平铺关联信息，不嵌套 User / Ticket 对象 */
    private Long authorId;
    private String authorUsername;
    private Long ticketId;
}

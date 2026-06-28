package com.codelogium.ticketing.dto.response;

import java.time.Instant;

import com.codelogium.ticketing.entity.enums.Category;
import com.codelogium.ticketing.entity.enums.Priority;
import com.codelogium.ticketing.entity.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TicketResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Category category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant creationDate;

    /** 平铺 Creator 信息，不嵌套 User 对象 */
    private Long creatorId;
    private String creatorUsername;

    /** 平铺 Assignee 信息（接单后才有值） */
    private Long assigneeId;
    private String assigneeUsername;
}

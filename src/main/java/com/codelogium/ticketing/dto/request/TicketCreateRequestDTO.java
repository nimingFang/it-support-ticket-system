package com.codelogium.ticketing.dto.request;

import com.codelogium.ticketing.entity.enums.Category;
import com.codelogium.ticketing.entity.enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequestDTO {

    @NotBlank(message = "工单标题不能为空")
    private String title;

    @NotBlank(message = "工单描述不能为空")
    private String description;

    @NotNull(message = "工单分类不能为空")
    private Category category;

    @NotNull(message = "优先级不能为空")
    private Priority priority;
}

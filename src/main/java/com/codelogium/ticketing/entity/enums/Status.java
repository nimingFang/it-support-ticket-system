package com.codelogium.ticketing.entity.enums;

public enum Status {
    NEW,           // 新建待分配
    ASSIGNED,      // 已指派 IT 人员
    IN_PROGRESS,   // IT 人员处理中
    RESOLVED,      // IT 人员标记已解决
    CLOSED         // 员工验收关闭
}

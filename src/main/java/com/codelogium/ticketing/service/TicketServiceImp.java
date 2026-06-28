package com.codelogium.ticketing.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.codelogium.ticketing.dto.TicketInfoUpdateDTO;
import com.codelogium.ticketing.dto.TicketStatusUpdateDTO;
import com.codelogium.ticketing.dto.request.TicketCreateRequestDTO;
import com.codelogium.ticketing.dto.response.TicketResponseDTO;
import com.codelogium.ticketing.entity.AuditLog;
import com.codelogium.ticketing.entity.Ticket;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.Category;
import com.codelogium.ticketing.entity.enums.Priority;
import com.codelogium.ticketing.entity.enums.Status;
import com.codelogium.ticketing.exception.ResourceNotFoundException;
import com.codelogium.ticketing.mapper.TicketMapper;
import com.codelogium.ticketing.repository.AuditLogRepository;
import com.codelogium.ticketing.repository.TicketRepository;
import com.codelogium.ticketing.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import static com.codelogium.ticketing.util.EntityUtils.*;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TicketServiceImp implements TicketService {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private AuditLogRepository auditLogRepository;
    private TicketMapper ticketMapper;

    // ──────────────────── 安全上下文 ────────────────────

    /** 从 SecurityContext 获取当前登录用户 */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
    }

    /** IT_SUPPORT 或 ADMIN 均具有管理权限 */
    private boolean isSupportOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("IT_SUPPORT")
                          || a.getAuthority().equals("ADMIN"));
    }

    // ──────────────────── 业务方法 ────────────────────

    @Override
    public TicketResponseDTO createTicket(TicketCreateRequestDTO dto) {
        User currentUser = getCurrentUser();

        Ticket newTicket = new Ticket();
        newTicket.setTitle(dto.getTitle());
        newTicket.setDescription(dto.getDescription());
        newTicket.setCategory(dto.getCategory());
        newTicket.setPriority(dto.getPriority());
        newTicket.setCreator(currentUser);
        newTicket.setStatus(Status.NEW);
        newTicket.setCreationDate(Instant.now());

        Ticket saved = ticketRepository.save(newTicket);

        auditLogRepository.save(new AuditLog(null, saved.getId(), null,
                currentUser.getId(), "TICKET_CREATED", null,
                saved.getStatus().toString(), Instant.now()));
        auditLogRepository.flush();

        return ticketMapper.toResponseDTO(saved);
    }

    @Transactional
    @Override
    public TicketResponseDTO updateTicketInfo(Long ticketId, TicketInfoUpdateDTO dto) {
        Ticket ticket = findTicketWithRbac(ticketId);

        updateIfNotNull(ticket::setTitle, dto.getTitle());
        updateIfNotNull(ticket::setDescription, dto.getDescription());
        updateIfNotNull(ticket::setCategory, dto.getCategory());
        updateIfNotNull(ticket::setPriority, dto.getPriority());

        return ticketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    // ──────────────────── 接单 ────────────────────

    @CacheEvict(value = "tickets", key = "#ticketId")
    @Transactional
    @Override
    public TicketResponseDTO assignTicket(Long ticketId) {
        User currentUser = getCurrentUser();
        if (!isSupportOrAdmin()) {
            throw new IllegalArgumentException("只有 IT 支持人员才能接单");
        }
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(ticketId, Ticket.class));
        if (ticket.getStatus() != Status.NEW) {
            throw new IllegalArgumentException("只有新建状态的工单才能被指派，当前状态: " + ticket.getStatus());
        }
        Status oldStatus = ticket.getStatus();
        ticket.setAssignedTo(currentUser);
        ticket.setStatus(Status.ASSIGNED);
        Ticket saved = ticketRepository.save(ticket);

        auditLogRepository.save(new AuditLog(null, ticketId, null,
                currentUser.getId(), "ASSIGNED",
                oldStatus.toString(), saved.getStatus().toString(), Instant.now()));
        auditLogRepository.flush();
        return ticketMapper.toResponseDTO(saved);
    }

    // ──────────────────── 状态机流转 ────────────────────

    @CacheEvict(value = "tickets", key = "#ticketId")
    @Transactional
    @Override
    public TicketResponseDTO updateTicketStatus(Long ticketId, TicketStatusUpdateDTO dto) {
        Ticket ticket = findTicketWithRbac(ticketId);
        Status currentStatus = ticket.getStatus();
        Status targetStatus = dto.getStatus();
        User currentUser = getCurrentUser();

        // 无变化则直接返回
        if (currentStatus == targetStatus) {
            return ticketMapper.toResponseDTO(ticket);
        }

        // 状态转移合法性校验 + 角色权限校验
        validateTransition(currentStatus, targetStatus, ticket, currentUser);

        Status oldStatus = ticket.getStatus();
        ticket.setStatus(targetStatus);
        Ticket saved = ticketRepository.save(ticket);

        auditLogRepository.save(new AuditLog(null, ticketId, null,
                currentUser.getId(), "STATUS_UPDATED",
                oldStatus.toString(), targetStatus.toString(), Instant.now()));
        auditLogRepository.flush();
        return ticketMapper.toResponseDTO(saved);
    }

    /**
     * 有限状态机（FSM）转移规则：
     *   ASSIGNED → IN_PROGRESS：只有接单人
     *   IN_PROGRESS → RESOLVED：只有接单人
     *   RESOLVED → CLOSED：只有工单创建人
     */
    private void validateTransition(Status from, Status to, Ticket ticket, User currentUser) {
        switch (to) {
            case IN_PROGRESS:
                if (from != Status.ASSIGNED) {
                    throw new IllegalArgumentException("只有已指派的工单才能开始处理，当前状态: " + from);
                }
                if (!Objects.equals(currentUser.getId(), ticket.getAssignedTo().getId())) {
                    throw new IllegalArgumentException("只有接单人才能处理此工单");
                }
                break;
            case RESOLVED:
                if (from != Status.IN_PROGRESS) {
                    throw new IllegalArgumentException("只有处理中的工单才能标记为已解决，当前状态: " + from);
                }
                if (!Objects.equals(currentUser.getId(), ticket.getAssignedTo().getId())) {
                    throw new IllegalArgumentException("只有接单人才能解决此工单");
                }
                break;
            case CLOSED:
                if (from != Status.RESOLVED) {
                    throw new IllegalArgumentException("只有已解决的工单才能关闭，当前状态: " + from);
                }
                if (!Objects.equals(currentUser.getId(), ticket.getCreator().getId())) {
                    throw new IllegalArgumentException("只有工单创建人才能关闭工单");
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "不支持的状态转换: " + from + " → " + to);
        }
    }

    @Cacheable(value = "tickets", key = "#ticketId")
    @Override
    public TicketResponseDTO retrieveTicket(Long ticketId) {
        return ticketMapper.toResponseDTO(findTicketWithRbac(ticketId));
    }

    @Override
    public List<TicketResponseDTO> retrieveTicketsByCreator() {
        User currentUser = getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByCreatorId(currentUser.getId());
        if (tickets == null || tickets.isEmpty())
            throw new ResourceNotFoundException("暂无工单");
        return tickets.stream().map(ticketMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    public Page<TicketResponseDTO> searchTickets(String status, String priority,
                                                  String category, String keyword, Pageable pageable) {
        User currentUser = getCurrentUser();
        Specification<Ticket> spec = buildSpecification(currentUser, status, priority, category, keyword);
        Page<Ticket> page = ticketRepository.findAll(spec, pageable);
        return page.map(ticketMapper::toResponseDTO);
    }

    @Override
    public TicketResponseDTO searchTicket(Long ticketId, Status status) {
        Ticket ticket = findTicketWithRbac(ticketId);
        if (ticket.getStatus() != status) {
            throw new ResourceNotFoundException(ticketId, Ticket.class);
        }
        return ticketMapper.toResponseDTO(ticket);
    }

    @Override
    public List<AuditLog> retrieveAuditLogs(Long ticketId) {
        // 校验工单可见性
        findTicketWithRbac(ticketId);
        return auditLogRepository.findByTicketId(ticketId);
    }

    @Override
    public void removeTicket(Long ticketId) {
        Ticket ticket = findTicketWithRbac(ticketId);
        User creator = ticket.getCreator();
        creator.getTickets().remove(ticket);
        userRepository.save(creator);
    }

    // ──────────────────── 权限核心 ────────────────────

    /**
     * RBAC 工单查询：EMPLOYEE 只能查自己的，IT_SUPPORT 可查全局。
     * 直接抛 ResourceNotFoundException，不在消息中区分「不存在」和「无权限」。
     */
    private Ticket findTicketWithRbac(Long ticketId) {
        if (isSupportOrAdmin()) {
            return ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException(ticketId, Ticket.class));
        } else {
            User currentUser = getCurrentUser();
            return ticketRepository.findByIdAndCreatorId(ticketId, currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(ticketId, Ticket.class));
        }
    }

    // ──────────────────── 动态查询 ────────────────────

    private Specification<Ticket> buildSpecification(User currentUser, String status,
                                                      String priority, String category, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // RBAC：EMPLOYEE 只能查自己创建的工单
            if (!isSupportOrAdmin()) {
                predicates.add(cb.equal(root.get("creator").get("id"), currentUser.getId()));
            }
            // IT_SUPPORT 不加 creator 限制，可搜索全局工单

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), Status.valueOf(status)));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), Priority.valueOf(priority)));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), Category.valueOf(category)));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("description"), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ──────────────────── 工具方法 ────────────────────

    private boolean isStatusChanged(Status oldStatus, Status newStatus) {
        return newStatus != null && !oldStatus.equals(newStatus);
    }

    public static Ticket unwrapTicket(Long ticketId, Optional<Ticket> optionalTicket) {
        return optionalTicket.orElseThrow(() -> new ResourceNotFoundException(ticketId, Ticket.class));
    }
}

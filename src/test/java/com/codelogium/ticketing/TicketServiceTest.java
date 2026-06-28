package com.codelogium.ticketing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.codelogium.ticketing.dto.TicketStatusUpdateDTO;
import com.codelogium.ticketing.dto.request.TicketCreateRequestDTO;
import com.codelogium.ticketing.dto.response.TicketResponseDTO;
import com.codelogium.ticketing.entity.Ticket;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.Category;
import com.codelogium.ticketing.entity.enums.Priority;
import com.codelogium.ticketing.entity.enums.Status;
import com.codelogium.ticketing.entity.enums.UserRole;
import com.codelogium.ticketing.mapper.TicketMapper;
import com.codelogium.ticketing.repository.AuditLogRepository;
import com.codelogium.ticketing.repository.TicketRepository;
import com.codelogium.ticketing.repository.UserRepository;
import com.codelogium.ticketing.service.TicketServiceImp;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TicketServiceImp ticketService;

    private User employee;
    private User itSupport;
    private Ticket ticket;
    private TicketCreateRequestDTO createDTO;
    private TicketResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        employee = new User(1L, "zhangsan", "pass123", "zs@test.com", UserRole.EMPLOYEE, null, null);
        itSupport = new User(3L, "it_wang", "pass123", "it@test.com", UserRole.IT_SUPPORT, null, null);

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("测试工单");
        ticket.setDescription("测试描述");
        ticket.setStatus(Status.NEW);
        ticket.setCreator(employee);
        ticket.setCategory(Category.NETWORK);
        ticket.setPriority(Priority.HIGH);

        createDTO = new TicketCreateRequestDTO();
        createDTO.setTitle("测试工单");
        createDTO.setDescription("测试描述");
        createDTO.setCategory(Category.NETWORK);
        createDTO.setPriority(Priority.HIGH);

        responseDTO = new TicketResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("测试工单");
        responseDTO.setStatus(Status.NEW);
        responseDTO.setCreatorId(1L);
        responseDTO.setCreatorUsername("zhangsan");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────────── 用例 1：正常提单 ────────────────────

    @Test
    void shouldCreateTicketSuccessfully() {
        // createTicket 只用到 getName()，不需要 getAuthorities()
        when(authentication.getName()).thenReturn("zhangsan");
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.of(employee));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponseDTO(any(Ticket.class))).thenReturn(responseDTO);

        TicketResponseDTO result = ticketService.createTicket(createDTO);

        assertNotNull(result);
        assertEquals("测试工单", result.getTitle());
        assertEquals(Status.NEW, result.getStatus());
        assertEquals("zhangsan", result.getCreatorUsername());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(auditLogRepository, times(1)).save(any());
    }

    // ──────────────────── 用例 2：EMPLOYEE 越权接单 ────────────────────

    @Test
    void shouldRejectAssignWhenNotITSupport() {
        when(authentication.getName()).thenReturn("zhangsan");
        doReturn(List.of(new SimpleGrantedAuthority("EMPLOYEE")))
                .when(authentication).getAuthorities();
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.of(employee));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.assignTicket(1L));
        assertEquals("只有 IT 支持人员才能接单", ex.getMessage());
    }

    // ──────────────────── 用例 3：NEW 直接跳 CLOSED ────────────────────

    @Test
    void shouldRejectNewToClosedTransition() {
        when(authentication.getName()).thenReturn("zhangsan");
        doReturn(List.of(new SimpleGrantedAuthority("EMPLOYEE")))
                .when(authentication).getAuthorities();
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.of(employee));
        when(ticketRepository.findByIdAndCreatorId(1L, 1L)).thenReturn(Optional.of(ticket));

        TicketStatusUpdateDTO dto = new TicketStatusUpdateDTO(Status.CLOSED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(1L, dto));
        assertEquals("只有已解决的工单才能关闭，当前状态: NEW", ex.getMessage());
    }

    // ──────────────────── 用例 4：IT_SUPPORT 正常接单 ────────────────────

    @Test
    void shouldAssignTicketSuccessfully() {
        when(authentication.getName()).thenReturn("it_wang");
        doReturn(List.of(new SimpleGrantedAuthority("IT_SUPPORT")))
                .when(authentication).getAuthorities();
        when(userRepository.findByUsername("it_wang")).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        // mapper 返回含 assignee 的 DTO
        when(ticketMapper.toResponseDTO(any(Ticket.class))).thenAnswer(inv -> {
            TicketMapper realMapper = new TicketMapper();
            return realMapper.toResponseDTO(inv.getArgument(0));
        });

        TicketResponseDTO result = ticketService.assignTicket(1L);

        assertNotNull(result);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }
}

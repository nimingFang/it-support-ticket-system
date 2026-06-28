package com.codelogium.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codelogium.ticketing.entity.Ticket;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.Status;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByIdAndCreatorId(Long ticketId, Long userId);

    List<Ticket> findByCreatorId(Long userId);
    /** 查找工单创建者（用于权限校验） */
    @Query("SELECT t.creator FROM Ticket t WHERE t.id = :ticketId")
    Optional<User> findCreatorByTicket(@Param("ticketId") Long ticketId);

    /** 按工单 ID 和状态精确查找 */
    @Query("SELECT t FROM Ticket t WHERE (t.id = :ticketId) AND (t.status = :status)")
    Optional<Ticket> findByTicketIdAndStatus(@Param("ticketId") Long ticketId, @Param("status") Status status);
}
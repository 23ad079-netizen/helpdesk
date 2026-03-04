package com.example.helpdeskapi.repository;

import com.example.helpdeskapi.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
}

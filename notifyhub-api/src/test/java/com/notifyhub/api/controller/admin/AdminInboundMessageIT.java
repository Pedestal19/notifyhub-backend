package com.notifyhub.api.controller.admin;

import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminInboundMessageControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired InboundMessageRepository repo;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("truncate table inbound_message");
    }

    @Test
    void list_returnsPage_andSupportsFilters() throws Exception {
        repo.save(InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+2348000000001")
                .body("m1")
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(OffsetDateTime.now().minusMinutes(2))
                .createdAt(OffsetDateTime.now().minusMinutes(2))
                .updatedAt(OffsetDateTime.now().minusMinutes(2))
                .build());

        repo.save(InboundMessageEntity.builder()
                .channel("WHATSAPP")
                .phoneNumber("+2348000000002")
                .body("m2")
                .status(InboundMessageStatus.PROCESSED)
                .receivedAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now().minusMinutes(1))
                .updatedAt(OffsetDateTime.now().minusMinutes(1))
                .build());

        mockMvc.perform(get("/admin/inbound-messages")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].phoneNumber").isNotEmpty());

        mockMvc.perform(get("/admin/inbound-messages")
                        .param("status", "PROCESSED")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PROCESSED"));

        mockMvc.perform(get("/admin/inbound-messages")
                        .param("phoneNumber", "+2348000000001")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].phoneNumber").value("+2348000000001"));

        mockMvc.perform(get("/admin/inbound-messages")
                        .param("status", "RECEIVED")
                        .param("phoneNumber", "+2348000000001")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void get_returnsDetails_and404WhenMissing() throws Exception {
        var saved = repo.save(InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+2348000000003")
                .body("hello details")
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now().minusMinutes(1))
                .updatedAt(OffsetDateTime.now().minusMinutes(1))
                .build());

        mockMvc.perform(get("/admin/inbound-messages/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.body").value("hello details"))
                .andExpect(jsonPath("$.phoneNumber").value("+2348000000003"));

        mockMvc.perform(get("/admin/inbound-messages/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Inbound message not found"));
    }
}
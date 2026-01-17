package com.notifyhub.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class InboundMessageControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    InboundMessageRepository inboundMessageRepository;

    @BeforeEach
    void clean() throws SQLException {
        jdbcTemplate.execute("truncate table inbound_message");
    }

    @Test
    void ingest_createRow_andReturns201() throws Exception {
        var payload = """
                {"channel":"SMS","phoneNumber":"+2348012345678","body":"hello"}
                """;

        mockMvc.perform(post("/v1/inbound-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.receivedAt").isNotEmpty());

        assertThat(inboundMessageRepository.count()).isEqualTo(1);
        var saved = inboundMessageRepository.findAll().get(0);
        assertThat(saved.getChannel()).isEqualTo("SMS");
        assertThat(saved.getPhoneNumber()).isEqualTo("+2348012345678");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getStatus()).isEqualTo(InboundMessageStatus.RECEIVED);
    }

    @Test
    void ingest_rejectsInvalidPayload_400() throws Exception {
        var payloadMissingBody = """
            {"channel":"SMS","phoneNumber":"+2348012345678"}
        """;

        mockMvc.perform(post("/v1/inbound-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadMissingBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_returnsMessages_andCanFilterByStatus() throws Exception {
        mockMvc.perform(post("/v1/inbound-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"channel":"SMS","phoneNumber":"+2348000000001","body":"m1"}"""))
                        .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/inbound-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"channel":"SMS","phoneNumber":"+2348000000002","body":"m2"}"""))
                        .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/inbound-messages").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].channel").isNotEmpty())
                .andExpect(jsonPath("$[0].body").isNotEmpty());

        mockMvc.perform(get("/v1/inbound-messages")
                        .param("status", "RECEIVED")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void webhookAlias_behavesSameAsV1() throws Exception {
        var payload = """
            {"channel":"SMS","phoneNumber":"+2348012345678","body":"hello"}
        """;

        mockMvc.perform(post("/webhooks/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

}

package com.notifyhub.api.integrations.twilio;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class TwilioWebhookControllerIT {

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

        mockMvc.perform(post("/webhooks/twilio")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("From=%2B2348012345678&To=%2B2348000000000&Body=hello&MessageSid=SM123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        assertThat(inboundMessageRepository.count()).isEqualTo(1);
        var saved = inboundMessageRepository.findAll().get(0);
        assertThat(saved.getChannel()).isEqualTo("SMS");
        assertThat(saved.getPhoneNumber()).isEqualTo("+2348012345678");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getStatus()).isEqualTo(InboundMessageStatus.RECEIVED);
    }
}

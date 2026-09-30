package com.guessword.guesstheword.controller;

import com.guessword.guesstheword.config.SecurityConfig;
import com.guessword.guesstheword.service.AdminReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReportController.class)
@Import(SecurityConfig.class)
class AdminReportControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReportService adminReportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessReports() throws Exception {
        mockMvc.perform(get("/api/admin/reports/daily").param("date", "2026-09-27"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void playerIsForbiddenFromReports() throws Exception {
        mockMvc.perform(get("/api/admin/reports/daily").param("date", "2026-09-27"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessReports() throws Exception {
        mockMvc.perform(get("/api/admin/reports/daily").param("date", "2026-09-27"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/admin/reports/daily").param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }
}

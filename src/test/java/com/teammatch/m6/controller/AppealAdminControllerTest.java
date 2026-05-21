package com.teammatch.m6.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teammatch.common.ReasonCode;
import com.teammatch.config.AuthExceptionHandler;
import com.teammatch.entity.Appeal;
import com.teammatch.exception.AuthenticationException;
import com.teammatch.exception.AuthorizationException;
import com.teammatch.m6.dto.AppealHandleDTO;
import com.teammatch.m6.service.AppealService;
import com.teammatch.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppealAdminController.class)
@Import(AuthExceptionHandler.class)
@DisplayName("申诉控制器测试（管理端）")
class AppealAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppealService appealService;

    @MockBean
    private AuthUtil authUtil;

    private static final String ADMIN_TOKEN = "Bearer admin-token";
    private static final Long ADMIN_ID = 999L;
    private static final Long APPEAL_ID = 1L;

    private Appeal pendingAppeal;

    @BeforeEach
    void setUp() {
        pendingAppeal = new Appeal();
        pendingAppeal.setId(APPEAL_ID);
        pendingAppeal.setUserId(100L);
        pendingAppeal.setTargetType("evaluation");
        pendingAppeal.setTargetId(200L);
        pendingAppeal.setStatus("pending");

        lenient().when(authUtil.requireUserId(ADMIN_TOKEN)).thenReturn(ADMIN_ID);
    }

    @Test
    @DisplayName("getAppealList: 非管理员返回M3009")
    void getAppealList_nonAdmin() throws Exception {
        doThrow(new AuthorizationException(ReasonCode.ADMIN_REQUIRED))
                .when(authUtil).requireAdmin(ADMIN_TOKEN);

        mockMvc.perform(get("/admin/appeals")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ReasonCode.ADMIN_REQUIRED.getCode()));

        verify(appealService, never()).getAppealList(any(), any());
    }

    @Test
    @DisplayName("getAppealList: 管理员成功查询")
    void getAppealList_success() throws Exception {
        when(appealService.getAppealList(null, null)).thenReturn(List.of(pendingAppeal));

        mockMvc.perform(get("/admin/appeals")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("getAppealById: 管理员成功查询详情")
    void getAppealById_success() throws Exception {
        when(appealService.getAppealById(APPEAL_ID)).thenReturn(pendingAppeal);

        mockMvc.perform(get("/admin/appeals/" + APPEAL_ID)
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
    }

    @Test
    @DisplayName("handleAppeal: Token无效返回M3000")
    void handleAppeal_invalidToken() throws Exception {
        doThrow(new AuthenticationException(ReasonCode.UNAUTHORIZED))
                .when(authUtil).requireAdmin("Bearer invalid-token");

        AppealHandleDTO dto = new AppealHandleDTO();
        dto.setStatus("approved");

        mockMvc.perform(put("/admin/appeals/" + APPEAL_ID + "/handle")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ReasonCode.UNAUTHORIZED.getCode()));

        verify(appealService, never()).handleAppeal(any(), any(), any());
    }

    @Test
    @DisplayName("handleAppeal: 管理员成功处理")
    void handleAppeal_success() throws Exception {
        Appeal handled = new Appeal();
        handled.setId(APPEAL_ID);
        handled.setStatus("approved");
        when(appealService.handleAppeal(eq(APPEAL_ID), eq(ADMIN_ID), any(AppealHandleDTO.class)))
                .thenReturn(handled);

        AppealHandleDTO dto = new AppealHandleDTO();
        dto.setStatus("approved");
        dto.setHandleResult("同意");

        mockMvc.perform(put("/admin/appeals/" + APPEAL_ID + "/handle")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.status").value("approved"));

        verify(appealService).handleAppeal(eq(APPEAL_ID), eq(ADMIN_ID), any(AppealHandleDTO.class));
    }
}

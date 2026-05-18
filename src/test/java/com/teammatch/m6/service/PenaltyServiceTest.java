package com.teammatch.m6.service;

import com.teammatch.m6.dto.PenaltyCreateDTO;
import com.teammatch.m6.dto.PenaltyRevokeDTO;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.mapper.PenaltyMapper;
import com.teammatch.m6.service.impl.PenaltyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PenaltyService 单元测试
 * 使用 Mockito Mock 数据库操作，专注于 Service 业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("处罚服务单元测试")
class PenaltyServiceTest {

    @Mock
    private PenaltyMapper penaltyMapper;

    @InjectMocks
    private PenaltyServiceImpl service;

    private static final Long PENALTY_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long ADMIN_ID = 999L;
    private static final Long REPORT_ID = 50L;

    private Penalty activeCreditPenalty;
    private Penalty activeFunctionPenalty;
    private Penalty revokedPenalty;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", penaltyMapper);

        activeCreditPenalty = new Penalty();
        activeCreditPenalty.setId(PENALTY_ID);
        activeCreditPenalty.setUserId(USER_ID);
        activeCreditPenalty.setType("credit_deduct");
        activeCreditPenalty.setCreditDeductValue(10);
        activeCreditPenalty.setReason("违规操作");
        activeCreditPenalty.setAdminId(ADMIN_ID);
        activeCreditPenalty.setRelatedReportId(REPORT_ID);
        activeCreditPenalty.setStatus("active");
        activeCreditPenalty.setCreatedAt(LocalDateTime.now().minusDays(1));
        activeCreditPenalty.setUpdatedAt(LocalDateTime.now().minusDays(1));

        activeFunctionPenalty = new Penalty();
        activeFunctionPenalty.setId(2L);
        activeFunctionPenalty.setUserId(USER_ID);
        activeFunctionPenalty.setType("function_limit");
        activeFunctionPenalty.setReason("恶意举报");
        activeFunctionPenalty.setAdminId(ADMIN_ID);
        activeFunctionPenalty.setStatus("active");
        activeFunctionPenalty.setCreatedAt(LocalDateTime.now().minusDays(2));
        activeFunctionPenalty.setUpdatedAt(LocalDateTime.now().minusDays(2));

        revokedPenalty = new Penalty();
        revokedPenalty.setId(3L);
        revokedPenalty.setUserId(USER_ID);
        revokedPenalty.setType("credit_deduct");
        revokedPenalty.setCreditDeductValue(5);
        revokedPenalty.setReason("临时处罚");
        revokedPenalty.setAdminId(ADMIN_ID);
        revokedPenalty.setStatus("revoked");
        revokedPenalty.setRevokedAt(LocalDateTime.now());
        revokedPenalty.setCreatedAt(LocalDateTime.now().minusDays(3));
        revokedPenalty.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== createPenalty ====================

    @Test
    @DisplayName("createPenalty: 正常创建credit_deduct处罚")
    void createPenalty_creditDeduct_success() {
        when(penaltyMapper.insert(any(Penalty.class))).thenReturn(1);

        PenaltyCreateDTO dto = new PenaltyCreateDTO();
        dto.setUserId(USER_ID);
        dto.setType("credit_deduct");
        dto.setCreditDeductValue(10);
        dto.setReason("违规操作");
        dto.setRelatedReportId(REPORT_ID);

        Penalty result = service.createPenalty(ADMIN_ID, dto);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getType()).isEqualTo("credit_deduct");
        assertThat(result.getCreditDeductValue()).isEqualTo(10);
        assertThat(result.getReason()).isEqualTo("违规操作");
        assertThat(result.getAdminId()).isEqualTo(ADMIN_ID);
        assertThat(result.getRelatedReportId()).isEqualTo(REPORT_ID);
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getCreatedAt()).isNotNull();
        verify(penaltyMapper).insert(any(Penalty.class));
    }

    @Test
    @DisplayName("createPenalty: 正常创建function_limit处罚")
    void createPenalty_functionLimit_success() {
        when(penaltyMapper.insert(any(Penalty.class))).thenReturn(1);

        PenaltyCreateDTO dto = new PenaltyCreateDTO();
        dto.setUserId(USER_ID);
        dto.setType("function_limit");
        dto.setReason("恶意举报");

        Penalty result = service.createPenalty(ADMIN_ID, dto);

        assertThat(result.getType()).isEqualTo("function_limit");
        assertThat(result.getCreditDeductValue()).isNull();
        assertThat(result.getReason()).isEqualTo("恶意举报");
        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    @DisplayName("createPenalty: credit_deduct缺少扣分值抛出异常")
    void createPenalty_creditDeductMissingValue_throws() {
        PenaltyCreateDTO dto = new PenaltyCreateDTO();
        dto.setUserId(USER_ID);
        dto.setType("credit_deduct");
        dto.setReason("违规操作");
        // 未设置creditDeductValue

        assertThatThrownBy(() -> service.createPenalty(ADMIN_ID, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("credit_deduct类型的处罚必须指定扣分值");

        verify(penaltyMapper, never()).insert(any());
    }

    // ==================== revokePenalty ====================

    @Test
    @DisplayName("revokePenalty: 正常撤销处罚")
    void revokePenalty_success() {
        when(penaltyMapper.selectById(PENALTY_ID)).thenReturn(activeCreditPenalty);
        when(penaltyMapper.updateById(any(Penalty.class))).thenReturn(1);

        PenaltyRevokeDTO dto = new PenaltyRevokeDTO();
        dto.setReason("申诉通过，撤销处罚");

        Penalty result = service.revokePenalty(PENALTY_ID, ADMIN_ID, dto);

        assertThat(result.getStatus()).isEqualTo("revoked");
        assertThat(result.getRevokedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(penaltyMapper).updateById(any(Penalty.class));
    }

    @Test
    @DisplayName("revokePenalty: 处罚不存在抛出异常")
    void revokePenalty_notFound_throws() {
        when(penaltyMapper.selectById(999L)).thenReturn(null);

        PenaltyRevokeDTO dto = new PenaltyRevokeDTO();
        dto.setReason("申诉通过");

        assertThatThrownBy(() -> service.revokePenalty(999L, ADMIN_ID, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("处罚记录不存在");
    }

    @Test
    @DisplayName("revokePenalty: 已撤销的处罚重复撤销抛出异常")
    void revokePenalty_alreadyRevoked_throws() {
        when(penaltyMapper.selectById(3L)).thenReturn(revokedPenalty);

        PenaltyRevokeDTO dto = new PenaltyRevokeDTO();
        dto.setReason("重复撤销");

        assertThatThrownBy(() -> service.revokePenalty(3L, ADMIN_ID, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("处罚已撤销，无法重复撤销");
    }

    // ==================== getUserPenalties ====================

    @Test
    @DisplayName("getUserPenalties: 返回用户的所有处罚记录")
    void getUserPenalties_success() {
        when(penaltyMapper.selectByUserId(USER_ID))
                .thenReturn(Arrays.asList(activeCreditPenalty, activeFunctionPenalty, revokedPenalty));

        List<Penalty> result = service.getUserPenalties(USER_ID);

        assertThat(result).hasSize(3);
        verify(penaltyMapper).selectByUserId(USER_ID);
    }

    @Test
    @DisplayName("getUserPenalties: 无处罚记录返回空列表")
    void getUserPenalties_empty() {
        when(penaltyMapper.selectByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        List<Penalty> result = service.getUserPenalties(USER_ID);

        assertThat(result).isEmpty();
    }

    // ==================== getUserActivePenalties ====================

    @Test
    @DisplayName("getUserActivePenalties: 返回用户生效中的处罚")
    void getUserActivePenalties_success() {
        when(penaltyMapper.selectActiveByUserId(USER_ID))
                .thenReturn(Arrays.asList(activeCreditPenalty, activeFunctionPenalty));

        List<Penalty> result = service.getUserActivePenalties(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        assertThat(result.get(1).getStatus()).isEqualTo("active");
    }

    // ==================== getPenaltyList ====================

    @Test
    @DisplayName("getPenaltyList: 查询全部处罚")
    void getPenaltyList_all() {
        when(penaltyMapper.selectList(any())).thenReturn(Arrays.asList(activeCreditPenalty, revokedPenalty));

        List<Penalty> result = service.getPenaltyList(null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getPenaltyList: 按状态筛选active")
    void getPenaltyList_byStatusActive() {
        when(penaltyMapper.selectByStatus("active")).thenReturn(Arrays.asList(activeCreditPenalty, activeFunctionPenalty));

        List<Penalty> result = service.getPenaltyList("active", null);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getPenaltyList: 按类型筛选credit_deduct")
    void getPenaltyList_byType() {
        when(penaltyMapper.selectByType("credit_deduct")).thenReturn(Arrays.asList(activeCreditPenalty, revokedPenalty));

        List<Penalty> result = service.getPenaltyList(null, "credit_deduct");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getPenaltyList: 同时按状态和类型筛选")
    void getPenaltyList_byStatusAndType() {
        when(penaltyMapper.selectList(any())).thenReturn(Arrays.asList(activeCreditPenalty));

        List<Penalty> result = service.getPenaltyList("active", "credit_deduct");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("active");
        assertThat(result.get(0).getType()).isEqualTo("credit_deduct");
    }

    @Test
    @DisplayName("getPenaltyList: 空字符串状态视为无筛选")
    void getPenaltyList_emptyStatus() {
        when(penaltyMapper.selectList(any())).thenReturn(Arrays.asList(activeCreditPenalty));

        List<Penalty> result = service.getPenaltyList("", null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getPenaltyList: 空字符串类型视为无筛选")
    void getPenaltyList_emptyType() {
        when(penaltyMapper.selectList(any())).thenReturn(Arrays.asList(activeCreditPenalty));

        List<Penalty> result = service.getPenaltyList(null, "");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getPenaltyList: 按状态筛选revoked")
    void getPenaltyList_byStatusRevoked() {
        when(penaltyMapper.selectByStatus("revoked")).thenReturn(Arrays.asList(revokedPenalty));

        List<Penalty> result = service.getPenaltyList("revoked", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("revoked");
    }

    @Test
    @DisplayName("getPenaltyList: status有值但type为空字符串时按status筛选")
    void getPenaltyList_statusWithEmptyType() {
        when(penaltyMapper.selectByStatus("active")).thenReturn(Arrays.asList(activeCreditPenalty, activeFunctionPenalty));

        List<Penalty> result = service.getPenaltyList("active", "");

        assertThat(result).hasSize(2);
        verify(penaltyMapper).selectByStatus("active");
    }

    // ==================== getPenaltyById ====================

    @Test
    @DisplayName("getPenaltyById: 查询存在的处罚")
    void getPenaltyById_exists() {
        when(penaltyMapper.selectById(PENALTY_ID)).thenReturn(activeCreditPenalty);

        Penalty result = service.getPenaltyById(PENALTY_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(PENALTY_ID);
    }

    @Test
    @DisplayName("getPenaltyById: 查询不存在的处罚返回null")
    void getPenaltyById_notExists() {
        when(penaltyMapper.selectById(999L)).thenReturn(null);

        Penalty result = service.getPenaltyById(999L);

        assertThat(result).isNull();
    }
}

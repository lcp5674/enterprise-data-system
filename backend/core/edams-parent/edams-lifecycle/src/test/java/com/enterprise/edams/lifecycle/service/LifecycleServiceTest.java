package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.repository.LifecycleMapper;
import com.enterprise.edams.lifecycle.repository.LifecycleStageMapper;
import com.enterprise.edams.lifecycle.service.impl.LifecycleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("生命周期服务测试")
class LifecycleServiceTest {

    @Mock
    private LifecycleMapper lifecycleMapper;

    @Mock
    private LifecycleStageMapper lifecycleStageMapper;

    @InjectMocks
    private LifecycleServiceImpl lifecycleService;

    private DataLifecycle testLifecycle;
    private LifecycleStage testStage;

    @BeforeEach
    void setUp() {
        testLifecycle = createTestLifecycle();
        testStage = createTestStage();
    }

    private DataLifecycle createTestLifecycle() {
        DataLifecycle lifecycle = new DataLifecycle();
        lifecycle.setId(1L);
        lifecycle.setDataAssetId(100L);
        lifecycle.setDataAssetName("测试数据资产");
        lifecycle.setDataAssetType("TABLE");
        lifecycle.setCurrentStage("CREATED");
        lifecycle.setStatus(1);
        lifecycle.setTenantId(1L);
        return lifecycle;
    }

    private LifecycleStage createTestStage() {
        LifecycleStage stage = new LifecycleStage();
        stage.setId(1L);
        stage.setStageCode("CREATED");
        stage.setStageName("创建阶段");
        stage.setNextStageCode("DEVELOPMENT");
        stage.setEnabled(1);
        stage.setSortOrder(1);
        return stage;
    }

    private LifecycleStage createNextStage() {
        LifecycleStage stage = new LifecycleStage();
        stage.setId(2L);
        stage.setStageCode("DEVELOPMENT");
        stage.setStageName("开发阶段");
        stage.setNextStageCode("MATURITY");
        stage.setEnabled(1);
        stage.setSortOrder(2);
        return stage;
    }

    private List<DataLifecycle> createTestLifecycleList(int count) {
        DataLifecycle[] lifecycles = new DataLifecycle[count];
        for (int i = 0; i < count; i++) {
            DataLifecycle lc = new DataLifecycle();
            lc.setId((long) (i + 1));
            lc.setDataAssetId((long) (100 + i));
            lc.setDataAssetName("资产" + i);
            lc.setCurrentStage("CREATED");
            lc.setStatus(1);
            lifecycles[i] = lc;
        }
        return Arrays.asList(lifecycles);
    }

    @Nested
    @DisplayName("生命周期状态转换测试")
    class StateTransitionTests {

        @Test
        @DisplayName("当资产创建时应创建生命周期记录")
        void shouldCreateLifecycleWhenAssetCreated() {
            when(lifecycleMapper.findByDataAssetId(anyLong())).thenReturn(null);
            when(lifecycleStageMapper.findByStageCode("CREATED")).thenReturn(testStage);
            when(lifecycleMapper.insert(any(DataLifecycle.class))).thenReturn(1);

            DataLifecycle result = lifecycleService.createLifecycle(testLifecycle);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(1);
            assertThat(result.getStageChangedAt()).isNotNull();
            verify(lifecycleMapper).insert(any(DataLifecycle.class));
        }

        @Test
        @DisplayName("当生命周期记录已存在时应抛出异常")
        void shouldThrowExceptionWhenLifecycleAlreadyExists() {
            when(lifecycleMapper.findByDataAssetId(anyLong())).thenReturn(testLifecycle);

            assertThatThrownBy(() -> lifecycleService.createLifecycle(testLifecycle))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("数据资产已存在生命周期记录");
        }

        @Test
        @DisplayName("当生命周期阶段不存在时应抛出异常")
        void shouldThrowExceptionWhenStageNotFound() {
            when(lifecycleMapper.findByDataAssetId(anyLong())).thenReturn(null);
            when(lifecycleStageMapper.findByStageCode("INVALID")).thenReturn(null);

            DataLifecycle lifecycle = createTestLifecycle();
            lifecycle.setCurrentStage("INVALID");

            assertThatThrownBy(() -> lifecycleService.createLifecycle(lifecycle))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("生命周期阶段不存在");
        }

        @Test
        @DisplayName("当切换到下一阶段时应更新状态")
        void shouldTransitionToNextStage() {
            LifecycleStage nextStage = createNextStage();
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);
            when(lifecycleStageMapper.findByStageCode("CREATED")).thenReturn(testStage);
            when(lifecycleStageMapper.findByStageCode("DEVELOPMENT")).thenReturn(nextStage);
            when(lifecycleMapper.updateById(any(DataLifecycle.class))).thenReturn(1);

            DataLifecycle result = lifecycleService.transitionToNextStage(1L);

            assertThat(result).isNotNull();
            assertThat(result.getPreviousStage()).isEqualTo("CREATED");
            assertThat(result.getCurrentStage()).isEqualTo("DEVELOPMENT");
            assertThat(result.getStageChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("当切换阶段但生命周期记录不存在时应抛出异常")
        void shouldThrowExceptionWhenLifecycleNotFoundOnTransition() {
            when(lifecycleMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> lifecycleService.transitionToNextStage(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("生命周期记录不存在");
        }

        @Test
        @DisplayName("当切换阶段但当前阶段不存在时应抛出异常")
        void shouldThrowExceptionWhenCurrentStageNotFoundOnTransition() {
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);
            when(lifecycleStageMapper.findByStageCode("CREATED")).thenReturn(null);

            assertThatThrownBy(() -> lifecycleService.transitionToNextStage(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("当前阶段不存在");
        }

        @Test
        @DisplayName("当切换阶段但无下一阶段时应抛出异常")
        void shouldThrowExceptionWhenNoNextStage() {
            LifecycleStage finalStage = createTestStage();
            finalStage.setNextStageCode(null);
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);
            when(lifecycleStageMapper.findByStageCode("CREATED")).thenReturn(finalStage);

            assertThatThrownBy(() -> lifecycleService.transitionToNextStage(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无下一阶段");
        }

        @Test
        @DisplayName("当切换阶段但下一阶段不存在时应抛出异常")
        void shouldThrowExceptionWhenNextStageNotFound() {
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);
            when(lifecycleStageMapper.findByStageCode("CREATED")).thenReturn(testStage);
            when(lifecycleStageMapper.findByStageCode("DEVELOPMENT")).thenReturn(null);

            assertThatThrownBy(() -> lifecycleService.transitionToNextStage(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("下一阶段不存在");
        }
    }

    @Nested
    @DisplayName("生命周期CRUD测试")
    class CrudTests {

        @Test
        @DisplayName("获取生命周期记录")
        void shouldGetLifecycleById() {
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);

            DataLifecycle result = lifecycleService.getLifecycle(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("根据数据资产ID获取生命周期记录")
        void shouldGetLifecycleByDataAssetId() {
            when(lifecycleMapper.findByDataAssetId(100L)).thenReturn(testLifecycle);

            DataLifecycle result = lifecycleService.getLifecycleByDataAssetId(100L);

            assertThat(result).isNotNull();
            assertThat(result.getDataAssetId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("更新生命周期记录")
        void shouldUpdateLifecycle() {
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);
            when(lifecycleMapper.updateById(any(DataLifecycle.class))).thenReturn(1);
            when(lifecycleMapper.selectById(1L)).thenReturn(testLifecycle);

            DataLifecycle updateData = new DataLifecycle();
            updateData.setId(1L);
            updateData.setCurrentStage("DEVELOPMENT");
            updateData.setChangeReason("测试更新");

            DataLifecycle result = lifecycleService.updateLifecycle(1L, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getPreviousStage()).isEqualTo("CREATED");
            assertThat(result.getStageChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("更新不存在的生命周期记录应抛出异常")
        void shouldThrowExceptionWhenUpdatingNonExistentLifecycle() {
            when(lifecycleMapper.selectById(999L)).thenReturn(null);

            DataLifecycle updateData = new DataLifecycle();
            updateData.setId(999L);

            assertThatThrownBy(() -> lifecycleService.updateLifecycle(999L, updateData))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("生命周期记录不存在");
        }

        @Test
        @DisplayName("删除生命周期记录")
        void shouldDeleteLifecycle() {
            doNothing().when(lifecycleMapper).deleteById(1L);

            lifecycleService.deleteLifecycle(1L);

            verify(lifecycleMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class PaginationTests {

        @Test
        @DisplayName("分页查询应返回正确的数据")
        void shouldReturnCorrectPage() {
            Page<DataLifecycle> page = new Page<>(1, 10);
            List<DataLifecycle> lifecycles = createTestLifecycleList(10);
            page.setRecords(lifecycles);
            page.setTotal(10);
            when(lifecycleMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            IPage<DataLifecycle> result = lifecycleService.listLifecycles(1, 10);

            assertThat(result.getRecords()).hasSize(10);
            assertThat(result.getTotal()).isEqualTo(10);
        }

        @Test
        @DisplayName("分页查询空结果应返回空列表")
        void shouldReturnEmptyListWhenNoResults() {
            Page<DataLifecycle> page = new Page<>(1, 10);
            page.setRecords(List.of());
            page.setTotal(0);
            when(lifecycleMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            IPage<DataLifecycle> result = lifecycleService.listLifecycles(1, 10);

            assertThat(result.getRecords()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("分页查询第二页应正确偏移")
        void shouldCorrectlyOffsetSecondPage() {
            Page<DataLifecycle> page = new Page<>(2, 10);
            List<DataLifecycle> lifecycles = createTestLifecycleList(5);
            page.setRecords(lifecycles);
            page.setTotal(15);
            when(lifecycleMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            IPage<DataLifecycle> result = lifecycleService.listLifecycles(2, 10);

            assertThat(result.getRecords()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("搜索查询测试")
    class SearchTests {

        @Test
        @DisplayName("根据关键词搜索应返回匹配结果")
        void shouldReturnMatchingResults() {
            Page<DataLifecycle> page = new Page<>(1, 10);
            List<DataLifecycle> lifecycles = List.of(testLifecycle);
            page.setRecords(lifecycles);
            page.setTotal(1);
            when(lifecycleMapper.searchByKeyword(any(Page.class), anyString())).thenReturn(page);

            IPage<DataLifecycle> result = lifecycleService.searchLifecycles("测试", 1, 10);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getDataAssetName()).contains("测试");
        }

        @Test
        @DisplayName("根据阶段查询应返回该阶段的所有记录")
        void shouldReturnRecordsByStage() {
            Page<DataLifecycle> page = new Page<>(1, 10);
            List<DataLifecycle> lifecycles = createTestLifecycleList(5);
            page.setRecords(lifecycles);
            page.setTotal(5);
            when(lifecycleMapper.findByStage(any(Page.class), anyString())).thenReturn(page);

            IPage<DataLifecycle> result = lifecycleService.listLifecyclesByStage("CREATED", 1, 10);

            assertThat(result.getRecords()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("阶段查询测试")
    class StageTests {

        @Test
        @DisplayName("获取所有启用的生命周期阶段")
        void shouldReturnAllEnabledStages() {
            List<LifecycleStage> stages = List.of(
                    createTestStage(),
                    createNextStage()
            );
            when(lifecycleStageMapper.selectList(any(QueryWrapper.class))).thenReturn(stages);

            List<LifecycleStage> result = lifecycleService.getAllStages();

            assertThat(result).hasSize(2);
            verify(lifecycleStageMapper).selectList(any(QueryWrapper.class));
        }

        @Test
        @DisplayName("获取阶段应按排序字段升序排列")
        void shouldReturnStagesOrderedBySortOrder() {
            LifecycleStage stage1 = createTestStage();
            stage1.setSortOrder(1);
            LifecycleStage stage2 = createNextStage();
            stage2.setSortOrder(2);
            when(lifecycleStageMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(stage1, stage2));

            List<LifecycleStage> result = lifecycleService.getAllStages();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSortOrder()).isLessThan(result.get(1).getSortOrder());
        }
    }
}

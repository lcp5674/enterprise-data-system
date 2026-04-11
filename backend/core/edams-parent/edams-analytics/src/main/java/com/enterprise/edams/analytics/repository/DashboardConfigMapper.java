package com.enterprise.edams.analytics.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.analytics.entity.DashboardConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 仪表盘配置Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface DashboardConfigMapper extends BaseMapper<DashboardConfig> {

    /**
     * 根据编码查询
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE dashboard_code = #{dashboardCode} AND deleted = 0 LIMIT 1")
    DashboardConfig selectByDashboardCode(@Param("dashboardCode") String dashboardCode);

    /**
     * 检查编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_dashboard_config WHERE dashboard_code = #{dashboardCode} AND deleted = 0")
    int existsByDashboardCode(@Param("dashboardCode") String dashboardCode);

    /**
     * 根据类型查询
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE dashboard_type = #{dashboardType} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<DashboardConfig> selectByDashboardType(@Param("dashboardType") String dashboardType);

    /**
     * 根据用户ID查询
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE user_id = #{userId} AND deleted = 0 ORDER BY sort_order ASC")
    List<DashboardConfig> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询系统默认仪表盘
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE is_default = 1 AND status = 1 AND deleted = 0 LIMIT 1")
    DashboardConfig selectDefaultDashboard();

    /**
     * 查询用户的默认仪表盘
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE user_id = #{userId} AND is_default = 1 AND status = 1 AND deleted = 0 LIMIT 1")
    DashboardConfig selectUserDefaultDashboard(@Param("userId") Long userId);

    /**
     * 更新仪表盘状态
     */
    @Update("UPDATE edams_dashboard_config SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新最后访问信息
     */
    @Update("UPDATE edams_dashboard_config SET last_access_time = #{accessTime}, access_count = access_count + 1, updated_time = NOW() WHERE id = #{id}")
    int updateAccessInfo(@Param("id") Long id, @Param("accessTime") String accessTime);

    /**
     * 取消所有默认仪表盘
     */
    @Update("UPDATE edams_dashboard_config SET is_default = 0, updated_time = NOW() WHERE dashboard_type = #{dashboardType}")
    int clearDefaultByType(@Param("dashboardType") String dashboardType);

    /**
     * 设置默认仪表盘
     */
    @Update("UPDATE edams_dashboard_config SET is_default = 1, updated_time = NOW() WHERE id = #{id}")
    int setAsDefault(@Param("id") Long id);

    /**
     * 根据访问权限查询
     */
    @Select("SELECT * FROM edams_dashboard_config WHERE access_level = #{accessLevel} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<DashboardConfig> selectByAccessLevel(@Param("accessLevel") String accessLevel);

    /**
     * 统计仪表盘数量
     */
    @Select("SELECT COUNT(1) FROM edams_dashboard_config WHERE deleted = 0")
    int countTotal();
}

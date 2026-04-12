package com.edams.incentive.repository;

import com.edams.incentive.entity.IncentiveRule;
import com.edams.incentive.entity.PointsRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface IncentiveMapper {

    @Insert("INSERT INTO t_incentive_rule(rule_name,action,points,description,status,create_time) VALUES(#{ruleName},#{action},#{points},#{description},#{status},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRule(IncentiveRule rule);

    @Select("SELECT * FROM t_incentive_rule ORDER BY create_time DESC LIMIT #{offset},#{size}")
    List<IncentiveRule> findAllRules(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM t_incentive_rule")
    long countRules();

    @Update("UPDATE t_incentive_rule SET rule_name=#{ruleName},points=#{points},description=#{description},update_time=#{updateTime} WHERE id=#{id}")
    int updateRule(IncentiveRule rule);

    @Delete("DELETE FROM t_incentive_rule WHERE id=#{id}")
    int deleteRule(Long id);

    @Select("SELECT * FROM t_incentive_rule WHERE action=#{action} AND status='ACTIVE' LIMIT 1")
    IncentiveRule findRuleByAction(String action);

    @Insert("INSERT INTO t_points_record(user_id,action,target_id,points,create_time) VALUES(#{userId},#{action},#{targetId},#{points},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPointsRecord(PointsRecord record);

    @Select("SELECT COALESCE(SUM(points),0) FROM t_points_record WHERE user_id=#{userId}")
    long sumUserPoints(String userId);

    @Select("SELECT * FROM t_points_record WHERE user_id=#{userId} ORDER BY create_time DESC LIMIT #{offset},#{size}")
    List<PointsRecord> findUserPoints(@Param("userId") String userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT user_id, SUM(points) as total_points FROM t_points_record GROUP BY user_id ORDER BY total_points DESC LIMIT #{top}")
    List<Map<String, Object>> getRankList(int top);
}

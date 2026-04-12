package com.enterprise.edams.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.catalog.entity.Catalog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CatalogMapper extends BaseMapper<Catalog> {

    @Select("SELECT * FROM data_catalog ORDER BY sort_order ASC")
    List<Catalog> findAll();

    @Select("SELECT * FROM data_catalog WHERE id = #{id}")
    Catalog findById(Long id);

    @Select("SELECT * FROM data_catalog WHERE parent_id = #{parentId}")
    List<Catalog> findByParentId(Long parentId);

    @Select("SELECT * FROM data_catalog WHERE name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%')")
    List<Catalog> searchByKeyword(String keyword);

    @Select("SELECT COUNT(*) FROM data_catalog")
    long countAll();

    @Select("SELECT COUNT(*) FROM data_catalog WHERE parent_id = #{parentId}")
    long countByParentId(Long parentId);

    @Insert("INSERT INTO data_catalog(name, description, icon, type, parent_id, sort_order, status, created_by, create_time, update_time) " +
            "VALUES(#{name},#{description},#{icon},#{type},#{parentId},#{sortOrder},#{status},#{createdBy},#{createTime},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Catalog catalog);

    @Update("UPDATE data_catalog SET name=#{name},description=#{description},icon=#{icon},type=#{type}," +
            "parent_id=#{parentId},sort_order=#{sortOrder},update_time=#{updateTime} WHERE id=#{id}")
    void update(Catalog catalog);

    @Delete("DELETE FROM data_catalog WHERE id = #{id}")
    void deleteById(Long id);
}

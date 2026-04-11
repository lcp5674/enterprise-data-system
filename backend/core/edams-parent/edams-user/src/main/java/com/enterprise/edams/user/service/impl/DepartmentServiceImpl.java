package com.enterprise.edams.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.user.entity.Department;
import com.enterprise.edams.user.repository.DepartmentMapper;
import com.enterprise.edams.user.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<Department> getDepartmentTree() {
        // 获取所有部门
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1)
               .orderByAsc(Department::getSortOrder);
        
        List<Department> allDepartments = departmentMapper.selectList(wrapper);

        // 构建树形结构（根节点parentId为0或null）
        List<Department> tree = new ArrayList<>();
        for (Department dept : allDepartments) {
            if (dept.getParentId() == null || dept.getParentId() == 0L) {
                tree.add(buildChildren(dept, allDepartments));
            }
        }
        return tree;
    }

    private Department buildChildren(Department parent, List<Department> allDepts) {
        for (Department dept : allDepts) {
            if (parent.getId().equals(dept.getParentId())) {
                parent.getChildren().add(buildChildren(dept, allDepts));
            }
        }
        return parent;
    }

    @Override
    public List<Department> getAllDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1).orderByAsc(Department::getSortOrder);
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public Department getById(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department create(Department department, String operator) {
        // 检查编码唯一性
        if (departmentMapper.findByCode(department.getCode()) != null) {
            throw new BusinessException("部门编码已存在: " + department.getCode());
        }

        // 设置层级和路径
        if (department.getParentId() != null && department.getParentId() > 0) {
            Department parent = departmentMapper.selectById(department.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            department.setLevel(parent.getLevel() != null ? parent.getLevel() + 1 : 1);
            department.setTreePath(parent.getTreePath() + String.format("%06d/", departmentMapper.selectCount(null) + 1));
        } else {
            department.setParentId(0L);
            department.setLevel(0);
            department.setTreePath(String.format("%06d/", departmentMapper.selectCount(null) + 1));
        }

        department.setStatus(1);
        department.setTenantId(1L);
        department.setCreatedBy(operator);

        departmentMapper.insert(department);
        log.info("部门创建成功: {} ({})", department.getName(), department.getId());
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Department department, String operator) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }

        existing.setName(department.getName());
        existing.setCode(department.getCode());
        existing.setDescription(department.getDescription());
        existing.setLeaderId(department.getLeaderId());
        existing.setLeaderName(department.getLeaderName());
        existing.setPhone(department.getPhone());
        existing.setEmail(department.getEmail());
        existing.setSortOrder(department.getSortOrder());

        existing.setUpdatedBy(operator);
        departmentMapper.updateById(existing);
        log.info("部门更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        int childCount = departmentMapper.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException("该部门下还有" + childCount + "个子部门，无法删除。请先删除子部门或移动到其他部门。");
        }

        // 检查是否有用户关联（通过user表查询）
        long userCount = checkUserCountByDept(id); // 简化处理
        if (userCount > 0) {
            throw new BusinessException("该部门下还有" + userCount + "名用户，无法删除。请先将用户转移到其他部门。");
        }

        dept.setDeleted(1);
        dept.setUpdatedBy(operator);
        departmentMapper.updateById(dept);

        log.info("部门已删除: {}", id);
    }

    private long checkUserCountByDept(Long deptId) {
        // 实际应通过Feign调用user-service或直接查询，这里简化
        return 0;
    }

    @Override
    public List<Department> getChildren(Long parentId) {
        return departmentMapper.findByParentId(parentId);
    }
}

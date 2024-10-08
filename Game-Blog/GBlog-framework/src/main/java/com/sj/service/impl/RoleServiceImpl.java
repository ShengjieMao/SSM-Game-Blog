package com.sj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sj.constants.CommonConstants;
import com.sj.domain.ResponseResult;
import com.sj.domain.dto.RoleDto;
import com.sj.domain.entity.Role;
import com.sj.domain.entity.RoleMenu;
import com.sj.domain.vo.AdminRoleVo;
import com.sj.domain.vo.PageVo;
import com.sj.domain.vo.RoleVo;
import com.sj.mapper.RoleMapper;
import com.sj.mapper.RoleMenuMapper;
import com.sj.service.RoleMenuService;
import com.sj.service.RoleService;
import com.sj.utils.BeanCopyUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sj.enums.AppHttpCodeEnum.*;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
        implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Lazy
    @Resource
    private RoleService roleService;

    @Resource
    private RoleMenuService roleMenuService;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<Role> getAllRoleList() {
        List<Role> roles = roleMapper.selectList(null);
        return roles;
    }

    @Override
    public ResponseResult getListAllRole() {
        List<Role> roles = roleMapper.selectList(null);
        return ResponseResult.okResult(roles);
    }

    @Override
    public ResponseResult getAllRoleByPage(Integer pageNum, Integer pageSize, RoleDto roleDto) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(roleDto.getStatus()), Role::getStatus,
                roleDto.getStatus());
        queryWrapper.like(StringUtils.hasText(roleDto.getRoleName()), Role::getRoleName
                , roleDto.getRoleName());

        Page<Role> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);

        List<Role> roles = page.getRecords();
        List<RoleVo> roleVos = BeanCopyUtils.copyBeanList(roles, RoleVo.class);
        AdminRoleVo adminRoleVo = new AdminRoleVo(roleVos, page.getTotal());

        return ResponseResult.okResult(adminRoleVo);
    }

    @Override
    public ResponseResult changeRoleStatus(Integer roleId, Integer status) {
        LambdaUpdateWrapper<Role> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Role::getId, roleId)
                .set(Role::getStatus, status);
        roleService.update(null, updateWrapper);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult addRole(RoleDto roleDto) {
        if (!this.judgeRole(roleDto.getRoleName())) {
            return ResponseResult.errorResult(ROLE_INFO_EXIST);
        }

        if (!this.judgeRoleKey(roleDto.getRoleKey())) {
            return ResponseResult.errorResult(ROLEKEY_INFO_EXIST);
        }

        List<Long> menuIds = roleDto.getMenuIds();

        Role role = BeanCopyUtils.copyBean(roleDto, Role.class);
        save(role);

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleName, role.getRoleName());
        Role getRole = getOne(queryWrapper);

        menuIds.stream()
                .map(menuId -> roleMenuService.save
                        (new RoleMenu(getRole.getId(), menuId)));

        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult getRoleInfoById(Long id) {
        Role role = roleService.getById(id);
        RoleVo roleVo = BeanCopyUtils.copyBean(role, RoleVo.class);

        return ResponseResult.okResult(roleVo);
    }

    @Override
    public ResponseResult deleteRole(Long id) {
        roleMenuService.removeById(id);
        removeById(id);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult updateRoleInfo(RoleDto roleDto) {
        if (!StringUtils.hasText(roleDto.getRoleName()) ||
                !StringUtils.hasText(roleDto.getRoleKey()) ||
                !StringUtils.hasText(String.valueOf(roleDto.getStatus())) ||
                !StringUtils.hasText(roleDto.getRemark()) ||
                !StringUtils.hasText(String.valueOf(roleDto.getRoleSort()))) {
            return ResponseResult.errorResult(CONTENT_IS_BLANK);
        }

        List<Long> menuIds = roleDto.getMenuIds();

        Role role = BeanCopyUtils.copyBean(roleDto, Role.class);
        updateById(role);

        List<Long> roleMenuIdsById = roleMenuService.getRoleMenuIdsById(role.getId());
        roleMenuService.removeById(role.getId());
        for (Long menuId : menuIds) {
            roleMenuService.save(new RoleMenu(role.getId(), menuId));
        }
        return ResponseResult.okResult();
    }

    @Override
    public List<String> selectRoleKeyByUserId(Long id) {
        if (id == 1L) { // add admin key
            List<String> roleKeys = new ArrayList<>();
            roleKeys.add("admin");
            return roleKeys;
        }
        return getBaseMapper().selectRoleKeyByUserId(id);
    }

    @Override
    public ResponseResult selectRolePage(Role role, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Role> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.hasText(role.getRoleName()),
                Role::getRoleName,role.getRoleName());
        lambdaQueryWrapper.eq(StringUtils.hasText(role.getStatus()), Role::getStatus,
                role.getStatus());
        lambdaQueryWrapper.orderByAsc(Role::getRoleSort);

        Page<Role> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page(page,lambdaQueryWrapper);

        List<Role> roles = page.getRecords();

        PageVo pageVo = new PageVo();
        pageVo.setTotal(page.getTotal());
        pageVo.setRows(roles);

        return ResponseResult.okResult(pageVo);
    }

    @Override
    @Transactional
    public void addRole(Role role) {
        save(role);
        System.out.println(role.getId());
        if(role.getMenuIds()!=null&&role.getMenuIds().length>0){
            insertRoleMenu(role);
        }
    }

    @Override
    public void updateRole(Role role) {
        updateById(role);
        roleMenuService.deleteRoleMenuByRoleId(role.getId());
        insertRoleMenu(role);
    }

    @Override
    public List<Role> selectRoleAll() {
        return list(Wrappers.<Role>lambdaQuery().eq(Role::getStatus,
                CommonConstants.MENU_STATUS_NORMAL));
    }

    @Override
    public List<Long> selectRoleIdByUserId(Long userId) {
        return getBaseMapper().selectRoleIdByUserId(userId);
    }

    private void insertRoleMenu(Role role) {
        List<RoleMenu> roleMenuList = Arrays.stream(role.getMenuIds())
                .map(menuId -> new RoleMenu(role.getId(), menuId))
                .collect(Collectors.toList());
        roleMenuService.saveBatch(roleMenuList);
    }

    public boolean judgeRole(String roleName) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Role::getRoleName, roleName);
        Role role = roleService.getOne(queryWrapper);
        if (Objects.isNull(role)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean judgeRoleKey(String roleKey) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Role::getRoleKey, roleKey);
        Role role = roleService.getOne(queryWrapper);
        if (Objects.isNull(role)) {
            return true;
        } else {
            return false;
        }
    }
}
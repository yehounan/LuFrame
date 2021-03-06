package com.yezi.luframe.service.impl;

import com.yezi.luframe.dto.AdminResourceDTO;
import com.yezi.luframe.entity.AdminResource;
import com.yezi.luframe.entity.AdminRoleResource;
import com.yezi.luframe.entity.AdminUserRole;
import com.yezi.luframe.enums.CodeEnum;
import com.yezi.luframe.mapper.AdminResourceMapper;
import com.yezi.luframe.mapper.AdminRoleResourceMapper;
import com.yezi.luframe.mapper.AdminUserMapper;
import com.yezi.luframe.mapper.AdminUserRoleMapper;
import com.yezi.luframe.param.AdminResourceInsertParam;
import com.yezi.luframe.service.AdminResourceService;
import com.yezi.luframe.util.VOUtils;
import com.yezi.luframe.vo.BaseVO;
import com.yezi.luframe.vo.ExtendVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Desc    : 资源服务实现类
 * @date    : 2018-01-22
 *
 * @author : yxy
 */
@Service
public class AdminResourceServiceImpl implements AdminResourceService {

    @Autowired
    private AdminResourceMapper adminResourceMapper;

    @Autowired
    private AdminRoleResourceMapper adminRoleResourceMapper;

    @Autowired
    private AdminUserRoleMapper adminUserRoleMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public ExtendVO getResourceList(Long roleId) {
        List<AdminRoleResource> roleResourceList = null;
        if (roleId != null) {
            Example example = new Example(AdminRoleResource.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("roleId", roleId);
            roleResourceList = adminRoleResourceMapper.selectByExample(example);
        }
        List<AdminResource> adminResourceList = adminResourceMapper.selectAll();
        List<AdminResourceDTO> resourceDTOList = new ArrayList<>();
        List<AdminResourceDTO> allSecondResourceDTOList = new ArrayList<>();
        for (AdminResource resource : adminResourceList) {
            if (resource.getType() == 1) {
                if (resource.getParentId() == 0L) {
                    AdminResourceDTO adminResourceDTO = new AdminResourceDTO();
                    BeanUtils.copyProperties(resource, adminResourceDTO);
                    adminResourceDTO.setRight(false);
                    if (roleResourceList != null) {
                        for (AdminRoleResource adminRoleResource : roleResourceList) {
                            if (Objects.equals(adminRoleResource.getResourceId(), adminResourceDTO.getId())) {
                                adminResourceDTO.setRight(true);
                                break;
                            }
                        }
                    }
                    List<AdminResourceDTO> secondResouceDTOList = new ArrayList<>();
                    adminResourceDTO.setSubResourceList(secondResouceDTOList);
                    resourceDTOList.add(adminResourceDTO);
                } else {
                    AdminResourceDTO secondResourceDTO = new AdminResourceDTO();
                    BeanUtils.copyProperties(resource, secondResourceDTO);
                    secondResourceDTO.setRight(false);
                    if (roleResourceList != null) {
                        for (AdminRoleResource adminRoleResource : roleResourceList) {
                            if (Objects.equals(adminRoleResource.getResourceId(), secondResourceDTO.getId())) {
                                secondResourceDTO.setRight(true);
                                break;
                            }
                        }
                    }
                    allSecondResourceDTOList.add(secondResourceDTO);
                }
            }
        }
        for (AdminResourceDTO secondResourceDTO : allSecondResourceDTOList) {
            for (AdminResourceDTO dto : resourceDTOList) {
                if (Objects.equals(secondResourceDTO.getParentId(), dto.getId())) {
                    dto.getSubResourceList().add(secondResourceDTO);
                }
            }
        }
        return VOUtils.returnExtendVOSuccess(resourceDTOList);
    }

    @Override
    public ExtendVO getMenuList(Long userId) {
        List<AdminUserRole> adminUserRoleList = adminUserRoleMapper.selectByUserId(userId);
        if (adminUserRoleList == null || adminUserRoleList.size() == 0) {
            return VOUtils.returnExtendVOError(CodeEnum.ADMIN_USER_NOT_ROLE, null);
        }
        List<Long> roleIdList = new ArrayList<>();
        for (AdminUserRole adminUserRole : adminUserRoleList) {
            Long roleId = adminUserRole.getRoleId();
            roleIdList.add(roleId);

        }
        //判断要用户属不属于超级管理员
        Boolean isAdmin = false;
        if (adminUserRoleMapper.checkAdmin(userId) > 0) {
            isAdmin = true;
        }
        List<AdminRoleResource> roleResourceListRedund = null;
        List<AdminRoleResource> roleResourceList = new ArrayList<>();
        roleResourceListRedund = adminRoleResourceMapper.selectByRoleIdList(roleIdList);
        for (AdminRoleResource adminRoleResource : roleResourceListRedund) {
            Boolean isRedund = false;
            for (AdminRoleResource adminRoleResource1 : roleResourceList) {
                if (Objects.equals(adminRoleResource.getResourceId(), adminRoleResource1.getResourceId())) {
                    isRedund = true;
                    break;
                }
            }
            if (!isRedund) {
                roleResourceList.add(adminRoleResource);
            }
        }
        List<AdminResource> adminResourceList = adminResourceMapper.selectAllOrderBySort();
        List<AdminResourceDTO> resourceDTOList = new ArrayList<>();
        List<AdminResourceDTO> allSecondResourceDTOList = new ArrayList<>();
        getMenuTempList(isAdmin, roleResourceList, adminResourceList, resourceDTOList, allSecondResourceDTOList);
        return VOUtils.returnExtendVOSuccess(resourceDTOList);
    }

    private void getMenuTempList(Boolean isAdmin, List<AdminRoleResource> roleResourceList, List<AdminResource> adminResourceList, List<AdminResourceDTO> resourceDTOList, List<AdminResourceDTO> allSecondResourceDTOList) {
        for (AdminResource resource : adminResourceList) {
            if (resource.getType() == 1) {
                if (resource.getParentId() == 0L) {
                    AdminResourceDTO adminResourceDTO = new AdminResourceDTO();
                    BeanUtils.copyProperties(resource, adminResourceDTO);
                    adminResourceDTO.setRight(false);
                    if (isAdmin) {
                        adminResourceDTO.setRight(true);
                        resourceDTOList.add(adminResourceDTO);
                    } else {
                        if (roleResourceList != null) {
                            for (AdminRoleResource adminRoleResource : roleResourceList) {
                                if (Objects.equals(adminRoleResource.getResourceId(), adminResourceDTO.getId())) {
                                    adminResourceDTO.setRight(true);
                                    resourceDTOList.add(adminResourceDTO);
                                    break;
                                }
                            }
                        }
                    }
                    List<AdminResourceDTO> secondResouceDTOList = new ArrayList<>();
                    adminResourceDTO.setSubResourceList(secondResouceDTOList);

                } else {
                    AdminResourceDTO secondResourceDTO = new AdminResourceDTO();
                    BeanUtils.copyProperties(resource, secondResourceDTO);
                    secondResourceDTO.setRight(false);
                    if (isAdmin) {
                        secondResourceDTO.setRight(true);
                        allSecondResourceDTOList.add(secondResourceDTO);
                    } else {
                        if (roleResourceList != null) {
                            for (AdminRoleResource adminRoleResource : roleResourceList) {
                                if (Objects.equals(adminRoleResource.getResourceId(), secondResourceDTO.getId())) {
                                    secondResourceDTO.setRight(true);
                                    allSecondResourceDTOList.add(secondResourceDTO);
                                    break;
                                }
                            }
                        }
                    }

                }
            }
        }
        for (AdminResourceDTO secondResourceDTO : allSecondResourceDTOList) {
            for (AdminResourceDTO dto : resourceDTOList) {
                if (Objects.equals(secondResourceDTO.getParentId(), dto.getId())) {
                    dto.getSubResourceList().add(secondResourceDTO);
                }
            }
        }
    }


    /**
     * 新增菜单
     *
     * @param adminResourceInsertParam
     * @return
     */
    @Override
    public BaseVO insertResource(AdminResourceInsertParam adminResourceInsertParam) {
        AdminResource adminResource = new AdminResource();
        BeanUtils.copyProperties(adminResourceInsertParam, adminResource);
        adminResource.setCreateTime(new Date());
        adminResourceMapper.insert(adminResource);
        return VOUtils.returnBaseVOSuccess();
    }

}

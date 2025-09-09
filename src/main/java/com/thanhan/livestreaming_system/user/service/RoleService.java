package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.request.RoleRequest;
import com.thanhan.livestreaming_system.user.entity.Role;

import java.util.List;

public interface RoleService {
    Role createRole(RoleRequest request);
    List<Role> getAll();
    void deleteRole(String roleName);
}

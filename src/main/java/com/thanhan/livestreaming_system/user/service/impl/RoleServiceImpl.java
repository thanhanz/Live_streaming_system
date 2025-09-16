package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.user.dto.request.RoleRequest;
import com.thanhan.livestreaming_system.user.entity.Role;
import com.thanhan.livestreaming_system.user.repository.RoleRepository;
import com.thanhan.livestreaming_system.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role createRole(RoleRequest request) {
        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());

        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public void deleteRole(String roleName) {
        roleRepository.deleteById(roleName);
    }

    @Override
    public Role getRole(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not found"));
    }
}

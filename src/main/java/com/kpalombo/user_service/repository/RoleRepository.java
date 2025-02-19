package com.kpalombo.user_service.repository;

import com.kpalombo.music_mgmt_collection_library.interfaces.BaseRepository;
import com.kpalombo.user_service.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends BaseRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}

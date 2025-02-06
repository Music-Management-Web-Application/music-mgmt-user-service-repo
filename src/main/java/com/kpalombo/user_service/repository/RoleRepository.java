package com.kpalombo.user_service.repository;

import com.kpalombo.music_mgmt_collection_library.interfaces.BaseRepository;
import com.kpalombo.user_service.model.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role> {
    Optional<Role> findByName(String name);
}

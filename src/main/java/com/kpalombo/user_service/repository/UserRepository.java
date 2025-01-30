package com.kpalombo.user_service.repository;

import com.kpalombo.music_mgmt_collection_library.interfaces.BaseRepository;
import com.kpalombo.user_service.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
}

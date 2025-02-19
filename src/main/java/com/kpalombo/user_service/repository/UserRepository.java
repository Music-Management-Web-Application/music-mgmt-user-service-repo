package com.kpalombo.user_service.repository;

import com.kpalombo.music_mgmt_collection_library.interfaces.BaseRepository;
import com.kpalombo.user_service.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findBySpotifyId(String spotifyId);
}

package com.kpalombo.user_service.controller;

import com.kpalombo.music_mgmt_collection_library.annnotation.CollectionRecordRepository;
import com.kpalombo.user_service.model.User;
import com.kpalombo.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.Optional;

@CollectionRecordRepository(path = "/spotify")
public class SpotifyAuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/login")
    public Map<String, Object> getSpotifyLogin(OAuth2AuthenticationToken token) {
        OAuth2User user = token.getPrincipal();
        Map<String, Object> attributes = user.getAttributes();

        // Extract Spotify user data
        String spotifyId = (String) attributes.get("id");
        String displayName = (String) attributes.get("display_name");
        String email = (String) attributes.get("email");
        String profileImageUrl = ((attributes.get("images") instanceof java.util.List) &&
                !((java.util.List<?>) attributes.get("images")).isEmpty()) ?
                (String) ((Map<?, ?>) ((java.util.List<?>) attributes.get("images")).get(0)).get("url") : null;

        // Check if the user already exists
        Optional<User> existingUser = userRepository.findBySpotifyId(spotifyId);
        User record;
        if (existingUser.isPresent()) {
            record = existingUser.get();
        } else {
            // Create a new user
            record = new User();
            record.setSpotifyId(spotifyId);
            record.setUsername(displayName);
            record.setEmail(email);
            record.setProfileImageUrl(profileImageUrl);
            record.setSpotifyUser(true);
            record.setPassword(passwordEncoder.encode(record.getPassword()));
            userRepository.save(record);
        }
        return attributes;
    }
}

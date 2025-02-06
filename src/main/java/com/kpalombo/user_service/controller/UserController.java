package com.kpalombo.user_service.controller;

import com.kpalombo.music_mgmt_collection_library.annnotation.CollectionRecordRepository;
import com.kpalombo.music_mgmt_collection_library.interfaces.CollectionController;
import com.kpalombo.music_mgmt_collection_library.interfaces.Request;
import com.kpalombo.music_mgmt_collection_library.interfaces.Response;
import com.kpalombo.user_service.model.Role;
import com.kpalombo.user_service.model.User;
import com.kpalombo.user_service.repository.RoleRepository;
import com.kpalombo.user_service.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CollectionRecordRepository(path = "/users")
public class UserController extends CollectionController<User, UUID> {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public UserController(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    @PostMapping("/create")
    public Response<User> create(@RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        Optional<User> existingUser = ((UserRepository) repository).findByEmail(record.getEmail());
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = record;
            user.setPassword(passwordEncoder.encode(record.getPassword()));
            repository.save(user);
        }
        response.setResponse(new ResponseEntity<>(user, HttpStatus.CREATED));
        return response;
    }

    @Override
    @PutMapping("/record")
    public Response<User> update(@RequestParam String id, @RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        record.setId(UUID.fromString(id));
        record.setPassword(passwordEncoder.encode(record.getPassword()));
        User updatedRecord = repository.save(record);
        response.setResponse(new ResponseEntity<>(updatedRecord, HttpStatus.OK));
        return response;
    }

    @GetMapping("/spotify/login")
    public Response<User> spotifyLogin(@AuthenticationPrincipal OAuth2User principal) {
        Response<User> response = new Response<>();
        Map<String, Object> attributes = principal.getAttributes();
        // spotify user information
        String spotifyId = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("display_name");
        Optional<List<?>> images = Optional.ofNullable((List<?>) attributes.get("images"));
        String profileImageUrl = images.map(list -> ((Map<?, ?>) list.get(0)).get("url")).map(Object::toString).orElse(null);

        Optional<User> existingUser = ((UserRepository) repository).findByEmail(email);
        User user = existingUser.orElseGet(User::new);
        if (existingUser.isEmpty()) {
            user.setEmail(email);
            user.setUsername(username);
            user.setProfileImageUrl(profileImageUrl);
            user.setSpotifyUser(true);
            user.setPassword("spotify"); // not used but required to save
        }
        user.setSpotifyId(spotifyId);
        repository.save(user);
        response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        return response;
    }

    @PutMapping("{id}/assign-role")
    public Response<User> assignRole(@PathVariable UUID id, @RequestParam String roleName) {
        Response<User> response = new Response<>();
        User user = repository.findById(id).orElse(null);
        Role role = roleRepository.findByName(roleName).orElse(null);
        if (user != null && role != null) {
            user.getRoles().add(role);
            repository.save(user);
            response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        } else {
            response.setResponse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return response;
    }
}

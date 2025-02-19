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

        if (existingUser.isPresent()) {
            response.setResponse(new ResponseEntity<>(existingUser.get(), HttpStatus.CONFLICT));
        } else {
            record.setPassword(passwordEncoder.encode(record.getPassword()));
            User createdRecord = repository.save(record);
            response.setResponse(new ResponseEntity<>(createdRecord, HttpStatus.CREATED));
        }
        return response;
    }

    @Override
    @PutMapping("/update")
    public Response<User> update(@RequestParam String id, @RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        Optional<User> existingUser = repository.findById(UUID.fromString(id));
        if (existingUser.isPresent()) {
            record.setId(UUID.fromString(id));
            record.setPassword(passwordEncoder.encode(record.getPassword()));
            User updatedRecord = repository.save(record);
            response.setResponse(new ResponseEntity<>(updatedRecord, HttpStatus.OK));
        } else {
            response.setResponse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return response;
    }

    @GetMapping("/login")
    public Response<User> login(@RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        Optional<User> existingUser = ((UserRepository) repository).findByEmail(record.getEmail());
        if (existingUser.isPresent() && passwordEncoder.matches(record.getPassword(), existingUser.get().getPassword())) {
            response.setResponse(new ResponseEntity<>(existingUser.get(), HttpStatus.OK));
        } else {
            response.setResponse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return response;
    }

    @GetMapping("/spotify/login")
    public Response<User> spotifyLogin(@AuthenticationPrincipal OAuth2User principal) {
        Response<User> response = new Response<>();
        if (principal == null) {
            response.setResponse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
        } else {
            Map<String, Object> attributes = principal.getAttributes();
            // spotify user information
            String profileImageUrl = Optional.ofNullable((List<?>) attributes.get("images"))
                    .map(list -> ((Map<?, ?>) list.get(0)).get("url"))
                    .map(Object::toString).orElse(null);
            Optional<User> existingUser = ((UserRepository) repository).findByEmail((String) attributes.get("email"));
            User user = existingUser.orElseGet(User::new);
            if (existingUser.isEmpty()) {
                user.setEmail((String) attributes.get("email"));
                user.setUsername((String) attributes.get("display_name"));
                user.setProfileImageUrl(profileImageUrl);
                user.setPassword("spotify"); // not used but required to save
            }
            user.setSpotifyUser(true);
            user.setSpotifyId((String) attributes.get("id"));
            repository.save(user);
            response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        }
        return response;
    }

    @PutMapping("/assign-role")
    public Response<User> assignRole(@RequestParam String roleId, @RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        UUID id = UUID.fromString(request.getId());
        Optional<User> userOptional = repository.findById(id);
        Optional<Role> roleOptional = roleRepository.findById(UUID.fromString(roleId));
        if (userOptional.isPresent() && roleOptional.isPresent()) {
            User user = userOptional.get();
            Role role = roleOptional.get();
            user.getRoles().add(role);
            repository.save(user);
            response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        } else {
            response.setResponse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return response;
    }

    @PostMapping("/create-role")
    public Response<Role> createRole(@RequestBody @Valid Request<Role> request) {
        Response<Role> response = new Response<>();
        Role role = request.getRecord();
        Optional<Role> existingRole = roleRepository.findByName(role.getName());
        if (existingRole.isPresent()) {
            response.setResponse(new ResponseEntity<>(existingRole.get(), HttpStatus.CONFLICT));
        } else {
            roleRepository.save(role);
            response.setResponse(new ResponseEntity<>(role, HttpStatus.CREATED));
        }
        return response;
    }
}

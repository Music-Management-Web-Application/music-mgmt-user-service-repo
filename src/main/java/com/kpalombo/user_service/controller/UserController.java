package com.kpalombo.user_service.controller;

import com.kpalombo.music_mgmt_collection_library.annnotation.CollectionRecordRepository;
import com.kpalombo.music_mgmt_collection_library.interfaces.CollectionController;
import com.kpalombo.music_mgmt_collection_library.interfaces.Request;
import com.kpalombo.music_mgmt_collection_library.interfaces.Response;
import com.kpalombo.user_service.model.User;
import com.kpalombo.user_service.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CollectionRecordRepository(path = "/users")
public class UserController extends CollectionController<User, UUID> {
    @Autowired
    PasswordEncoder passwordEncoder;
    public UserController(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    @PostMapping("/create")
    public Response<User> create(@RequestBody @Valid Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        record.setPassword(passwordEncoder.encode(record.getPassword()));
        User createdRecord = repository.save(record);
        response.setResponse(new ResponseEntity<>(createdRecord, HttpStatus.CREATED));
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

    @GetMapping("/spotify/callback")
    public Response<User> handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        Response<User> response = new Response<>();
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String spotifyId = (String) attributes.get("id");
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("display_name");

        Optional<User> existingUser = ((UserRepository)repository).findByEmail(email);
        User user;
        if (existingUser.isPresent()){
            user = existingUser.get();
            if (user.getSpotifyId() == null){
                user.setSpotifyId(spotifyId);
            }
        }
        else{
            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setSpotifyId(spotifyId);
            repository.save(user);
        }
        response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        return response;
    }
}

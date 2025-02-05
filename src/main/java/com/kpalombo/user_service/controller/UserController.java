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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/spotify/login")
    public Response<User> spotifyLogin(@AuthenticationPrincipal OAuth2User principal) {
        Response<User> response = new Response<>();
        User user = new User();
        user.setSpotifyId(principal.getAttribute("id"));
        user.setUsername(principal.getAttribute("display_name"));
        user.setEmail(principal.getAttribute("email"));
        user.setProfileImageUrl(principal.getAttribute("images[0].url"));
        user.setSpotifyUser(true);
        user.setPassword("spotify"); // not used but required
        repository.save(user);
        response.setResponse(new ResponseEntity<>(user, HttpStatus.OK));
        return response;
    }
}

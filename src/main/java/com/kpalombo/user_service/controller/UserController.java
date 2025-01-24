package com.kpalombo.user_service.controller;

import com.kpalombo.music_mgmt_collection_library.annnotation.CollectionRecordRepository;
import com.kpalombo.music_mgmt_collection_library.interfaces.CollectionController;
import com.kpalombo.music_mgmt_collection_library.interfaces.Request;
import com.kpalombo.music_mgmt_collection_library.interfaces.Response;
import com.kpalombo.user_service.model.User;
import com.kpalombo.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CollectionRecordRepository(path = "/users")
public class UserController extends CollectionController<User, Long> {
    @Autowired
    PasswordEncoder passwordEncoder;
    public UserController(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    @PostMapping("/create")
    public Response<User> create(@RequestBody Request<User> request) {
        Response<User> response = new Response<>();
        User record = request.getRecord();
        record.setPassword(passwordEncoder.encode(record.getPassword()));
        User createdRecord = repository.save(record);
        response.setResponse(new ResponseEntity<>(createdRecord, HttpStatus.CREATED));
        return response;
    }

    @Override
    @PutMapping("/{id}")
    public Response<User> update(@RequestBody Request<User> request) {
        Response<User> response = new Response<>();
        User newRecord = request.getRecord();
        User record = repository.findById(Long.valueOf(request.getId())).orElse(null);
        record.setUsername(newRecord.getUsername());
        record.setPassword(passwordEncoder.encode(newRecord.getPassword()));
        record.setEmail(newRecord.getEmail());
        User updatedRecord = repository.save(record);
        response.setResponse(new ResponseEntity<>(updatedRecord, HttpStatus.OK));
        return response;
    }
}

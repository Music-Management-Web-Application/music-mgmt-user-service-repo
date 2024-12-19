package com.kpalombo.user_service.controller;

import com.kpalombo.user_service.model.User;
import com.kpalombo.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody @Valid User user) {
        User createdUser = userService.registerUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}

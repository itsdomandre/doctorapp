package com.domandre.controllers;

import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.mappers.UserMapper;
import com.domandre.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAll() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        List<UserDTO> dtoUser = new ArrayList<>();
        for (User user : users) {
            dtoUser.add(UserMapper.toDTO(user));
        }
        return ResponseEntity.ok(dtoUser);
    }


}

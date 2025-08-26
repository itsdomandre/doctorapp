package com.domandre.services;

import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.mappers.UserMapper;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with user: " + username));
    }

    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getUserById(UUID id) throws ResourceNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Page<UserDTO> getAllPatients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").descending());
        Page<User> users = userRepository.findByRole(Role.USER, pageable);
        return users.map(UserMapper::toDTO);
    }
}
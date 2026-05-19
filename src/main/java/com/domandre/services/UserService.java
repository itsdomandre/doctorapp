package com.domandre.services;

import com.domandre.controllers.request.AdminCreateUserRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.enums.UserStatus;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.mappers.UserMapper;
import com.domandre.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with user: " + username));
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getUserById(UUID id) throws ResourceNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Page<UserDTO> getAllPatients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<User> users = userRepository.findByRole(Role.USER, pageable);
        return users.map(UserMapper::toDTO);
    }

    public UserDTO updateUserRole(UUID id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return UserMapper.toDTO(userRepository.save(user));
    }

    public UserDTO adminCreateUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException();
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirthdate(request.getBirthdate());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        return UserMapper.toDTO(userRepository.save(user));
    }
}
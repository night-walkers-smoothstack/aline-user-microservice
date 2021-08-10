package com.aline.usermicroservice.service;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.NotFoundException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.repository.MemberUserRepository;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.SearchSpecification;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MemberService memberService;
    private final UserRepository repository;
    private final MemberUserRepository memberUserRepository;
    private final ModelMapper mapper;

    /**
     * Retrieve a User Response by ID
     * @param id The ID of the user entity
     * @return A UserResponse mapped from a User entity.
     */
    public UserResponse getUserById(@NonNull Long id) {
        User user = repository.findById(id).orElseThrow(UserNotFoundException::new);
        return mapToDto(user);
    }

    /**
     * Get all users by page and search term
     * @param pageable The pageable object passed from the controller
     * @param search The search term used to find a user
     * @return A PaginatedResponse of UserResponse DTOs.
     */
    public PaginatedResponse<UserResponse> getAllUsers(@NonNull final Pageable pageable, @NonNull String search) {
        SearchSpecification<User> spec = new SearchSpecification<>(search);
        Page<User> usersPage = repository.findAll(spec, pageable);
        Page<UserResponse> userResponsePage = usersPage.map(this::mapToDto);
        return new PaginatedResponse<>(userResponsePage.getContent(), pageable, userResponsePage.getTotalElements());
    }

    /**
     * Save a user manually
     * @param user A user entity (usually with a null ID value)
     * @return The saved user from the repository.
     */
    public UserResponse saveUser(User user) {
        return mapToDto(repository.save(user));
    }

    /**
     * Map a User entity to a UserResponse
     * @param user The user entity to map
     * @return A UserResponse mapped from a User entity.
     */
    private UserResponse mapToDto(User user) {
        return mapper.map(user, UserResponse.class);
    }
}

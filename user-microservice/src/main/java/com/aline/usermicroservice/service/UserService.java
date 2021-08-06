package com.aline.usermicroservice.service;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.NotFoundException;
import com.aline.core.model.user.User;
import com.aline.core.repository.UserRepository;
import com.aline.usermicroservice.util.DtoMapper;
import com.aline.usermicroservice.util.SimpleCrudService;
import org.springframework.stereotype.Service;

@Service
public class UserService extends SimpleCrudService<UserResponse, User, UserRegistration, UserRegistration, Long, UserRepository> {

    public UserService(UserRepository repository, DtoMapper<User, UserResponse> mapper) {
        super(repository, mapper);
    }

    @Override
    public UserResponse create(UserRegistration createDto) {
        // TODO: Implement create logic here.
        return null;
    }

    @Override
    protected NotFoundException throwNotFoundException() {
        return new NotFoundException("User does not exist.");
    }
}

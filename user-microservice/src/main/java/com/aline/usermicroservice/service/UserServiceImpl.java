package com.aline.usermicroservice.service;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.user.User;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.SearchSpecification;
import com.aline.usermicroservice.service.registration.UserRegistrationHandler;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final ModelMapper modelMapper;
    @SuppressWarnings("rawtypes")
    private final List<UserRegistrationHandler> handlers;
    @SuppressWarnings("rawtypes")
    private Map<Class<? extends UserRegistration>, UserRegistrationHandler> handlerMap;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(UserRegistrationHandler::registersAs, Function.identity()));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapToDto(repository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    @Override
    public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable, String search) {
        SearchSpecification<User> spec = new SearchSpecification<>(search);
        Page<User> usersPage = repository.findAll(spec, pageable);
        Page<UserResponse> userResponsePage = usersPage.map(this::mapToDto);
        return new PaginatedResponse<>(userResponsePage.getContent(), pageable, userResponsePage.getTotalElements());
    }

    protected UserResponse mapToDto(User user) {
        return modelMapper.map(user, UserResponse.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserResponse registerUser(UserRegistration registration) {
        handlerMap.get(registration.getClass()).register(registration);
        return null;
    }
}

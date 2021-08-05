package com.aline.usermicroservice.controller;

import com.aline.core.dto.response.PaginatedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController implements ICrudController<C, R, U, K> {
}

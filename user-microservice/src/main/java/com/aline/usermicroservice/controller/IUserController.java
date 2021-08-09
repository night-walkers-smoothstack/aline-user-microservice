package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The CrudController interface provides
 * the default get mappings and return types for a
 * base CRUD Rest Controller.
 * 
 * @apiNote The interface is annotated by default with
 *          <code>@RequestMapping("/default")</code> which
 *          means all mappings will have a parent path of
 *          <em>/default</em>.
 */
@RequestMapping("/users")
public interface IUserController {

    @GetMapping("/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping
    ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers();

    @PostMapping
    ResponseEntity<UserResponse> registerUser(@RequestBody UserRegistration registration);

    @PutMapping("/{id}")
    ResponseEntity<Void> updateUser(@PathVariable("id") Long id, @RequestBody UserProfileUpdate profileUpdate);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);
}

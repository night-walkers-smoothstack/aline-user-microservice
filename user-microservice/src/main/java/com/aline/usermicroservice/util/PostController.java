package com.aline.usermicroservice.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The PostController interface provides
 * a post mapping to the root location of
 * the controller.
 * @param <T> The response type.
 * @param <C> The type of the DTO that creates the entity.
 */
@RequestMapping("/default")
public interface PostController<T, C> {

    @PostMapping
    ResponseEntity<T> post(@RequestBody C createDto);

}

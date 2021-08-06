package com.aline.usermicroservice.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * GetController interface provides
 * a standard ID get mapping.
 * @param <T> Response type
 * @param <ID> Id type
 */
@RequestMapping("/default")
public interface GetController<T, ID> {

    @GetMapping("/{id}")
    ResponseEntity<T> get(@PathVariable("id") ID id);

}

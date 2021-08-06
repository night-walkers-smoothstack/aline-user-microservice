package com.aline.usermicroservice.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The delete controller provides a
 * void response entity method that uses
 * the DELETE mapping annotation. It uses
 * the resource's id.
 * @param <ID> The type of the id.
 */
@RequestMapping("/")
public interface DeleteController<ID> {

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") ID id);

}

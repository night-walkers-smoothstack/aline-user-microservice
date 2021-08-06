package com.aline.usermicroservice.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The UpdateController provides a
 * void response entity that is annotated with
 * a PUT mapping to the resource's id.
 * @param <U> The DTO type that is used to update the entity.
 * @param <ID> The type of the id.
 */
public interface UpdateController<U, ID> {

    @PutMapping("/{id}")
    ResponseEntity<Void> update(@PathVariable("id") ID id, @RequestBody U updateDto);

}

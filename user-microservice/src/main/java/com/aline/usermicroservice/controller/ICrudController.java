package com.aline.usermicroservice.controller;

import com.aline.core.dto.response.PaginatedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ICrudController provides an interface for
 * a class annotated with <code>@RestController</code>. The interface
 * provides controller crud methods that are not yet
 * implemented but are already annotated with
 * RESTful mappings.
 * <p>
 *     Mappings:
 *     <ul>
 *         <li><code>{@link ICrudController#get(K)}</code> -> <code><em>'GET .../{id}'</em></code></li>
 *         <li><code>{@link ICrudController#getAll()}</code> -> <code><em>'GET .../'</em></code></li>
 *         <li><code>{@link ICrudController#create(C)}</code> -> <code><em>'POST .../'</em></code></li>
 *         <li><code>{@link ICrudController#update(K, U)}</code> -> <code><em>'PUT .../{id}'</em></code></li>
 *         <li><code>{@link ICrudController#delete(K)}</code> -> <code><em>'DELETE .../{id}'</em></code></li>
 *     </ul>
 * </p>
 *
 * @param <C> The request body type that is used to post a new entity.
 * @param <R> The response dto that represents the entity. (This is used
 *           as the return type of the create method.)
 * @param <U> The update request body that is used to update a new entity.
 * @param <K> The id type. Usually an {@link Integer} or a {@link Long}.
 */
@RequestMapping("/default")
public interface ICrudController<C, R, U, K> {

    @GetMapping("/{id}")
    ResponseEntity<R> get(@PathVariable("id") K id);

    @GetMapping
    ResponseEntity<PaginatedResponse<R>> getAll();

    @PostMapping
    ResponseEntity<R> create(@RequestBody C createDto);

    @PutMapping("/{id}")
    ResponseEntity<Void> update(@PathVariable("id") K id, @RequestBody U updateDto);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") K id);

}

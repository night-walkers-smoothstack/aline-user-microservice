package com.aline.usermicroservice.controller;

import com.aline.core.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

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
@RequestMapping("/default")
public interface CrudController<Response, Post, Update, ID> {

    @GetMapping("/{id}")
    ResponseEntity<Response> getById(@PathVariable("id") ID id);

    @GetMapping
    ResponseEntity<PaginatedResponse<Response>> getAll(Pageable pageable,
                                                       @RequestParam(name = "search", defaultValue = "")
                                                               String search);

    @PostMapping
    ResponseEntity<Response> post(@RequestBody @Valid Post postBody);

    @PutMapping("/{id}")
    ResponseEntity<Void> update(@PathVariable("id") ID id, @RequestBody @Valid Update updateBody);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") ID id);


}

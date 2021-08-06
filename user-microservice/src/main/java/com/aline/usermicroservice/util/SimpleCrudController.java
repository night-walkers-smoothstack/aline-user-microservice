package com.aline.usermicroservice.util;

import com.aline.core.dto.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * The Simple Crud Service abstract class provides an abstracted
 * implementation of the CrudService interface.
 * The type parameters are used to automatically build the simple crud
 * pattern seen in most services in this application.
 * @param <T> The Response DTO that the entity will be mapped to.
 * @param <E> The entity domain model that is accessed by the repository.
 * @param <C> The creation DTO that creates the entity.
 * @param <U> The update DTO that updates the entity.
 * @param <ID> The ID type.
 * @param <S> The service type. The service must extend the {@link CrudService} interface.
 */
@RequestMapping("/")
@RequiredArgsConstructor
public abstract class SimpleCrudController<T, E, C, U, ID, S extends CrudService<T, E, C, U, ID>> implements GetController<T, ID>, GetAllController<T>, PostController<T, C>, UpdateController<U, ID>, DeleteController<ID> {

    private final S service;

    @Override
    public ResponseEntity<T> get(ID id) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getById(id));
    }

    @Override
    public ResponseEntity<PaginatedResponse<T>> getAll(Pageable pageable, String search) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getAll(pageable, search));
    }

    protected abstract ID getId(T t);

    @Override
    public ResponseEntity<T> post(C createDto) {
        T created = service.create(createDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .build(getId(created));
        return ResponseEntity
                .created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(created);
    }

    @Override
    public ResponseEntity<Void> update(ID id, U updateDto) {
        service.update(id, updateDto);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

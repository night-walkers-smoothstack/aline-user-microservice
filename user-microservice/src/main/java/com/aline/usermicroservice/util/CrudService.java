package com.aline.usermicroservice.util;

import com.aline.core.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

/**
 * Crud service interface provides methods
 * that is used for basic CRUD operations.
 * @param <T> The return type of get and create methods.
 * @param <E> The entity that will be mapped to the T DTO.
 * @param <C> The creation DTO used to create an entity of type E.
 * @param <U> The update DTO used to update and entity of type E.
 * @param <ID> The type of the id.
 */
public interface CrudService<T, E, C, U, ID> {

    T save(E t);

    T getById(ID id);

    PaginatedResponse<T> getAll(Pageable pageable, String search);

    T create(C createDto);

    void update(ID id, U updateDto);

    void delete(ID id);

}

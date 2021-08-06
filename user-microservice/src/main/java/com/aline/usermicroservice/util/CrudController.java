package com.aline.usermicroservice.util;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * To implement your own controller logic,
 * implement this interface. If you want the method
 * implementations to be abstracted, extend the abstract
 * class {@link SimpleCrudController}.
 * @param <T> The Response DTO that the entity will be mapped to.
 * @param <E> The entity domain model that is accessed by the repository.
 * @param <C> The creation DTO that creates the entity.
 * @param <U> The update DTO that updates the entity.
 * @param <ID> The ID type.
 * @param <S> The service type. The service must extend the {@link CrudService} interface.
 * @see SimpleCrudController
 */
@RequestMapping("/")
public interface CrudController<T, E, C, U, ID, S extends CrudService<T, E, C, U, ID>> extends GetController<T, ID>, GetAllController<T>, PostController<T, C>, UpdateController<U, ID>, DeleteController<ID> {
}

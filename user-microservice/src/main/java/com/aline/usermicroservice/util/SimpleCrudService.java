package com.aline.usermicroservice.util;

import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.exception.NotFoundException;
import com.aline.core.repository.JpaRepositoryWithSpecification;
import com.aline.core.util.SearchSpecification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.validation.constraints.NotNull;

/**
 * Simple Crud Service abstract class implements the CrudService interface.
 * The type parameters are used to automatically build the simple crud
 * pattern seen in most services in this application.
 * @param <T> The Response DTO that the entity will be mapped to.
 * @param <E> The entity domain model that is accessed by the repository.
 * @param <C> The creation DTO that creates the entity.
 * @param <U> The update DTO that updates the entity.
 * @param <ID> The ID type.
 * @param <R> The repository type. The repository must extend the {@link JpaRepositoryWithSpecification} class.
 */
@RequiredArgsConstructor
public abstract class SimpleCrudService<T, E, C, U, ID, R extends JpaRepositoryWithSpecification<E, ID>> implements CrudService<T, E, C, U, ID> {

    protected final R repository;
    private final DtoMapper<E, T> mapper;

    protected abstract NotFoundException throwNotFoundException();

    @Override
    public T save(E e) {
        return mapper.mapToDto(repository.save(e));
    }

    @Override
    public T getById(ID id) {
        return mapper.mapToDto(repository.findById(id).orElseThrow(this::throwNotFoundException));
    }

    @Override
    public PaginatedResponse<T> getAll(@NotNull Pageable pageable, @NotNull final String search) {
        Specification<E> specification = new SearchSpecification<>(search);
        Page<T> page = repository.findAll(specification, pageable)
                .map(mapper::mapToDto);
        return new PaginatedResponse<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    public void update(ID id, U updateDto) {
        E e = repository.findById(id).orElseThrow(this::throwNotFoundException);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(updateDto, e);
        repository.save(e);
    }

    @Override
    public void delete(ID id) {
        E e = repository.findById(id).orElseThrow(this::throwNotFoundException);
        repository.delete(e);
    }
}

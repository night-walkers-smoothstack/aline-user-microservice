package com.aline.usermicroservice.util;

import com.google.gson.reflect.TypeToken;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * The DTO Mapper class provides methods
 * to map Entity E to DTO and vice-versa.
 * @param <E> Entity type.
 * @param <T> DTO type.
 */
@Component
public class DtoMapper<E, T> {

    protected Type getDtoType() {
        return new TypeToken<T>(){}.getType();
    }

    protected Type getEntityType() {
        return new TypeToken<E>(){}.getType();
    }

    public T mapToDto(E e) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(e, getDtoType());
    }

    public E mapToEntity(T t) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(t, getEntityType());
    }

}

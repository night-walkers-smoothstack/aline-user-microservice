package com.aline.usermicroservice.util;

import com.aline.core.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GetAllController interface provides
 * a get mapping that should return a
 * paginated response.
 * @param <T> Response type to be paginated.
 */
@RequestMapping("/default")
public interface GetAllController<T> {

    @GetMapping
    ResponseEntity<PaginatedResponse<T>> getAll(Pageable pageable, @RequestParam(name = "search", defaultValue = "") String search);

}

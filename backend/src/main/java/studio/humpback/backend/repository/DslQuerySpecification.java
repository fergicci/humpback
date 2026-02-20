package studio.humpback.backend.repository;

import java.util.List;

import com.querydsl.core.types.Predicate;

public interface DslQuerySpecification<T> {

    Predicate toPredicate(List<String> dslFilters);
}

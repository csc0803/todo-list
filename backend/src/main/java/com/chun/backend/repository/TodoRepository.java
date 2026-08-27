package com.chun.backend.repository;

import com.chun.backend.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByCompleted(Boolean completed);

}

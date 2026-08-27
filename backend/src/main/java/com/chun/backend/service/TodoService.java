package com.chun.backend.service;

import com.chun.backend.entity.Todo;
import java.util.List;

public interface TodoService {

    List<Todo> getAllTodos();                  // GET /api/todos
    List<Todo> getAllTodos(Boolean completed);  // GET /api/todos?completed=xxx（篩選，可選）

    Todo getTodoById(Long id);                  // GET /api/todos/{id}，查無則丟例外（供 Controller 轉 404）

    Todo createTodo(Todo todo);                 // POST /api/todos

    Todo updateTodo(Long id, Todo todo);        // PUT /api/todos/{id}

    Todo toggleCompleted(Long id);               // PATCH /api/todos/{id}/toggle

    void deleteTodo(Long id);                    // DELETE /api/todos/{id}
}

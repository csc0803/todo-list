package com.chun.backend.controller;

import com.chun.backend.entity.Todo;
import com.chun.backend.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // GET /api/todos            → 查全部
    // GET /api/todos?completed=true/false → 依完成狀態篩選（可選）
    @GetMapping
    public List<Todo> getAllTodos(
            @RequestParam(required = false) Boolean completed) {
        if (completed == null) {
            return todoService.getAllTodos();
        }
        return todoService.getAllTodos(completed);
    }

    // GET /api/todos/{id}       → 查單筆
    @GetMapping("/{id}")
    public Todo getTodoById(@PathVariable Long id) {
        return todoService.getTodoById(id);
    }

    // POST /api/todos           → 新增
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo) {
        Todo created = todoService.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/todos/{id}       → 更新
    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @Valid @RequestBody Todo todo) {
        return todoService.updateTodo(id, todo);
    }

    // PATCH /api/todos/{id}/toggle → 切換完成狀態
    @PatchMapping("/{id}/toggle")
    public Todo toggleCompleted(@PathVariable Long id) {
        return todoService.toggleCompleted(id);
    }

    // DELETE /api/todos/{id}    → 刪除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
}

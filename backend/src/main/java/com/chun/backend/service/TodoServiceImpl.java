package com.chun.backend.service;

import com.chun.backend.entity.Todo;
import com.chun.backend.exception.TodoNotFoundException;
import com.chun.backend.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Override
    public List<Todo> getAllTodos() {

        return todoRepository.findAll();
    }

    @Override
    public List<Todo> getAllTodos(Boolean completed) {
        return todoRepository.findAllByCompleted(completed);
    }

    @Override
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Override
    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    @Override
    public Todo updateTodo(Long id, Todo todo) {
        Todo oldTodo = getTodoById(id);
        oldTodo.setTitle(todo.getTitle());
        oldTodo.setDescription(todo.getDescription());
        oldTodo.setCompleted(todo.getCompleted());
        return todoRepository.save(oldTodo);

    }

    @Override
    public Todo toggleCompleted(Long id) {
        Todo oldTodo = getTodoById(id);
        oldTodo.setCompleted(!oldTodo.getCompleted());
        return todoRepository.save(oldTodo);

    }

    @Override
    public void deleteTodo(Long id) {
        getTodoById(id);
        todoRepository.deleteById(id);
    }
}

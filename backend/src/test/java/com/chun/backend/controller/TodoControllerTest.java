package com.chun.backend.controller;

import com.chun.backend.entity.Todo;
import com.chun.backend.exception.TodoNotFoundException;
import com.chun.backend.service.TodoService;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTodos_withoutCompletedParam_shouldCallGetAllTodos() throws Exception {
        Todo todo = new Todo();
        todo.setId(1L);
        todo.setTitle("買牛奶");
        when(todoService.getAllTodos()).thenReturn(List.of(todo));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("買牛奶"));

        verify(todoService).getAllTodos();
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void getAllTodos_withCompletedParam_shouldCallGetAllTodosWithFilter() throws Exception {
        Todo todo = new Todo();
        todo.setId(2L);
        todo.setTitle("已完成的事");
        todo.setCompleted(true);
        when(todoService.getAllTodos(true)).thenReturn(List.of(todo));

        mockMvc.perform(get("/api/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completed").value(true));

        verify(todoService).getAllTodos(true);
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void getAllTodos_shouldReturnEmptyArray_whenNoTodoExists() throws Exception {
        when(todoService.getAllTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTodoById_shouldCallGetTodoById() throws Exception {
        Todo todo = new Todo();
        todo.setId(1L);
        todo.setTitle("買牛奶");
        when(todoService.getTodoById(1L)).thenReturn(todo);

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("買牛奶"));

        verify(todoService).getTodoById(1L);
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void getTodoById_shouldReturn404_whenNoTodoExists() throws Exception {
        when(todoService.getTodoById(1L)).thenThrow(new TodoNotFoundException(1L));

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 1"));

        verify(todoService).getTodoById(1L);
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void createTodo_shouldCallCreateTodo() throws Exception {
        Todo requestBody = new Todo();
        requestBody.setTitle("買牛奶");

        Todo created = new Todo();
        created.setId(1L);
        created.setTitle("買牛奶");
        when(todoService.createTodo(any(Todo.class))).thenReturn(created);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("買牛奶"));

        verify(todoService).createTodo(any(Todo.class));
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void createTodo_shouldReturn400_whenTitleExceedsMaxLength() throws Exception {
        Todo requestBody = new Todo();
        requestBody.setTitle("買牛奶".repeat(40)); // 120 字，超過 @Size(max = 100)

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(todoService);
    }

    @Test
    void createTodo_shouldReturn400_whenJsonMalformed() throws Exception {
        String malformedJson = "{ \"title\": \"買牛奶\", "; // 故意少右括號

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoService);
    }

    @Test
    void createTodo_shouldReturn400_whenTitleFieldMissing() throws Exception {
        String jsonWithoutTitle = "{ \"description\": \"沒有標題欄位\" }"; // title key 整個不存在，跟「空字串」是不同 case

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutTitle))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(todoService);
    }

    @Test
    void updateTodo_shouldCallUpdateTodo() throws Exception {
        Todo requestBody = new Todo();
        requestBody.setTitle("買起司");

        Todo updated = new Todo();
        updated.setId(1L);
        updated.setTitle("買起司");
        when(todoService.updateTodo(eq(1L), any(Todo.class))).thenReturn(updated);

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("買起司"));

        verify(todoService).updateTodo(eq(1L), any(Todo.class));
        verifyNoMoreInteractions(todoService);

    }

    @Test
    void updateTodo_shouldReturn400_whenTitleExceedsMaxLength() throws Exception {
        Todo requestBody = new Todo();
        requestBody.setTitle("買牛奶".repeat(40)); // 120 字，超過 @Size(max = 100)

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(todoService);
    }

    @Test
    void updateTodo_shouldReturn404_whenNoExistID() throws Exception {
        Todo requestBody = new Todo();
        requestBody.setTitle("買牛奶");

        when(todoService.updateTodo(eq(1L), any(Todo.class))).thenThrow(new TodoNotFoundException(1L));

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 1"));

        verify(todoService).updateTodo(eq(1L), any(Todo.class));
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void toggleCompleted_shouldCallToggleCompleted() throws Exception {
        Todo toggled = new Todo();
        toggled.setTitle("買起司");
        toggled.setCompleted(false);

        when(todoService.toggleCompleted(eq(1L))).thenReturn(toggled);

        mockMvc.perform(patch("/api/todos/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("買起司"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService).toggleCompleted(eq(1L));
        verifyNoMoreInteractions(todoService);

    }

    @Test
    void toggleCompleted_shouldReturn404_whenNoExistID() throws Exception {

        when(todoService.toggleCompleted(eq(1L))).thenThrow(new TodoNotFoundException(1L));

        mockMvc.perform(patch("/api/todos/1/toggle"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 1"));

        verify(todoService).toggleCompleted(eq(1L));
        verifyNoMoreInteractions(todoService);

    }

    @Test
    void deleteTodo_shouldCalldeleteTodo() throws Exception {

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService).deleteTodo(1L);
        verifyNoMoreInteractions(todoService);
    }

    @Test
    void deleteTodo_shouldReturn404_whenNoExistID() throws Exception {

        doThrow(new TodoNotFoundException(1L)).when(todoService).deleteTodo(eq(1L));

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 1"));

        verify(todoService).deleteTodo(1L);
        verifyNoMoreInteractions(todoService);
    }



}

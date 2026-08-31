package com.chun.backend.repository;

import com.chun.backend.entity.Todo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldGenerateIdAndApplyPrePersistDefaults() {
        Todo todo = new Todo();
        todo.setTitle("買牛奶");
        // completed 故意不設定，驗證 @PrePersist 是否補上預設值

        Todo saved = todoRepository.save(todo);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCompleted()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void save_shouldThrow_whenTitleIsBlank() {
        Todo todo = new Todo();
        todo.setTitle("   ");

        assertThatThrownBy(() -> todoRepository.saveAndFlush(todo))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void save_shouldThrow_whenTitleExceedsMaxLength() {
        Todo todo = new Todo();
        todo.setTitle("a".repeat(101));

        assertThatThrownBy(() -> todoRepository.saveAndFlush(todo))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void findById_shouldGetTodoById() {
        Todo todo = new Todo();
        todo.setTitle("買牛奶");
        Todo saved = todoRepository.save(todo);
        Long savedId = saved.getId();

        // 清掉 persistence context，逼 findById 真的重新查一次 DB，
        // 而不是直接從 first-level cache 回傳同一個物件參考
        entityManager.flush();
        entityManager.clear();

        Optional<Todo> found = todoRepository.findById(savedId);

        assertThat(found).isPresent();
        assertThat(found.get()).isNotSameAs(saved); // 確認真的是重新撈出來的物件
        assertThat(found.get().getId()).isEqualTo(savedId);
        assertThat(found.get().getTitle()).isEqualTo("買牛奶");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Todo> found = todoRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldGetAllTodo() {
        Todo todo1 = new Todo();
        todo1.setTitle("測試1");
        todoRepository.save(todo1);

        Todo todo2 = new Todo();
        todo2.setTitle("測試2");
        todoRepository.save(todo2);

        Todo todo3 = new Todo();
        todo3.setTitle("測試3");
        todoRepository.save(todo3);

        entityManager.flush();
        entityManager.clear();

        List<Todo> todos =  todoRepository.findAll();

        assertThat(todos)
                .hasSize(3)
                .extracting(Todo::getTitle)
                .containsExactlyInAnyOrder("測試1", "測試2", "測試3");

    }

    @Test
    void findAll_shouldReturnEmptyList_whenNotExists() {
        List<Todo> todos =  todoRepository.findAll();

        assertThat(todos).isEmpty();
    }

    @Test
    void update_shouldUpdateTodo() {
        Todo todo = new Todo();
        todo.setTitle("BeforeUpdate");
        todoRepository.save(todo);

        entityManager.flush();
        entityManager.clear();

        Long id = todo.getId();

        Todo fetched = todoRepository.findById(id).orElseThrow();
        Instant originalCreatedAt = fetched.getCreatedAt();
        Instant originalUpdatedAt = fetched.getUpdatedAt();

        fetched.setTitle("Updated");
        todoRepository.save(fetched);

        entityManager.flush();
        entityManager.clear();

        Todo result = todoRepository.findById(id).orElseThrow();

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt); // 不應被更新覆蓋
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt); // @PreUpdate 應該有刷新
    }

    @Test
    void findAllByCompleted_shouldGetAllByCompleted() {
        Todo todo1 = new Todo();
        todo1.setTitle("測試1");
        todo1.setCompleted(true);
        todoRepository.save(todo1);

        Todo todo2 = new Todo();
        todo2.setTitle("測試2");
        todo2.setCompleted(true);
        todoRepository.save(todo2);

        Todo todo3 = new Todo();
        todo3.setTitle("測試3");
        todoRepository.save(todo3);

        Todo todo4 = new Todo();
        todo4.setTitle("測試4");
        todoRepository.save(todo4);

        entityManager.flush();
        entityManager.clear();

        List<Todo> completed = todoRepository.findAllByCompleted(true);

        assertThat(completed)
                .hasSize(2)
                .extracting(Todo::getTitle)
                .containsExactlyInAnyOrder("測試1", "測試2");

        List<Todo> notCompleted = todoRepository.findAllByCompleted(false);

        assertThat(notCompleted)
                .hasSize(2)
                .extracting(Todo::getTitle)
                .containsExactlyInAnyOrder("測試3", "測試4");

    }

    @Test
    void deleteById_shouldDeleteTodoById() {
        Todo todo1 = new Todo();
        todo1.setTitle("測試1");
        todoRepository.save(todo1);

        Todo todo2 = new Todo();
        todo2.setTitle("測試2");
        todoRepository.save(todo2);

        Todo todo3 = new Todo();
        todo3.setTitle("測試3");
        todoRepository.save(todo3);

        Todo todo4 = new Todo();
        todo4.setTitle("測試4");
        todoRepository.save(todo4);

        entityManager.flush();
        entityManager.clear();

        todoRepository.deleteById(todo1.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<Todo> found = todoRepository.findById(todo1.getId());
        assertThat(found).isEmpty();

        List<Todo> todos = todoRepository.findAll();

        assertThat(todos)
                .hasSize(3)
                .extracting(Todo::getTitle)
                .containsExactlyInAnyOrder("測試2", "測試3", "測試4");
    }

    @Test
    void deleteById_shouldDoNothing_whenNotExists() {
        // Spring Data JPA 4.1.1 的 SimpleJpaRepository#deleteById 內部是
        // findById(id).ifPresent(this::delete)，找不到就靜默跳過，不會丟例外
        Todo todo = new Todo();
        todo.setTitle("測試1");
        todoRepository.save(todo);

        entityManager.flush();
        entityManager.clear();

        assertThatCode(() -> todoRepository.deleteById(-1L)).doesNotThrowAnyException();

        List<Todo> todos = todoRepository.findAll();
        assertThat(todos).hasSize(1);
    }
}

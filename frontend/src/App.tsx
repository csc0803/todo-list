import { useEffect, useState } from "react";
import type { Todo } from "./api/types";
import {
  getTodos,
  createTodo,
  toggleTodo,
  updateTodo,
  deleteTodo,
  ApiError,
} from "./api/todoApi";
import AddTodoForm from "./components/AddTodoForm";
import TodoList from "./components/TodoList";
import "./App.css";

function App() {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getTodos()
      .then(setTodos)
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "無法載入待辦事項");
      })
      .finally(() => setIsLoading(false));
  }, []);

  function handleAdd(title: string) {
    setError(null);
    createTodo({ title })
      .then((created) => setTodos((prev) => [...prev, created]))
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "新增失敗");
      });
  }

  function handleToggle(id: number) {
    setError(null);
    toggleTodo(id)
      .then((toggled) =>
        setTodos((prev) =>
          prev.map((todo) => (todo.id === id ? toggled : todo)),
        ),
      )
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "更新失敗");
      });
  }

  function handleDelete(id: number) {
    setError(null);
    deleteTodo(id)
      .then(() => setTodos((prev) => prev.filter((todo) => todo.id !== id)))
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "刪除失敗");
      });
  }

  function handleUpdate(
    id: number,
    title: string,
    description: string,
    completed: boolean,
  ) {
    setError(null);
    updateTodo(id, { title, description, completed })
      .then((updated) =>
        setTodos((prev) =>
          prev.map((todo) => (todo.id === id ? updated : todo)),
        ),
      )
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "更新失敗");
      });
  }

  return (
    <div className="container py-5" style={{ maxWidth: "600px" }}>
      <h1 className="mb-4">Todo List</h1>
      <AddTodoForm onAdd={handleAdd} />
      {error && <p className="text-danger">{error}</p>}
      {isLoading ? (
        <p className="text-muted text-center">載入中...</p>
      ) : (
        <TodoList
          todos={todos}
          onToggle={handleToggle}
          onDelete={handleDelete}
          onUpdate={handleUpdate}
        />
      )}
    </div>
  );
}

export default App;

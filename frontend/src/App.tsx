import { useState } from 'react';
import type { Todo } from './api/types';
import AddTodoForm from './components/AddTodoForm';
import TodoList from './components/TodoList';
import './App.css';

function App() {
  const [todos, setTodos] = useState<Todo[]>([]);

  function handleAdd(title: string) {
    const now = new Date().toISOString();
    const newTodo: Todo = {
      id: Date.now(),
      title,
      description: '',
      completed: false,
      createdAt: now,
      updatedAt: now,
    };
    setTodos((prev) => [...prev, newTodo]);
  }

  function handleToggle(id: number) {
    setTodos((prev) =>
      prev.map((todo) =>
        todo.id === id
          ? { ...todo, completed: !todo.completed, updatedAt: new Date().toISOString() }
          : todo
      )
    );
  }

  function handleDelete(id: number) {
    setTodos((prev) => prev.filter((todo) => todo.id !== id));
  }

  function handleUpdate(id: number, title: string) {
    setTodos((prev) =>
      prev.map((todo) =>
        todo.id === id
          ? { ...todo, title, updatedAt: new Date().toISOString() }
          : todo
      )
    );
  }

  return (
    <div className="container py-5" style={{ maxWidth: '600px' }}>
      <h1 className="mb-4">Todo List</h1>
      <AddTodoForm onAdd={handleAdd} />
      <TodoList
        todos={todos}
        onToggle={handleToggle}
        onDelete={handleDelete}
        onUpdate={handleUpdate}
      />
    </div>
  );
}

export default App;

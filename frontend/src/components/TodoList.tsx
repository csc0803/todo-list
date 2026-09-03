import type { Todo } from "../api/types";
import TodoItem from "./TodoItem";


interface TodoListProps {
  todos: Todo[];
  onToggle: (id: number) => void;
  onDelete: (id: number) => void;
  onUpdate: (id: number, title: string, description: string, completed: boolean) => void;
}

function TodoList({ todos, onToggle, onDelete, onUpdate }: TodoListProps) {
  if (todos.length === 0) {
    return <p className="text-muted text-center">目前沒有待辦事項</p>;
  }

  return (
    <ul className="list-group">
      {todos.map((todo) => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={onToggle}
          onDelete={onDelete}
          onUpdate={onUpdate}
        />
      ))}
    </ul>
  );
}

export default TodoList;

import { useState } from "react";
import type { Todo } from "../api/types";

interface TodoItemProps {
  todo: Todo;
  onToggle: (id: number) => void;
  onDelete: (id: number) => void;
  onUpdate: (id: number, title: string) => void;
}

function TodoItem({todo, onToggle, onDelete, onUpdate}: TodoItemProps){
    const [isEditing, setIsEditing] = useState(false);
    const [editValue, setEditValue] = useState(todo.title);

    function handleStartEdit() {
        setEditValue(todo.title);
        setIsEditing(true);
    }

    function handleSave() {
        const trimmed = editValue.trim();
        if (!trimmed) return;
        onUpdate(todo.id, trimmed);
        setIsEditing(false);
    }

    function handleCancel() {
        setEditValue(todo.title);
        setIsEditing(false);
    }

    return(
        <li className="list-group-item d-flex align-items-center gap-2">
            <input
                type="checkbox"
                className="form-check-input mt-0"
                checked={todo.completed}
                onChange={() => onToggle(todo.id)}
            />
            {isEditing ? (
                <>
                    <input
                        type="text"
                        className="form-control form-control-sm"
                        value={editValue}
                        onChange={(e) => setEditValue(e.target.value)}
                        autoFocus
                    />
                    <button type="button" className="btn btn-sm btn-success" onClick={handleSave}>儲存</button>
                    <button type="button" className="btn btn-sm btn-secondary" onClick={handleCancel}>取消</button>
                </>
            ) : (
                <span
                    className={`flex-grow-1 ${todo.completed ? 'text-decoration-line-through text-muted' : ''}`}
                    onClick={handleStartEdit}
                    role="button"
                >
                    {todo.title}
                </span>
            )}
            <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => onDelete(todo.id)}>
                刪除
            </button>
        </li>
    )
}

export default TodoItem;
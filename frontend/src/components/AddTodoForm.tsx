import { useState } from 'react';

interface AddTodoFormProps {
  onAdd: (title: string) => void;
}

function AddTodoForm({ onAdd }: AddTodoFormProps) {
  const [title, setTitle] = useState('');

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = title.trim();
    if (!trimmed) return;
    onAdd(trimmed);
    setTitle('');
  }

  return (
    <form className="input-group mb-4" onSubmit={handleSubmit}>
        <input
          type="text"
          className="form-control"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="新增待辦事項..."
        />
        <button type="submit" className="btn btn-primary">新增</button>
    </form>
  );
}

export default AddTodoForm;
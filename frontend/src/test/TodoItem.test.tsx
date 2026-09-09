import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { Todo } from '../api/types';
import TodoItem from '../components/TodoItem';

const baseTodo: Todo = {
  id: 1,
  title: '買牛奶',
  description: '',
  completed: false,
  createdAt: '2026-09-01T00:00:00Z',
  updatedAt: '2026-09-01T00:00:00Z',
};

describe('TodoItem', () => {
  it('calls onToggle with the todo id when the checkbox is clicked', async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();
    render(
      <ul>
        <TodoItem todo={baseTodo} onToggle={onToggle} onDelete={vi.fn()} onUpdate={vi.fn()} />
      </ul>,
    );

    await user.click(screen.getByRole('checkbox'));

    expect(onToggle).toHaveBeenCalledWith(1);
  });

  it('calls onDelete with the todo id when the delete button is clicked', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();
    render(
      <ul>
        <TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={onDelete} onUpdate={vi.fn()} />
      </ul>,
    );

    await user.click(screen.getByRole('button', { name: '刪除' }));

    expect(onDelete).toHaveBeenCalledWith(1);
  });

  it('enters edit mode on title click and saves the trimmed title, keeping description/completed', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn();
    render(
      <ul>
        <TodoItem
          todo={{ ...baseTodo, description: '全脂', completed: true }}
          onToggle={vi.fn()}
          onDelete={vi.fn()}
          onUpdate={onUpdate}
        />
      </ul>,
    );

    await user.click(screen.getByText('買牛奶'));
    const input = screen.getByRole('textbox');
    await user.clear(input);
    await user.type(input, '  買豆漿  ');
    await user.click(screen.getByRole('button', { name: '儲存' }));

    expect(onUpdate).toHaveBeenCalledWith(1, '買豆漿', '全脂', true);
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
  });

  it('reverts to the original title and exits edit mode on cancel', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn();
    render(
      <ul>
        <TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={vi.fn()} onUpdate={onUpdate} />
      </ul>,
    );

    await user.click(screen.getByText('買牛奶'));
    await user.type(screen.getByRole('textbox'), '被丟棄的內容');
    await user.click(screen.getByRole('button', { name: '取消' }));

    expect(onUpdate).not.toHaveBeenCalled();
    expect(screen.getByText('買牛奶')).toBeInTheDocument();
  });

  it('does not save when the edited title is empty or only whitespace', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn();
    render(
      <ul>
        <TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={vi.fn()} onUpdate={onUpdate} />
      </ul>,
    );

    await user.click(screen.getByText('買牛奶'));
    const input = screen.getByRole('textbox');
    await user.clear(input);
    await user.type(input, '   ');
    await user.click(screen.getByRole('button', { name: '儲存' }));

    expect(onUpdate).not.toHaveBeenCalled();
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import AddTodoForm from '../components/AddTodoForm';

describe('AddTodoForm', () => {
  it('calls onAdd with the trimmed title and clears the input on submit', async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<AddTodoForm onAdd={onAdd} />);

    const input = screen.getByPlaceholderText('新增待辦事項...');
    await user.type(input, '  買牛奶  ');
    await user.click(screen.getByRole('button', { name: '新增' }));

    expect(onAdd).toHaveBeenCalledWith('買牛奶');
    expect(input).toHaveValue('');
  });

  it('does not call onAdd when the title is empty or only whitespace', async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<AddTodoForm onAdd={onAdd} />);

    await user.click(screen.getByRole('button', { name: '新增' }));

    const input = screen.getByPlaceholderText('新增待辦事項...');
    await user.type(input, '   ');
    await user.click(screen.getByRole('button', { name: '新增' }));

    expect(onAdd).not.toHaveBeenCalled();
  });
});

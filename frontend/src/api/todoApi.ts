import type { Todo, TodoInput, TodoUpdateInput } from './types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    const body: ApiErrorBody = await response.json();
    throw new ApiError(response.status, body.message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export function getTodos(completed?: boolean): Promise<Todo[]> {
  const query = completed === undefined ? '' : `?completed=${completed}`;
  return request<Todo[]>(`/api/todos${query}`);
}

export function getTodo(id: number): Promise<Todo> {
  return request<Todo>(`/api/todos/${id}`);
}

export function createTodo(input: TodoInput): Promise<Todo> {
  return request<Todo>('/api/todos', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateTodo(id: number, input: TodoUpdateInput): Promise<Todo> {
  return request<Todo>(`/api/todos/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  });
}

export function toggleTodo(id: number): Promise<Todo> {
  return request<Todo>(`/api/todos/${id}/toggle`, {
    method: 'PATCH',
  });
}

export function deleteTodo(id: number): Promise<void> {
  return request<void>(`/api/todos/${id}`, {
    method: 'DELETE',
  });
}

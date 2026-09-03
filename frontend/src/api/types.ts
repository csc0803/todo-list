export interface Todo{
    id: number,
    title: string,
    description: string,
    completed: boolean,
    createdAt: string,
    updatedAt: string,
}

export interface TodoInput {
    title: string;
    description?: string;
}

export interface TodoUpdateInput extends TodoInput {
    completed: boolean;
}
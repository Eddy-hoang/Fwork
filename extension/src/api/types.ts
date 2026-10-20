export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
  errors?: Record<string, string> | null;
  localDateTime?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface BoardResponse {
  id: string;
  title: string;
  description?: string;
  color?: string;
  workspaceId?: string;
  position?: number;
  isArchived?: boolean;
}

export interface BoardColumnResponse {
  id: string;
  name: string;
  position?: number;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  dueDate?: string | null;
  position?: number;
}

export interface TaskResponse {
  id: string;
  title: string;
  description?: string;
  position?: number;
  priority?: string;
  columnId: string;
  createdAt?: string;
}

export interface ExtensionSession {
  token: string;
  user?: UserResponse;
  apiBaseUrl?: string;
  webBaseUrl?: string;
}

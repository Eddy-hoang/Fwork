import { ApiResponse, BoardColumnResponse, BoardResponse, CreateTaskRequest, LoginRequest, LoginResponse, TaskResponse } from './types';

export const DEFAULT_API_BASE = 'https://backend-8vi0.onrender.com/api';
export const DEFAULT_WEB_BASE = 'https://fwork-web.vercel.app';

export class AuthenticationError extends Error {
  constructor(message = 'Session expired or invalid credentials. Please log in again.') {
    super(message);
    this.name = 'AuthenticationError';
  }
}

export class ForbiddenError extends Error {
  constructor(message = 'You do not have permission to perform this action.') {
    super(message);
    this.name = 'ForbiddenError';
  }
}

export class ValidationError extends Error {
  errors?: Record<string, string>;
  constructor(message: string, errors?: Record<string, string>) {
    super(message);
    this.name = 'ValidationError';
    this.errors = errors;
  }
}

export class NetworkError extends Error {
  constructor(message = 'Could not connect to Fwork server. Please check your network or server status.') {
    super(message);
    this.name = 'NetworkError';
  }
}

export class ApiClient {
  private baseUrl: string;
  private token: string | null = null;

  constructor(baseUrl: string = DEFAULT_API_BASE, token: string | null = null) {
    this.baseUrl = baseUrl.replace(/\/+$/, '');
    this.token = token;
  }

  setToken(token: string | null) {
    this.token = token;
  }

  setBaseUrl(url: string) {
    this.baseUrl = url.replace(/\/+$/, '');
  }

  getBaseUrl(): string {
    return this.baseUrl;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.baseUrl}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers as Record<string, string> || {}),
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    let response: Response;
    try {
      response = await fetch(url, {
        ...options,
        headers,
      });
    } catch (err) {
      throw new NetworkError();
    }

    let json: ApiResponse<T> | null = null;
    try {
      json = await response.json();
    } catch {
      // Non-JSON response
    }

    if (!response.ok) {
      const message = json?.message || `Request failed with status ${response.status}`;

      if (response.status === 401) {
        throw new AuthenticationError(message);
      }
      if (response.status === 403) {
        throw new ForbiddenError(message);
      }
      if (response.status === 400) {
        const fieldErrors = (json?.errors && typeof json.errors === 'object')
          ? (json.errors as Record<string, string>)
          : undefined;
        throw new ValidationError(message, fieldErrors);
      }

      throw new Error(message);
    }

    if (json && typeof json === 'object' && 'success' in json) {
      if (!json.success) {
        throw new Error(json.message || 'Operation failed');
      }
      return json.data;
    }

    return (json as unknown) as T;
  }

  async login(request: LoginRequest): Promise<LoginResponse> {
    return this.request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async loginWithGoogle(idToken: string): Promise<LoginResponse> {
    return this.request<LoginResponse>('/auth/google', {
      method: 'POST',
      body: JSON.stringify({ token: idToken }),
    });
  }

  async getBoards(): Promise<BoardResponse[]> {
    return this.request<BoardResponse[]>('/boards', {
      method: 'GET',
    });
  }

  async getColumns(boardId: string): Promise<BoardColumnResponse[]> {
    return this.request<BoardColumnResponse[]>(`/boards/${boardId}/columns`, {
      method: 'GET',
    });
  }

  async createTask(columnId: string, request: CreateTaskRequest): Promise<TaskResponse> {
    return this.request<TaskResponse>(`/columns/${columnId}/tasks`, {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }
}

export const api = new ApiClient();

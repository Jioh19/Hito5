export enum UserStatus {
  ACTIVE = "ACTIVE",
  PENDING = "PENDING",
  INACTIVE = "INACTIVE",
}

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface RegisterUserDTO {
  username: string;
  password: string;
  email: string;
}

export interface LoginUserDTO {
  username: string;
  password: string;
}

export interface ApiErrorResponse {
  message: string;
  code?: string;
  errorCode?: string;
  timestamp?: string;
}

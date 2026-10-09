import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private router = inject(Router);

  token = signal<string | null>(null);
  role  = signal<string | null>(null);

  constructor(private http: HttpClient) {
    const storedToken = localStorage.getItem('token');
    const storedRole  = localStorage.getItem('role');
    if (storedToken && !this.isTokenExpired(storedToken)) {
      this.token.set(storedToken);
      this.role.set(storedRole);
    } else {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
    }
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role',  res.role);
        this.token.set(res.token);
        this.role.set(res.role);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.token.set(null);
    this.role.set(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const t = this.token();
    if (!t) return false;
    if (this.isTokenExpired(t)) {
      this.logout();
      return false;
    }
    return true;
  }

  isAdmin(): boolean { return this.role() === 'admin'; }

  private isTokenExpired(token: string): boolean {
    try {
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) return true;
      const payload = JSON.parse(atob(payloadBase64));
      // exp is in seconds
      return typeof payload.exp === 'number' && payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}

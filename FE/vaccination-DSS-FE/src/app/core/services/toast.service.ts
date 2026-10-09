import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'warning';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  readonly toasts: Toast[] = [];
  readonly change$ = new Subject<void>();

  success(message: string): void { this._add(message, 'success'); }
  error(message: string): void   { this._add(message, 'error'); }
  warning(message: string): void { this._add(message, 'warning'); }

  dismiss(id: number): void {
    const idx = this.toasts.findIndex(t => t.id === id);
    if (idx !== -1) { this.toasts.splice(idx, 1); this.change$.next(); }
  }

  private _add(message: string, type: ToastType): void {
    const toast: Toast = { id: ++this.counter, message, type };
    this.toasts.push(toast);
    this.change$.next();
    setTimeout(() => this.dismiss(toast.id), 3500);
  }
}

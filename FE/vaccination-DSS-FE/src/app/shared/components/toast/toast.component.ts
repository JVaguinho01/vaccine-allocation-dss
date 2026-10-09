import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { Subscription } from 'rxjs';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [NgClass],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
})
export class ToastComponent implements OnInit, OnDestroy {
  private toastService = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);
  private sub!: Subscription;

  get toasts(): Toast[] { return this.toastService.toasts; }

  ngOnInit(): void {
    this.sub = this.toastService.change$.subscribe(() => this.cdr.markForCheck());
  }

  ngOnDestroy(): void { this.sub.unsubscribe(); }

  dismiss(id: number): void { this.toastService.dismiss(id); }
}

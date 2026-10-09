import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = false;
  errorMessage = '';
  showPassword = false;

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor() {}

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value.email!, this.form.value.password!).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Invalid email or password.';
      },
    });
  }
}

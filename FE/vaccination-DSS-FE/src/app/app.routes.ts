import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { RegionsComponent } from './features/regions/regions.component';
import { InstitutionsComponent } from './features/institutions/institutions.component';
import { PatientsComponent } from './features/patients/patients.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard',    component: DashboardComponent },
      { path: 'regions',      component: RegionsComponent },
      { path: 'institutions', component: InstitutionsComponent },
      { path: 'patients',     component: PatientsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: '' },
];

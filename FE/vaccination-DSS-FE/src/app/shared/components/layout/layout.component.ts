import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { ToastComponent } from '../toast/toast.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, ToastComponent],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css',
})
export class LayoutComponent {}

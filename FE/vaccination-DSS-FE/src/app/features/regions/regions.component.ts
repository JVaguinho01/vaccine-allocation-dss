import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RegionService } from '../../core/services/region.service';
import { Region } from '../../core/models/region.model';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-regions',
  standalone: true,
  imports: [FormsModule, PaginationComponent],
  templateUrl: './regions.component.html',
  styleUrl: './regions.component.css',
})
export class RegionsComponent implements OnInit {
  regions: Region[] = [];
  loading = true;

  search = '';
  page = 0;
  size = 50;
  totalElements = 0;
  totalPages = 0;
  sortField = 'name';
  sortDir = 'asc';

  private searchTimeout: any;

  constructor(private regionService: RegionService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.regionService.getPage({
      page: this.page, size: this.size,
      search: this.search, sort: this.sortField, dir: this.sortDir,
    }).subscribe({
      next: res => {
        this.regions = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); },
    });
  }

  toggleSort(field: string): void {
    if (this.sortField === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDir = 'asc';
    }
    this.page = 0;
    this.load();
  }

  sortIcon(field: string): string {
    if (this.sortField !== field) return '↕';
    return this.sortDir === 'asc' ? '↑' : '↓';
  }

  onSearchChange(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => { this.page = 0; this.load(); }, 400);
  }

  onPageChange(p: number): void { this.page = p; this.load(); }
}

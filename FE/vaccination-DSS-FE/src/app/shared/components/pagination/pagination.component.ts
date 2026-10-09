import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css',
})
export class PaginationComponent implements OnChanges {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() size = 50;
  @Output() pageChange = new EventEmitter<number>();

  inputPage = 1;

  ngOnChanges(): void {
    this.inputPage = this.page + 1;
  }

  get from(): number { return this.totalElements === 0 ? 0 : this.page * this.size + 1; }
  get to():   number { return Math.min((this.page + 1) * this.size, this.totalElements); }

  prev(): void { if (this.page > 0) this.pageChange.emit(this.page - 1); }
  next(): void { if (this.page < this.totalPages - 1) this.pageChange.emit(this.page + 1); }

  onInputChange(): void {
    const p = Math.round(this.inputPage);
    if (isNaN(p)) { this.inputPage = this.page + 1; return; }
    const clamped = Math.max(1, Math.min(p, this.totalPages || 1));
    this.inputPage = clamped;
    this.pageChange.emit(clamped - 1);
  }
}

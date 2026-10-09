import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { CustomSelectComponent, SelectOption } from '../../shared/components/custom-select/custom-select.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { InstitutionService } from '../../core/services/institution.service';
import { RegionService } from '../../core/services/region.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { Institution } from '../../core/models/institution.model';
import { Region } from '../../core/models/region.model';

type SortField = 'name' | 'regionName' | 'storageCapacity' | 'dailyVaccinationRate';

function storageGeVaccinationValidator(group: AbstractControl): ValidationErrors | null {
  const rate = group.get('dailyVaccinationRate')?.value;
  const stor = group.get('storageCapacity')?.value;
  if (rate != null && stor != null && stor < rate) {
    return { storageBelowVaccination: true };
  }
  return null;
}

@Component({
  selector: 'app-institutions',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, CustomSelectComponent, PaginationComponent],
  templateUrl: './institutions.component.html',
  styleUrl: './institutions.component.css',
})
export class InstitutionsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private institutionService = inject(InstitutionService);
  private regionService = inject(RegionService);
  private cdr = inject(ChangeDetectorRef);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  institutions: Institution[] = [];
  regions: Region[] = [];
  loading = true;
  savingModal = false;
  deletingId: string | null = null;
  confirmDeleteId: string | null = null;
  confirmDeleteName = '';

  search = '';
  regionFilter = '';
  sortField: SortField = 'name';
  sortDir: 'asc' | 'desc' = 'asc';

  page = 0;
  size = 50;
  totalElements = 0;
  totalPages = 0;

  modalOpen = false;
  editingId: string | null = null;

  private searchTimeout: any;

  form = this.fb.group({
    name:                 ['', Validators.required],
    regionId:             ['', Validators.required],
    dailyVaccinationRate: [null as number | null, [Validators.required, Validators.min(1)]],
    storageCapacity:      [null as number | null, [Validators.required, Validators.min(1)]],
    address:              ['' as string | null],
    zipCode:              ['' as string | null],
  }, { validators: storageGeVaccinationValidator });

  get storageCapacityError(): boolean {
    return this.form.hasError('storageBelowVaccination') &&
      (this.form.get('storageCapacity')?.touched ?? false);
  }

  constructor() {}

  get regionFilterOptions(): SelectOption[] {
    return [{ value: '', label: 'All regions' }, ...this.regions.map(r => ({ value: r.id, label: r.name }))];
  }

  get regionSelectOptions(): SelectOption[] {
    return this.regions.map(r => ({ value: r.id, label: r.name }));
  }

  ngOnInit(): void {
    this.load();
    this.regionService.getAll().subscribe(r => { this.regions = r; this.cdr.markForCheck(); });
  }

  load(): void {
    this.loading = true;
    this.institutionService.getPage({
      page: this.page, size: this.size,
      search: this.search, regionId: this.regionFilter,
      sort: this.sortField, dir: this.sortDir,
    }).subscribe({
      next: res => {
        this.institutions = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); },
    });
  }

  onSearchChange(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => { this.page = 0; this.load(); }, 400);
  }

  onFilterChange(): void { this.page = 0; this.load(); }
  onPageChange(p: number): void { this.page = p; this.load(); }

  toggleSort(field: SortField): void {
    if (this.sortField === field) this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    else { this.sortField = field; this.sortDir = 'asc'; }
    this.page = 0;
    this.load();
  }

  sortIcon(field: SortField): string {
    if (this.sortField !== field) return '↕';
    return this.sortDir === 'asc' ? '↑' : '↓';
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset();
    this.form.get('regionId')?.enable();
    this.modalOpen = true;
  }

  openEdit(inst: Institution): void {
    this.editingId = inst.id;
    this.form.patchValue({
      name: inst.name,
      regionId: inst.regionId,
      dailyVaccinationRate: inst.dailyVaccinationRate,
      storageCapacity: inst.storageCapacity,
      address: inst.address ?? '',
      zipCode: inst.zipCode ?? '',
    });
    this.form.get('regionId')?.disable();
    this.modalOpen = true;
  }

  closeModal(): void { this.modalOpen = false; }

  save(): void {
    if (this.form.invalid) return;
    this.savingModal = true;
    const v = this.form.getRawValue();

    if (this.editingId) {
      this.institutionService.update(this.editingId, {
        name: v.name!,
        dailyVaccinationRate: v.dailyVaccinationRate!,
        storageCapacity: v.storageCapacity!,
        address: v.address || undefined,
        zipCode: v.zipCode || undefined,
      }).subscribe({
        next: () => {
          this.savingModal = false;
          this.modalOpen = false;
          this.toast.success('Institution updated successfully');
          this.load();
          this.cdr.markForCheck();
        },
        error: () => {
          this.savingModal = false;
          this.toast.error('Failed to update institution');
          this.cdr.markForCheck();
        },
      });
    } else {
      this.institutionService.create({
        name: v.name!,
        regionId: v.regionId!,
        dailyVaccinationRate: v.dailyVaccinationRate!,
        storageCapacity: v.storageCapacity!,
        address: v.address || undefined,
        zipCode: v.zipCode || undefined,
      }).subscribe({
        next: () => {
          this.savingModal = false;
          this.modalOpen = false;
          this.toast.success('Institution created successfully');
          this.load();
          this.cdr.markForCheck();
        },
        error: () => {
          this.savingModal = false;
          this.toast.error('Failed to create institution');
          this.cdr.markForCheck();
        },
      });
    }
  }

  askDelete(inst: Institution): void {
    this.confirmDeleteId = inst.id;
    this.confirmDeleteName = inst.name;
  }

  cancelDelete(): void { this.confirmDeleteId = null; }

  confirmDelete(): void {
    if (!this.confirmDeleteId) return;
    const id = this.confirmDeleteId;
    this.deletingId = id;
    this.confirmDeleteId = null;
    this.institutionService.delete(id).subscribe({
      next: () => {
        this.deletingId = null;
        this.toast.success('Institution removed');
        this.load();
        this.cdr.markForCheck();
      },
      error: () => {
        this.deletingId = null;
        this.toast.error('Failed to remove institution');
        this.cdr.markForCheck();
      },
    });
  }
}

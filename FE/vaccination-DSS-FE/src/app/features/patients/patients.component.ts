import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CustomSelectComponent, SelectOption } from '../../shared/components/custom-select/custom-select.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { PatientService } from '../../core/services/patient.service';
import { RegionService } from '../../core/services/region.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { Patient } from '../../core/models/patient.model';
import { Region } from '../../core/models/region.model';

type SortField = 'fullName' | 'regionName' | 'gender';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [NgClass, FormsModule, ReactiveFormsModule, CustomSelectComponent, PaginationComponent],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.css',
})
export class PatientsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private regionService = inject(RegionService);
  private cdr = inject(ChangeDetectorRef);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  patients: Patient[] = [];
  regions: Region[] = [];
  loading = true;
  savingModal = false;
  deletingId: string | null = null;
  confirmDeleteId: string | null = null;
  confirmDeleteName = '';

  search = '';
  regionFilter = '';
  genderFilter = '';
  riskLevelFilter = '';
  riskExposureFilter = '';
  sortField: SortField = 'fullName';
  sortDir: 'asc' | 'desc' = 'asc';

  page = 0;
  size = 50;
  totalElements = 0;
  totalPages = 0;

  modalOpen = false;
  editingPatient: Patient | null = null;

  private searchTimeout: any;

  form = this.fb.group({
    riskLevel:    [null as number | null, [Validators.min(1), Validators.max(5)]],
    riskExposure: [null as number | null, [Validators.min(1), Validators.max(5)]],
  });

  constructor() {}

  ngOnInit(): void {
    this.load();
    this.regionService.getAll().subscribe(r => { this.regions = r; this.cdr.markForCheck(); });
  }

  get regionFilterOptions(): SelectOption[] {
    return [{ value: '', label: 'All regions' }, ...this.regions.map(r => ({ value: r.id, label: r.name }))];
  }

  get regionSelectOptions(): SelectOption[] {
    return this.regions.map(r => ({ value: r.id, label: r.name }));
  }

  get genderOptions(): SelectOption[] {
    return [
      { value: '', label: 'All genders' },
      { value: 'M', label: 'Male' },
      { value: 'F', label: 'Female' },
    ];
  }

  get riskOptions(): SelectOption[] {
    return [
      { value: '', label: 'All risks' },
      { value: 'low', label: 'Low' },
      { value: 'medium', label: 'Medium' },
      { value: 'high', label: 'High' },
    ];
  }

  load(): void {
    this.loading = true;
    this.patientService.getPage({
      page: this.page, size: this.size,
      search: this.search, regionId: this.regionFilter,
      gender: this.genderFilter,
      riskLevel: this.riskLevelFilter, riskExposure: this.riskExposureFilter,
      sort: this.sortField, dir: this.sortDir,
    }).subscribe({
      next: res => {
        this.patients = res.content;
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

  openEdit(patient: Patient): void {
    this.editingPatient = patient;
    this.form.patchValue({
      riskLevel:    patient.riskLevel    ?? null,
      riskExposure: patient.riskExposure ?? null,
    });
    this.modalOpen = true;
  }

  closeModal(): void { this.modalOpen = false; this.editingPatient = null; }

  save(): void {
    if (!this.editingPatient || this.form.invalid) return;
    this.savingModal = true;
    const v = this.form.getRawValue();
    // Preserve all existing patient fields; only riskLevel is editable here
    this.patientService.update(this.editingPatient.id, {
      regionId:     this.editingPatient.regionId,
      address:      this.editingPatient.address      ?? undefined,
      zipCode:      this.editingPatient.zipCode      ?? undefined,
      riskLevel:    v.riskLevel    ?? undefined,
      riskExposure: v.riskExposure ?? undefined,
    }).subscribe({
      next: () => {
        this.savingModal = false;
        this.modalOpen = false;
        this.editingPatient = null;
        this.toast.success('Patient updated successfully');
        this.load();
        this.cdr.markForCheck();
      },
      error: () => {
        this.savingModal = false;
        this.toast.error('Failed to update patient');
        this.cdr.markForCheck();
      },
    });
  }

  askDelete(patient: Patient): void {
    this.confirmDeleteId = patient.id;
    this.confirmDeleteName = `${patient.firstName} ${patient.lastName}`;
  }

  cancelDelete(): void { this.confirmDeleteId = null; }

  confirmDelete(): void {
    if (!this.confirmDeleteId) return;
    const id = this.confirmDeleteId;
    this.deletingId = id;
    this.confirmDeleteId = null;
    this.patientService.delete(id).subscribe({
      next: () => {
        this.deletingId = null;
        this.toast.success('Patient deactivated');
        this.load();
        this.cdr.markForCheck();
      },
      error: () => {
        this.deletingId = null;
        this.toast.error('Failed to deactivate patient');
        this.cdr.markForCheck();
      },
    });
  }

  riskLabel(val: number | undefined): string {
    if (val == null) return '—';
    if (val <= 2) return 'Low';
    if (val === 3) return 'Medium';
    return 'High';
  }

  riskClass(val: number | undefined): string {
    if (val == null) return '';
    if (val <= 2) return 'risk-low';
    if (val === 3) return 'risk-med';
    return 'risk-high';
  }
}

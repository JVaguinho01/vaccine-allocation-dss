import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { CdkDropList, CdkDrag, CdkDragHandle, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { RegionService } from '../../core/services/region.service';
import { AhpService } from '../../core/services/ahp.service';
import { Region } from '../../core/models/region.model';
import { AhpResponse, Criterion } from '../../core/models/ahp.model';
import { CustomSelectComponent, SelectOption } from '../../shared/components/custom-select/custom-select.component';

interface PaceOption {
  value: 'relaxed' | 'standard' | 'intensive';
  label: string;
  storagePct: string;
  ratePct: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, DecimalPipe, CdkDropList, CdkDrag, CdkDragHandle, CustomSelectComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  regions: Region[] = [];
  selectedRegionId = '';
  totalVaccines: number | null = null;
  campaignDays = 30;
  vaccinationPace: 'relaxed' | 'standard' | 'intensive' = 'intensive';
  loading = false;
  error = '';
  result: AhpResponse | null = null;
  animatedWeights: Record<string, number> = {};

  restockTooltipVisible = false;
  restockTooltipDays: number[] = [];
  restockTooltipDoses = 0;
  restockTooltipTop = 0;
  restockTooltipLeft = 0;

  criteria: Criterion[] = [
    { id: 1, name: 'Demand' },
    { id: 2, name: 'Risk Level' },
    { id: 3, name: 'Risk Exposure' },
    { id: 4, name: 'Logistic Cost' },
    { id: 5, name: 'Capacity' },
    { id: 6, name: 'Waste Risk' },
  ];

  paceOptions: PaceOption[] = [
    { value: 'relaxed',   label: 'Relaxed',   storagePct: '30%', ratePct: '50%'  },
    { value: 'standard',  label: 'Standard',  storagePct: '55%', ratePct: '75%'  },
    { value: 'intensive', label: 'Intensive', storagePct: '85%', ratePct: '100%' },
  ];

  constructor(
    private regionService: RegionService,
    private ahpService: AhpService,
    private cdr: ChangeDetectorRef,
  ) {}

  get regionOptions(): SelectOption[] {
    return this.regions.map(r => ({ value: r.id, label: r.name }));
  }

  ngOnInit(): void {
    this.regionService.getAll().subscribe({
      next: regions => {
        this.regions = regions;
        this.cdr.markForCheck();
      },
    });
  }

  onDrop(event: CdkDragDrop<Criterion[]>): void {
    moveItemInArray(this.criteria, event.previousIndex, event.currentIndex);
  }

  get canRun(): boolean {
    return !!this.selectedRegionId
        && !!this.totalVaccines
        && this.totalVaccines >= 1
        && this.campaignDays >= 1;
  }

  run(): void {
    if (!this.canRun) return;
    this.loading = true;
    this.error = '';
    this.result = null;
    this.animatedWeights = {};

    this.ahpService.allocate({
      regionId: this.selectedRegionId,
      totalVaccinesAvailable: this.totalVaccines!,
      criteriaOrder: this.criteria.map(c => c.id),
      campaignDays: this.campaignDays,
      vaccinationPace: this.vaccinationPace,
    }).subscribe({
      next: res => {
        this.loading = false;
        this.result = res;
        this.cdr.markForCheck();
        setTimeout(() => {
          const map: Record<string, number> = {};
          res.weights.forEach(w => { map[w.criteria] = +(w.weight * 100).toFixed(2); });
          this.animatedWeights = { ...map };
          this.cdr.markForCheck();
        }, 80);
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to run allocation. Please try again.';
        this.cdr.markForCheck();
      },
    });
  }

  get sortedWeights() {
    if (!this.result) return [];
    return [...this.result.weights].sort((a, b) => b.weight - a.weight);
  }

  get selectedRegionName(): string {
    return this.regions.find(r => r.id === this.selectedRegionId)?.name ?? '';
  }

  get selectedPaceLabel(): string {
    return this.paceOptions.find(p => p.value === this.vaccinationPace)?.label ?? '';
  }

  // High occupancy means the institution is under pressure, so red is high here.
  utilizationClass(allocated: number, effectiveCapacity: number): string {
    if (!effectiveCapacity) return 'yellow';
    const pct = allocated / effectiveCapacity;
    if (pct > 0.80) return 'red';
    if (pct > 0.50) return 'yellow';
    return 'green';
  }

  utilizationPct(allocated: number, effectiveCapacity: number): string {
    if (!effectiveCapacity) return '—';
    return (allocated / effectiveCapacity * 100).toFixed(0) + '%';
  }

  showRestockTooltip(event: MouseEvent, days: number[], doses: number): void {
    const rect = (event.currentTarget as Element).getBoundingClientRect();
    this.restockTooltipDays  = days;
    this.restockTooltipDoses = doses;
    this.restockTooltipTop   = rect.top;
    this.restockTooltipLeft  = rect.left + rect.width / 2;
    this.restockTooltipVisible = true;
    this.cdr.markForCheck();
  }

  hideRestockTooltip(): void {
    this.restockTooltipVisible = false;
    this.cdr.markForCheck();
  }

  get unvaccinatedPatients(): number {
    if (!this.result) return 0;
    return Math.max(0, this.result.totalPatients - this.result.totalAllocated);
  }
}

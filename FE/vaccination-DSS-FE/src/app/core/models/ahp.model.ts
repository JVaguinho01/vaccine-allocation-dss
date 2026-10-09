export interface AhpRequest {
  regionId: string;
  totalVaccinesAvailable: number;
  criteriaOrder: number[];
  campaignDays: number;
  vaccinationPace: 'relaxed' | 'standard' | 'intensive';
}

export interface AhpResponse {
  allocations: AllocationResult[];
  weights: CriteriaWeight[];
  consistency: Consistency;
  totalAllocated: number;
  totalUnallocated: number;
  totalPatients: number;
}

export interface AllocationResult {
  institutionId: string;
  institutionName: string;
  finalScore: number;
  allocatedVaccines: number;
  effectiveCapacity: number;
  completionDay: number;
  restockDays: number[];
  restockCount: number;
  restockDoses: number;
}

export interface CriteriaWeight {
  criteria: string;
  weight: number;
}

export interface Consistency {
  consistencyIndex: number;
  consistencyRatio: number;
  consistent: boolean;
}

export interface Criterion {
  id: number;
  name: string;
}

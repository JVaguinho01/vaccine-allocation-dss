export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  gender: string;
  regionId: string;
  regionName: string;
  address: string | null;
  zipCode: string | null;
  riskLevel: number;
  riskExposure: number;
}

export interface UpdatePatient {
  regionId: string;
  address?: string;
  zipCode?: string;
  riskLevel?: number;
  riskExposure?: number;
}

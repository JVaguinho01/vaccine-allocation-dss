export interface Institution {
  id: string;
  name: string;
  regionId: string;
  regionName: string;
  dailyVaccinationRate: number;
  storageCapacity: number;
  address?: string;
  zipCode?: string;
}

export interface CreateInstitution {
  name: string;
  regionId: string;
  dailyVaccinationRate: number;
  storageCapacity: number;
  address?: string;
  zipCode?: string;
}

export interface UpdateInstitution {
  name: string;
  dailyVaccinationRate: number;
  storageCapacity: number;
  address?: string;
  zipCode?: string;
}

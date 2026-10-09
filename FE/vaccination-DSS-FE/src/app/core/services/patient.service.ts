import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Patient, UpdatePatient } from '../models/patient.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private url = `${environment.apiUrl}/patients`;
  constructor(private http: HttpClient) {}

  getPage(params: {
    page: number; size: number;
    search?: string; regionId?: string; gender?: string;
    riskLevel?: string; riskExposure?: string;
    sort?: string; dir?: string;
  }): Observable<PageResponse<Patient>> {
    const p = new HttpParams()
      .set('page',        params.page)
      .set('size',        params.size)
      .set('search',      params.search      ?? '')
      .set('regionId',    params.regionId    ?? '')
      .set('gender',      params.gender      ?? '')
      .set('riskLevel',   params.riskLevel   ?? '')
      .set('riskExposure',params.riskExposure?? '')
      .set('sort',        params.sort        ?? 'fullName')
      .set('dir',         params.dir         ?? 'asc');
    return this.http.get<PageResponse<Patient>>(this.url, { params: p });
  }

  update(id: string, dto: UpdatePatient): Observable<Patient> {
    return this.http.put<Patient>(`${this.url}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

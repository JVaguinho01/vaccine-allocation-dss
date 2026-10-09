import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Institution, CreateInstitution, UpdateInstitution } from '../models/institution.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class InstitutionService {
  private url = `${environment.apiUrl}/institutions`;
  constructor(private http: HttpClient) {}

  getPage(params: {
    page: number; size: number;
    search?: string; regionId?: string;
    sort?: string; dir?: string;
  }): Observable<PageResponse<Institution>> {
    const p = new HttpParams()
      .set('page',     params.page)
      .set('size',     params.size)
      .set('search',   params.search   ?? '')
      .set('regionId', params.regionId ?? '')
      .set('sort',     params.sort     ?? 'name')
      .set('dir',      params.dir      ?? 'asc');
    return this.http.get<PageResponse<Institution>>(this.url, { params: p });
  }

  create(dto: CreateInstitution): Observable<Institution> {
    return this.http.post<Institution>(this.url, dto);
  }

  update(id: string, dto: UpdateInstitution): Observable<Institution> {
    return this.http.put<Institution>(`${this.url}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

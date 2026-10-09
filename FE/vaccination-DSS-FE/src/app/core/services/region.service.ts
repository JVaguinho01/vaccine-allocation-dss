import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Region } from '../models/region.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class RegionService {
  private url = `${environment.apiUrl}/regions`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Region[]> {
    return this.http.get<Region[]>(this.url);
  }

  getPage(params: {
    page: number; size: number;
    search?: string; sort?: string; dir?: string;
  }): Observable<PageResponse<Region>> {
    const p = new HttpParams()
      .set('page', params.page)
      .set('size', params.size)
      .set('search', params.search ?? '')
      .set('sort',   params.sort   ?? 'name')
      .set('dir',    params.dir    ?? 'asc');
    return this.http.get<PageResponse<Region>>(`${this.url}/page`, { params: p });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AhpRequest, AhpResponse } from '../models/ahp.model';

@Injectable({ providedIn: 'root' })
export class AhpService {
  private url = `${environment.apiUrl}/allocation/ahp`;
  constructor(private http: HttpClient) {}

  allocate(request: AhpRequest): Observable<AhpResponse> {
    return this.http.post<AhpResponse>(this.url, request);
  }
}

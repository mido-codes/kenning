import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { Document } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Document[]> {
    return this.http.get<Document[]>('/api/documents');
  }

  upload(file: File): Observable<Document> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<Document>('/api/documents', form);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/documents/${id}`);
  }
}

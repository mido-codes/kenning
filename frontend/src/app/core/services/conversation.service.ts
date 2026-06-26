import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import type { Observable } from 'rxjs';
import type { Conversation, ConversationDetail, Message } from '../models/conversation.model';

@Injectable({ providedIn: 'root' })
export class ConversationService {
  private readonly http = inject(HttpClient);

  create(documentId: string): Observable<Conversation> {
    return this.http.post<Conversation>('/api/conversations', null, {
      params: new HttpParams().set('documentId', documentId),
    });
  }

  get(id: string): Observable<ConversationDetail> {
    return this.http.get<ConversationDetail>(`/api/conversations/${id}`);
  }

  sendMessage(conversationId: string, question: string): Observable<Message> {
    return this.http.post<Message>(
      `/api/conversations/${conversationId}/message`,
      null,
      { params: new HttpParams().set('question', question) },
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/conversations/${id}`);
  }
}

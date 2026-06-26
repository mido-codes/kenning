import type { Document } from './document.model';

export interface Conversation {
  id: string;
  document: Document;
  title: string;
  createdAt: string;
}

export interface SourceReference {
  content: string;
  similarity: number;
}

export interface Message {
  id: string;
  conversation: { id: string };
  role: 'USER' | 'ASSISTANT';
  content: string;
  sources: string | null;
  createdAt: string;
}

export interface ConversationDetail {
  conversation: Conversation;
  messages: Message[];
}

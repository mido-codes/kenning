export type DocumentStatus = 'PROCESSING' | 'READY' | 'FAILED';

export interface Document {
  id: string;
  filename: string;
  contentType: string;
  sizeBytes: number;
  status: DocumentStatus;
  errorMessage: string | null;
  createdAt: string;
}

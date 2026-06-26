import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { FileUpload } from 'primeng/fileupload';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DocumentService } from '../../core/services/document.service';
import { ConversationService } from '../../core/services/conversation.service';
import type { Document } from '../../core/models/document.model';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';

type TagSeverity =
  | 'success'
  | 'info'
  | 'warn'
  | 'danger'
  | 'secondary'
  | 'contrast';

@Component({
  selector: 'app-document-list',
  templateUrl: './document-list.html',
  styleUrl: './document-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TableModule,
    Button,
    Tag,
    Toast,
    ConfirmDialog,
    FileUpload,
    TooltipModule,
    DatePipe,
    FileSizePipe,
  ],
})
export class DocumentList implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly conversationService = inject(ConversationService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly router = inject(Router);

  private readonly fileUpload = viewChild<FileUpload>('fileUpload');

  readonly documents = signal<Document[]>([]);
  readonly loading = signal(false);
  readonly uploading = signal(false);
  readonly startingChatDocId = signal<string | null>(null);

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading.set(true);
    this.documentService.getAll().subscribe({
      next: (docs) => {
        this.documents.set(docs);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load documents.',
        });
      },
    });
  }

  onFileSelect(event: { files: File[] }): void {
    const file = event.files[0];
    if (!file) return;
    this.uploadFile(file);
  }

  private uploadFile(file: File): void {
    this.uploading.set(true);
    this.documentService.upload(file).subscribe({
      next: (doc) => {
        this.documents.update((docs) => [doc, ...docs]);
        this.uploading.set(false);
        this.fileUpload()?.clear();
        this.messageService.add({
          severity: 'success',
          summary: 'Uploaded',
          detail: `${doc.filename} is being processed.`,
        });
      },
      error: () => {
        this.uploading.set(false);
        this.fileUpload()?.clear();
        this.messageService.add({
          severity: 'error',
          summary: 'Upload failed',
          detail: 'The file could not be uploaded. Please try again.',
        });
      },
    });
  }

  startChat(doc: Document): void {
    if (doc.status !== 'READY') {
      const detail =
        doc.status === 'PROCESSING'
          ? 'Document is still processing. Try again once it is ready.'
          : 'Document failed to process and cannot be used for chat.';
      this.messageService.add({ severity: 'warn', summary: 'Not ready', detail });
      return;
    }

    if (this.startingChatDocId()) return;
    this.startingChatDocId.set(doc.id);

    this.conversationService.create(doc.id).subscribe({
      next: (conv) => {
        this.startingChatDocId.set(null);
        this.router.navigate(['/conversations', conv.id]);
      },
      error: () => {
        this.startingChatDocId.set(null);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not start a conversation. Please try again.',
        });
      },
    });
  }

  onDeleteClick(event: Event, doc: Document): void {
    event.stopPropagation();
    this.confirmDelete(doc);
  }

  confirmDelete(doc: Document): void {
    this.confirmationService.confirm({
      message: `Delete "${doc.filename}"? This cannot be undone.`,
      header: 'Confirm deletion',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Delete',
      rejectLabel: 'Cancel',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteDocument(doc),
    });
  }

  private deleteDocument(doc: Document): void {
    this.documentService.delete(doc.id).subscribe({
      next: () => {
        this.documents.update((docs) => docs.filter((d) => d.id !== doc.id));
        this.messageService.add({
          severity: 'success',
          summary: 'Deleted',
          detail: `${doc.filename} has been removed.`,
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Delete failed',
          detail: 'Could not delete the document. Please try again.',
        });
      },
    });
  }

  statusSeverity(status: string): TagSeverity {
    if (status === 'READY') return 'success';
    if (status === 'FAILED') return 'danger';
    return 'warn';
  }

  statusLabel(status: string): string {
    if (status === 'READY') return 'Ready';
    if (status === 'FAILED') return 'Failed';
    return 'Processing';
  }
}

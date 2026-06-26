import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnInit,
  computed,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ConversationService } from '../../core/services/conversation.service';
import type { Conversation, Message, SourceReference } from '../../core/models/conversation.model';

interface DisplayMessage extends Message {
  parsedSources: SourceReference[];
}

@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Toast],
})
export class Chat implements OnInit {
  readonly id = input.required<string>();

  private readonly conversationService = inject(ConversationService);
  private readonly messageService = inject(MessageService);

  private readonly messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');
  private readonly textarea = viewChild<ElementRef<HTMLTextAreaElement>>('textarea');

  readonly conversation = signal<Conversation | null>(null);
  readonly messages = signal<DisplayMessage[]>([]);
  readonly loadingConversation = signal(true);
  readonly sending = signal(false);
  readonly question = signal('');
  readonly loadError = signal(false);
  readonly expandedSourceIds = signal(new Set<string>());
  readonly expandedChunkKeys = signal(new Set<string>());

  readonly hasValidQuestion = computed(() => this.question().trim().length > 0);

  ngOnInit(): void {
    this.conversationService.get(this.id()).subscribe({
      next: (detail) => {
        this.conversation.set(detail.conversation);
        this.messages.set(detail.messages.map((m) => this.toDisplay(m)));
        this.loadingConversation.set(false);
        this.scrollToBottom();
      },
      error: () => {
        this.loadingConversation.set(false);
        this.loadError.set(true);
      },
    });
  }

  send(): void {
    const text = this.question().trim();
    if (!text || this.sending()) return;

    const tempId = `pending-${Date.now()}`;
    const optimisticMsg: DisplayMessage = {
      id: tempId,
      conversation: { id: this.id() },
      role: 'USER',
      content: text,
      sources: null,
      parsedSources: [],
      createdAt: new Date().toISOString(),
    };

    this.messages.update((msgs) => [...msgs, optimisticMsg]);
    this.question.set('');
    this.resetTextareaHeight();
    this.sending.set(true);
    this.scrollToBottom();

    this.conversationService.sendMessage(this.id(), text).subscribe({
      next: (assistantMsg) => {
        this.messages.update((msgs) => [...msgs, this.toDisplay(assistantMsg)]);
        this.sending.set(false);
        this.scrollToBottom();
        this.focusTextarea();
      },
      error: () => {
        this.messages.update((msgs) => msgs.filter((m) => m.id !== tempId));
        this.question.set(text);
        this.sending.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to send',
          detail: 'Could not get a response. Please try again.',
        });
      },
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  onInput(event: Event): void {
    const ta = event.target as HTMLTextAreaElement;
    this.question.set(ta.value);
    ta.style.height = 'auto';
    ta.style.height = `${Math.min(ta.scrollHeight, 160)}px`;
  }

  toggleSources(messageId: string): void {
    this.expandedSourceIds.update((prev) => {
      const next = new Set(prev);
      next.has(messageId) ? next.delete(messageId) : next.add(messageId);
      return next;
    });
  }

  toggleChunk(key: string): void {
    this.expandedChunkKeys.update((prev) => {
      const next = new Set(prev);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });
  }

  isSourcesExpanded(messageId: string): boolean {
    return this.expandedSourceIds().has(messageId);
  }

  isChunkExpanded(key: string): boolean {
    return this.expandedChunkKeys().has(key);
  }

  private toDisplay(msg: Message): DisplayMessage {
    return { ...msg, parsedSources: this.parseSources(msg.sources) };
  }

  private parseSources(raw: string | null): SourceReference[] {
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      this.messagesEnd()?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    }, 0);
  }

  private focusTextarea(): void {
    setTimeout(() => this.textarea()?.nativeElement.focus(), 50);
  }

  private resetTextareaHeight(): void {
    const ta = this.textarea()?.nativeElement;
    if (ta) ta.style.height = 'auto';
  }
}

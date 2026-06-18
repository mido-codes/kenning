# kenning

Chat with your documents using RAG (Retrieval-Augmented Generation). Upload a PDF, Word file, or plain text and ask questions — answers are grounded in your document, not just the model's training data.

## Stack

- **Backend:** Java 21, Spring Boot, Spring AI
- **LLM & Embeddings:** Ollama (`llama3.2:3b`, `nomic-embed-text`) — runs locally
- **Vector store:** PostgreSQL + pgvector
- **Frontend:** Angular

## Local setup

**Prerequisites:** Docker, Java 21, Node.js

```bash
# Start the database and Ollama
docker-compose up -d

# Pull the required models
docker exec ollama ollama pull nomic-embed-text
docker exec ollama ollama pull llama3.2:3b

# Run the backend
cd backend && ./mvnw spring-boot:run

# Run the frontend
cd frontend && npm install && npm start
```

App runs at `http://localhost:4200`, API at `http://localhost:8080`.

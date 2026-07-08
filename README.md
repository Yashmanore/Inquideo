# 🎬 Inquideo — YouTube RAG Chatbot Web App

> Ask questions about any YouTube video using AI. Powered by **Spring Boot**, **React**, **Gemini embeddings**, **Pinecone vector search**, and **Gemini 2.5 Flash** for intelligent, context-grounded answers with timestamp citations.

---

## 📌 What is Inquideo?

**Inquideo** is a **Retrieval-Augmented Generation (RAG)** web application that lets you have a multi-turn conversation with any YouTube video. 

Instead of summarizing the entire video at once or exceeding context window token limits, it:
1. **Scrapes/Fetches** the video's transcript.
2. **Splits** it into semantically overlapping 30-second chunks.
3. **Indexes** those chunks as 768-dimensional vector embeddings into **Pinecone**.
4. **Retrieves** only the most relevant chunks based on the user's queries.
5. **Answers** questions using **Gemini 2.5 Flash**, fully backed by **exact timestamp citations** mapping back to the video source.

---

## 🧠 How the RAG Pipeline Works

This system implements a full end-to-end RAG architecture. Here's every step explained:

```
YouTube URL
    │
    ▼
┌─────────────────────────────┐
│  1. Transcript Fetching     │  youtube-transcript
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  2. Chunking with Overlap   │  30s sliding window / 5s overlap
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  3. Embedding Generation    │  gemini-embedding-001 (768-dim)
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  4. Vector Storage          │  Pinecone (Session Namespace)
└─────────────────────────────┘
    │        ▲
    │        │ (on each user question)
    ▼        │
┌─────────────────────────────┐
│  5. Query Embedding         │  gemini-embedding-001 (RETRIEVAL_QUERY)
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  6. Semantic Retrieval      │  Pinecone topK=5 similarity search
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  7. Context-Augmented LLM   │  Gemini 2.5 Flash + system prompt
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  8. Multi-turn Chat Loop    │  Session-based memory
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  9. Cleanup                 │  Pinecone session namespace cleared
└─────────────────────────────┘
```

---

## 🗂️ Project Structure

The project has been refactored from a CLI tool into a modern web application:

```
Inquideo/
├── yt-ai-backend/       # Spring Boot (Java 17+) backend service
│   ├── src/             # Source files (Controllers, Services, Config)
│   ├── pom.xml          # Maven dependencies
│   └── mvnw / mvnw.cmd  # Maven wrapper scripts
├── yt-ai-frontend/      # Vite + React + Tailwind CSS Web UI
│   ├── src/             # UI Components, Pages, and Assets
│   ├── package.json     # Node.js dependencies & scripts
│   └── vite.config.js   # Vite server proxy config
├── docker-compose.yml   # Multi-container configuration
├── .env                 # Environment variables (API keys & Config)
├── index.js             # (Legacy) Node.js CLI pipeline ingestion
├── query.js             # (Legacy) Node.js CLI chat loop
└── README.md            # This documentation
```

---

## 🛠️ Tech Stack

### Backend Service (Java / Spring Boot)
- **Spring Boot 3.3.5**: Core framework, REST API controllers, configuration injection.
- **Spring WebFlux WebClient**: Non-blocking REST client used for high-performance requests to Gemini and Pinecone APIs.
- **Jsoup**: For robust YouTube web-scraping & timedtext XML parsing.
- **Lombok**: For boilerplate reduction.
- **SpringDoc OpenAPI (Swagger UI)**: Auto-generated interactive API documentation.

### Frontend Web UI (React)
- **React 18**: Component-based UI logic.
- **Vite**: Rapid-build development server.
- **Tailwind CSS**: Modern utility styling and responsive layouts.
- **Framer Motion**: Fluid micro-animations and smooth transition effects.
- **Lucide React**: Vector iconography.
- **React Markdown**: Beautiful parsing of Gemini responses.

### AI & Vector Infrastructure
- **gemini-embedding-001**: Google Generative AI embeddings (768 dimensions).
- **gemini-2.5-flash**: For generating grounding answers based on context.
- **Pinecone**: Cloud serverless vector database used to query semantic chunks with metadata (times, text).

---

## ⚙️ Setup and Installation

### 1. Clone & Configuration

First, create a `.env` file in the root `Inquideo` directory containing your API keys and index configuration:

```env
GEMINI_API_KEY=your_gemini_api_key
PINECONE_API_KEY=your_pinecone_api_key
PINECONE_INDEX_NAME=your_index_name
PINECONE_HOST=https://your-index-host.pinecone.io
```

> Create your index at [pinecone.io](https://www.pinecone.io/) with **768 dimensions** and the **cosine** metric.

---

### 2. Running the Application

You can run Inquideo either using Docker Compose or manually on your local system.

#### Option A: Docker Compose (Recommended)
This compiles and runs both backend and frontend containers, automatically loading the `.env` variables:

```bash
docker compose up --build
```
- **Web UI**: Open [http://localhost:3000](http://localhost:3000)
- **Backend API**: Open [http://localhost:8080](http://localhost:8080)
- **Swagger Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

#### Option B: Manual Local Setup
If you don't have Docker installed, you can run the services separately:

**1. Start Backend (Spring Boot)**:
Open a terminal in the `yt-ai-backend/` folder. Load the `.env` variables into your environment, then run the Maven wrapper:

- **PowerShell (Windows)**:
  ```powershell
  Get-Content ..\.env | ForEach-Object { if ($_ -match '^(.*?)=(.*)$') { [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process') } }; .\mvnw spring-boot:run
  ```
- **Bash (macOS/Linux)**:
  ```bash
  export $(cat ../.env | xargs) && ./mvnw spring-boot:run
  ```

**2. Start Frontend (React/Vite)**:
Open another terminal in the `yt-ai-frontend/` folder:
```bash
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser. Vite automatically proxies `/api` calls to the Spring Boot backend running on port 8080.

---

## 📊 Key Design Decisions

| Decision | Why |
|---|---|
| **Session Isolation** | Multi-user support is achieved by creating unique `sessionId` values per processed video, uploading vector embeddings into Pinecone using the session ID as a namespace, and wiping the namespace on session end. |
| **Asymmetric Task Types** | `RETRIEVAL_DOCUMENT` is used when embedding chunk documents, and `RETRIEVAL_QUERY` is used when embedding the user's question, matching Google's guidelines for optimal retrieval accuracy. |
| **Sliding Window Chunking** | A 30s window with 5s overlap preserves full context around sentences, reducing fragmented responses. |
| **Embedded Swagger Docs** | Interactive backend endpoint testing via SpringDoc OpenAPI. |

---

## ✅ Feature Status

| Feature | Status | Notes |
|---|---|---|
| **Web UI** | ✅ Implemented | Sleek, modern chat console built with React + Vite + Tailwind CSS |
| **REST API Backend** | ✅ Implemented | Spring Boot 3.x web app managing transcripts, embeddings, and chat memory |
| **Videos with Auto-Captions** | ✅ Supported | Automatically fetches and parses auto-generated caption tracks |
| **Sliding Window Chunking** | ✅ Implemented | 30s window, 5s overlap matching Node.js RAG pipeline rules |
| **Timestamp Citations** | ✅ Implemented | Answers dynamically reference citations in `[M:SS to M:SS]` format |
| **Per-session Memory** | ✅ Implemented | In-memory session chat history is maintained by the backend |
| **Session Cleanup** | ✅ Implemented | Deletes vector indexes under specific namespaces upon request |
| **Docker Orchestration** | ✅ Implemented | One-command run capability via Docker Compose |

---

## 🚀 Upcoming / Future Roadmap
- [ ] **Persistent Memory**: Migrate from in-memory session lists to PostgreSQL or Redis.
- [ ] **Whisper Speech-to-Text Fallback**: Process videos that do not contain auto-generated captions by downloading audio and running it through a transcriber.
- [ ] **Multi-video support**: Let users chat across a playlist or custom video library.
- [ ] **Streaming Answers**: Stream Gemini responses character-by-character to the UI using server-sent events (SSE).

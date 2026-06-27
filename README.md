# 🎬 YT-AI — YouTube RAG Chatbot

> Ask questions about any YouTube video using AI. Powered by **Gemini embeddings**, **Pinecone vector search**, and **Gemini 2.5 Flash** for intelligent, context-grounded answers.

---

## 📌 What is this?

**YT-AI** is a **Retrieval-Augmented Generation (RAG)** system that lets you have a multi-turn conversation with any YouTube video. Instead of summarizing the entire video at once, it intelligently retrieves only the **most relevant chunks** of the transcript to answer each specific question — making responses accurate, grounded, and token-efficient.

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
│  2. Chunking with Overlap   │  Custom sliding window
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  3. Embedding Generation    │  gemini-embedding-001
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  4. Vector Storage          │  Pinecone (768-dim index)
└─────────────────────────────┘
    │        ▲
    │        │ (on each user question)
    ▼        │
┌─────────────────────────────┐
│  5. Query Embedding         │  gemini-embedding-001
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
│  8. Multi-turn Chat Loop    │  Conversation history maintained
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  9. Cleanup                 │  Pinecone namespace cleared on exit
└─────────────────────────────┘
```

---

### Step 1 — Transcript Fetching

```js
const rawTranscript = await YoutubeTranscript.fetchTranscript(videoId);
```

Uses the `youtube-transcript` library to fetch the full auto-generated transcript of a YouTube video. Each transcript item contains:
- `text` — the spoken words
- `offset` — start time in milliseconds
- `duration` — how long that caption lasts

---

### Step 2 — Chunking with Sliding Window + Overlap

```
|--- 30s window ---|
            |--- 30s window ---|   ← 5s overlap
                        |--- 30s window ---|
```

Raw transcripts are split into **overlapping time-based chunks**:
- **Window size**: 30 seconds of spoken content per chunk
- **Overlap**: 5 seconds shared between consecutive chunks
- **Step size**: 25 seconds (moves forward by this much each time)

Why overlap? It prevents a sentence from being split across two chunks and losing its meaning. Each chunk is wrapped as a `LangChain Document` with `startTime` and `endTime` metadata.

```js
const CHUNK_WINDOW_MS = 30000;  // 30s
const OVERLAP_MS      = 5000;   // 5s
const STEP_MS         = 25000;  // move forward 25s each iteration
```

---

### Step 3 — Embedding Generation

```js
const embeddingsModel = new CustomGoogleGenerativeAIEmbeddings({
    model: "gemini-embedding-001",
    taskType: "RETRIEVAL_DOCUMENT",
    outputDimensionality: 768,
});
```

Each chunk's text is converted into a **768-dimensional vector** using Google's `gemini-embedding-001` model. The `taskType: "RETRIEVAL_DOCUMENT"` tells the model to optimize embeddings for being retrieved (as opposed to being used as queries). These dense vectors numerically encode the semantic meaning of each chunk.

---

### Step 4 — Vector Storage in Pinecone

```js
await PineconeStore.fromDocuments(validChunks, embeddingsModel, {
    pineconeIndex: pineconeIndex,
    maxConcurrency: 2,
});
```

All chunk vectors are upserted into a **Pinecone serverless index** (768 dimensions) using LangChain's `PineconeStore`. The index stores:
- The vector (768 float values)
- Metadata: the original text, start/end timestamps

---

### Step 5 — Query Embedding (at question time)

```js
const queryEmbed = await ai.models.embedContent({
    model: "gemini-embedding-001",
    contents: userQuestion,
    config: { taskType: "RETRIEVAL_QUERY", outputDimensionality: 768 }
});
```

When the user asks a question, it's embedded using `taskType: "RETRIEVAL_QUERY"` — a different optimization than the document side. This asymmetric embedding approach improves retrieval accuracy.

---

### Step 6 — Semantic Retrieval from Pinecone

```js
const results = await pineconeIndex.query({
    topK: 5,
    vector: queryEmbedding,
    includeMetadata: true,
});
```

Pinecone performs **approximate nearest-neighbor search** to find the 5 transcript chunks whose vectors are most semantically similar to the question vector. These chunks form the **context** for the LLM.

---

### Step 7 — Context-Augmented Generation (RAG)

```js
const res = await ai.models.generateContent({
    model: "gemini-2.5-flash",
    contents: History,
    config: {
        systemInstruction: `Answer based on this context:\n${context}`
    }
});
```

The retrieved chunks are injected into a **system prompt** along with the full conversation history. Gemini 2.5 Flash generates an answer grounded strictly in the retrieved context — not its general knowledge. If the answer isn't in the context, it says so politely.

---

### Step 8 — Multi-turn Conversation History

```js
History.push({ role: 'user',  parts: [{ text: question }] });
// ... generate response ...
History.push({ role: 'model', parts: [{ text: res.text }]  });
```

Every user question and model answer is appended to a `History` array, enabling **natural follow-up questions** without losing prior context. The entire history is sent to Gemini on each call.

---

### Step 9 — Cleanup on Exit

```js
await index.namespace('').deleteAll();
```

When the user types `exit`, all vectors in the default Pinecone namespace are deleted to keep the index clean between sessions.

---

## 🗂️ Project Structure

```
YT-AI/
├── index.js       # Transcript fetching, chunking, embedding & Pinecone upload
├── query.js       # Main entry point: chat loop + Gemini Q&A
├── clr.js         # Standalone Pinecone cleanup utility
├── autotest.js    # Automated pipeline test (no TTY needed)
├── package.json
└── .env           # API keys (never commit this!)
```

---

## ⚙️ Setup

### 1. Clone & Install

```bash
git clone <your-repo-url>
cd YT-AI
npm install
```

### 2. Configure `.env`

```env
GEMINI_API_KEY=your_gemini_api_key
PINECONE_API_KEY=your_pinecone_api_key
PINECONE_INDEX_NAME=your_index_name   # must be 768-dim
```

> Create a free Pinecone index at [pinecone.io](https://www.pinecone.io/) with **768 dimensions** and **cosine** metric.

### 3. Run

```bash
node query.js
```

You'll be prompted to enter a YouTube URL, then you can ask unlimited questions. Type `exit` to quit and auto-clean Pinecone.

---

## 🛠️ Tech Stack

| Technology | Role |
|---|---|
| `youtube-transcript` | Fetch raw video captions |
| `gemini-embedding-001` | Generate 768-dim semantic vectors |
| `@pinecone-database/pinecone` | Store & retrieve vectors |
| `@langchain/pinecone` | High-level Pinecone upsert helper |
| `@langchain/google-genai` | LangChain embedding wrapper |
| `gemini-2.5-flash` | Generate final answers |
| `readline-sync` | Interactive terminal Q&A loop |
| `dotenv` | Manage API keys |

---

## 📊 Key Design Decisions

| Decision | Why |
|---|---|
| **30s chunks with 5s overlap** | Balances context richness vs. noise; overlap prevents sentence fragmentation |
| **768 dimensions** | Matches `gemini-embedding-001`'s native output size |
| **Asymmetric task types** | `RETRIEVAL_DOCUMENT` for storage, `RETRIEVAL_QUERY` for search — improves retrieval quality |
| **topK=5** | Retrieves enough context without overwhelming the LLM prompt |
| **Conversation history** | Enables natural follow-ups without re-asking context |
| **Namespace-scoped delete** | Pinecone SDK v5 requires explicit namespace targeting |

---

## 📋 Project Workflow Rating

> Rated after a live end-to-end test run against a real YouTube video using `autotest.js`.

**Overall Score: ⭐ 8.5 / 10**

| Area | Rating | Notes |
|---|---|---|
| Transcript ingestion | ✅ 9/10 | Clean, works perfectly |
| Chunking strategy | ✅ 9/10 | Sliding window + overlap is smart |
| Embedding pipeline | ✅ 9/10 | Correct model + dimensions |
| Vector retrieval | ✅ 8/10 | `topK: 5` is reasonable |
| Answer quality | ✅ 9/10 | Accurate, context-grounded |
| Cleanup | ✅ 9/10 | Fixed and working now |
| Code modularity | 🟡 7/10 | Could split into cleaner services |
| Error handling | 🟡 7/10 | Could add retry logic for 429s |

---

## 🚀 Upcoming

- [ ] **Web UI** — React/Next.js frontend for a visual chat experience
- [ ] **Timestamp citations** — Show which part of the video each answer came from
- [ ] **Multi-video support** — Query across multiple videos simultaneously
- [ ] **Export chat** — Save Q&A sessions to PDF or markdown

---

## 👨‍💻 Author

Built by **Yash Manore** — 5th semester CS student with a strong logical base.  
Built the entire RAG pipeline from scratch in ~5 hours. 🔥

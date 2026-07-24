import axios from 'axios'

// Automatically switches between your Render production URL and localhost for dev
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const BASE_URL = `${API_BASE_URL}/api/v1`

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 120_000,  // 2 minutes — video processing can be slow
})

// ── Request interceptor ───────────────────────────────────────────────────────
api.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
)

// ── Response interceptor — normalize errors ───────────────────────────────────
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message
      || error.response?.data?.error
      || error.message
      || 'An unexpected error occurred'
    return Promise.reject(new Error(message))
  }
)

// ── API functions (derived from existing JS workflow) ─────────────────────────

/**
 * POST /api/v1/video/process
 * Triggers the full ingestion pipeline for a YouTube video.
 *
 * @param {string} videoUrl  YouTube URL or bare video ID
 * @returns {Promise<{ sessionId, videoId, chunksProcessed, message }>}
 */
export const processVideo = (videoUrl) =>
  api.post('/video/process', { videoUrl })

/**
 * POST /api/v1/chat
 * Sends a user question and gets back an answer with source citations.
 *
 * @param {string} sessionId  from processVideo response
 * @param {string} question   user's question
 * @returns {Promise<{ sessionId, answer, sources }>}
 */
export const sendMessage = (sessionId, question) =>
  api.post('/chat', { sessionId, question })

/**
 * GET /api/v1/chat/history/{sessionId}
 * Retrieves the full conversation history for a session.
 *
 * @param {string} sessionId
 * @returns {Promise<{ sessionId, messages }>}
 */
export const getChatHistory = (sessionId) =>
  api.get(`/chat/history/${sessionId}`)

/**
 * DELETE /api/v1/cleanup/{sessionId}
 * Deletes all Pinecone vectors and clears conversation history.
 *
 * @param {string} sessionId
 * @returns {Promise<{ sessionId, message }>}
 */
export const cleanupSession = (sessionId) =>
  api.delete(`/cleanup/${sessionId}`)

export default API_BASE_URL

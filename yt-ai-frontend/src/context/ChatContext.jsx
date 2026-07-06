import React, { createContext, useContext, useReducer, useCallback } from 'react'

// ── State Shape ──────────────────────────────────────────────────────────────
const initialState = {
  // Video / session
  sessionId: null,
  videoId: null,
  videoUrl: '',
  chunksProcessed: 0,

  // Processing
  isProcessing: false,
  processingStatus: 'idle', // 'idle' | 'fetching' | 'chunking' | 'embedding' | 'complete' | 'error'
  processingError: null,

  // Chat
  messages: [],       // [{ id, role: 'user'|'model', text, sources, timestamp }]
  isTyping: false,    // true while waiting for Gemini response
  chatError: null,

  // Session
  isCleaning: false,
  isSessionActive: false,
}

// ── Actions ──────────────────────────────────────────────────────────────────
const ActionTypes = {
  SET_VIDEO_URL: 'SET_VIDEO_URL',
  SET_PROCESSING: 'SET_PROCESSING',
  SET_PROCESSING_STATUS: 'SET_PROCESSING_STATUS',
  VIDEO_PROCESSED: 'VIDEO_PROCESSED',
  VIDEO_PROCESS_ERROR: 'VIDEO_PROCESS_ERROR',
  ADD_USER_MESSAGE: 'ADD_USER_MESSAGE',
  ADD_MODEL_MESSAGE: 'ADD_MODEL_MESSAGE',
  SET_TYPING: 'SET_TYPING',
  SET_CHAT_ERROR: 'SET_CHAT_ERROR',
  SET_CLEANING: 'SET_CLEANING',
  CLEANUP_COMPLETE: 'CLEANUP_COMPLETE',
  RESET: 'RESET',
}

// ── Reducer ───────────────────────────────────────────────────────────────────
function chatReducer(state, action) {
  switch (action.type) {
    case ActionTypes.SET_VIDEO_URL:
      return { ...state, videoUrl: action.payload }

    case ActionTypes.SET_PROCESSING:
      return {
        ...state,
        isProcessing: action.payload,
        processingError: action.payload ? null : state.processingError,
      }

    case ActionTypes.SET_PROCESSING_STATUS:
      return { ...state, processingStatus: action.payload }

    case ActionTypes.VIDEO_PROCESSED:
      return {
        ...state,
        isProcessing: false,
        processingStatus: 'complete',
        sessionId: action.payload.sessionId,
        videoId: action.payload.videoId,
        chunksProcessed: action.payload.chunksProcessed,
        isSessionActive: true,
        messages: [],
        processingError: null,
      }

    case ActionTypes.VIDEO_PROCESS_ERROR:
      return {
        ...state,
        isProcessing: false,
        processingStatus: 'error',
        processingError: action.payload,
      }

    case ActionTypes.ADD_USER_MESSAGE:
      return {
        ...state,
        messages: [
          ...state.messages,
          {
            id: Date.now(),
            role: 'user',
            text: action.payload,
            timestamp: new Date().toISOString(),
          },
        ],
        chatError: null,
      }

    case ActionTypes.ADD_MODEL_MESSAGE:
      return {
        ...state,
        messages: [
          ...state.messages,
          {
            id: Date.now(),
            role: 'model',
            text: action.payload.answer,
            sources: action.payload.sources || [],
            timestamp: new Date().toISOString(),
          },
        ],
        isTyping: false,
      }

    case ActionTypes.SET_TYPING:
      return { ...state, isTyping: action.payload }

    case ActionTypes.SET_CHAT_ERROR:
      return { ...state, chatError: action.payload, isTyping: false }

    case ActionTypes.SET_CLEANING:
      return { ...state, isCleaning: action.payload }

    case ActionTypes.CLEANUP_COMPLETE:
      return { ...initialState }

    case ActionTypes.RESET:
      return { ...initialState }

    default:
      return state
  }
}

// ── Context ───────────────────────────────────────────────────────────────────
const ChatContext = createContext(null)

export function ChatProvider({ children }) {
  const [state, dispatch] = useReducer(chatReducer, initialState)

  // ── Action creators ───────────────────────────────────────────────────────
  const setVideoUrl = useCallback((url) => {
    dispatch({ type: ActionTypes.SET_VIDEO_URL, payload: url })
  }, [])

  const setProcessing = useCallback((isProcessing) => {
    dispatch({ type: ActionTypes.SET_PROCESSING, payload: isProcessing })
  }, [])

  const setProcessingStatus = useCallback((status) => {
    dispatch({ type: ActionTypes.SET_PROCESSING_STATUS, payload: status })
  }, [])

  const onVideoProcessed = useCallback((data) => {
    dispatch({ type: ActionTypes.VIDEO_PROCESSED, payload: data })
  }, [])

  const onVideoProcessError = useCallback((error) => {
    dispatch({ type: ActionTypes.VIDEO_PROCESS_ERROR, payload: error })
  }, [])

  const addUserMessage = useCallback((text) => {
    dispatch({ type: ActionTypes.ADD_USER_MESSAGE, payload: text })
  }, [])

  const addModelMessage = useCallback((data) => {
    dispatch({ type: ActionTypes.ADD_MODEL_MESSAGE, payload: data })
  }, [])

  const setTyping = useCallback((isTyping) => {
    dispatch({ type: ActionTypes.SET_TYPING, payload: isTyping })
  }, [])

  const setChatError = useCallback((error) => {
    dispatch({ type: ActionTypes.SET_CHAT_ERROR, payload: error })
  }, [])

  const setCleaning = useCallback((isCleaning) => {
    dispatch({ type: ActionTypes.SET_CLEANING, payload: isCleaning })
  }, [])

  const onCleanupComplete = useCallback(() => {
    dispatch({ type: ActionTypes.CLEANUP_COMPLETE })
  }, [])

  const reset = useCallback(() => {
    dispatch({ type: ActionTypes.RESET })
  }, [])

  const value = {
    // State
    ...state,
    // Actions
    setVideoUrl,
    setProcessing,
    setProcessingStatus,
    onVideoProcessed,
    onVideoProcessError,
    addUserMessage,
    addModelMessage,
    setTyping,
    setChatError,
    setCleaning,
    onCleanupComplete,
    reset,
  }

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>
}

// ── Hook ──────────────────────────────────────────────────────────────────────
export function useChat() {
  const context = useContext(ChatContext)
  if (!context) {
    throw new Error('useChat must be used within a ChatProvider')
  }
  return context
}

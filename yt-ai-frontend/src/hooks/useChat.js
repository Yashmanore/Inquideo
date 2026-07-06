import { useCallback, useRef } from 'react'
import { useChat } from '../context/ChatContext.jsx'
import { sendMessage, cleanupSession } from '../services/api.js'

/**
 * Hook for chat interactions.
 * Manages sending messages and cleanup.
 */
export function useChatActions() {
  const {
    sessionId,
    isTyping,
    chatError,
    isCleaning,
    isSessionActive,
    addUserMessage,
    addModelMessage,
    setTyping,
    setChatError,
    setCleaning,
    onCleanupComplete,
  } = useChat()

  const send = useCallback(async (question) => {
    if (!question?.trim() || !sessionId || isTyping) return

    addUserMessage(question)
    setTyping(true)
    setChatError(null)

    try {
      const data = await sendMessage(sessionId, question)
      addModelMessage(data)
    } catch (error) {
      setChatError(error.message || 'Failed to get response')
      setTyping(false)
    }
  }, [sessionId, isTyping, addUserMessage, addModelMessage, setTyping, setChatError])

  const cleanup = useCallback(async () => {
    if (!sessionId || isCleaning) return

    setCleaning(true)
    try {
      await cleanupSession(sessionId)
      onCleanupComplete()
    } catch (error) {
      setCleaning(false)
      setChatError(error.message || 'Cleanup failed')
    }
  }, [sessionId, isCleaning, setCleaning, onCleanupComplete, setChatError])

  return {
    sessionId,
    isTyping,
    chatError,
    isCleaning,
    isSessionActive,
    send,
    cleanup,
  }
}

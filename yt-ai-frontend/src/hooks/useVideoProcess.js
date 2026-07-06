import { useCallback } from 'react'
import { useChat } from '../context/ChatContext.jsx'
import { processVideo } from '../services/api.js'

/**
 * Hook for handling video processing.
 * Manages the full flow: validate → submit → update context state.
 */
export function useVideoProcess() {
  const {
    videoUrl,
    isProcessing,
    processingStatus,
    processingError,
    setVideoUrl,
    setProcessing,
    setProcessingStatus,
    onVideoProcessed,
    onVideoProcessError,
  } = useChat()

  const process = useCallback(async (url) => {
    const targetUrl = url || videoUrl
    if (!targetUrl?.trim()) return

    setProcessing(true)
    setProcessingStatus('fetching')

    try {
      // Simulate progress stages for UX feedback
      setProcessingStatus('fetching')
      const data = await processVideo(targetUrl)
      onVideoProcessed(data)
    } catch (error) {
      onVideoProcessError(error.message || 'Failed to process video')
    }
  }, [videoUrl, setProcessing, setProcessingStatus, onVideoProcessed, onVideoProcessError])

  return {
    videoUrl,
    setVideoUrl,
    isProcessing,
    processingStatus,
    processingError,
    process,
  }
}

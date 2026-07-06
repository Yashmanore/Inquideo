import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Youtube, ArrowRight, Loader2, AlertCircle, CheckCircle } from 'lucide-react'
import { useVideoProcess } from '../../hooks/useVideoProcess.js'

export default function VideoInputCard() {
  const {
    videoUrl,
    setVideoUrl,
    isProcessing,
    processingStatus,
    processingError,
    process,
  } = useVideoProcess()

  const [inputUrl, setInputUrl] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (inputUrl.trim()) {
      setVideoUrl(inputUrl)
      process(inputUrl)
    }
  }

  const statusMessages = {
    fetching: 'Fetching transcript from YouTube...',
    chunking: 'Chunking transcript into 30s windows...',
    embedding: 'Generating Gemini embeddings...',
    complete: 'Video processed!',
    error: processingError,
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="glass p-6"
    >
      <div className="flex items-center gap-3 mb-5">
        <div className="w-10 h-10 rounded-xl bg-primary-900/50 flex items-center justify-center border border-primary-700/40">
          <Youtube size={20} className="text-primary-400" />
        </div>
        <div>
          <h2 className="font-semibold text-white text-sm">Process a Video</h2>
          <p className="text-slate-500 text-xs">Paste any YouTube URL to begin</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-3">
        <div className="relative">
          <input
            id="video-url-input"
            type="text"
            value={inputUrl}
            onChange={(e) => setInputUrl(e.target.value)}
            placeholder="https://youtube.com/watch?v=..."
            className="input-field pr-12 text-sm"
            disabled={isProcessing}
          />
          <Youtube
            size={16}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500"
          />
        </div>

        <button
          id="process-video-btn"
          type="submit"
          disabled={isProcessing || !inputUrl.trim()}
          className="btn-primary w-full text-sm py-2.5"
        >
          {isProcessing ? (
            <>
              <Loader2 size={16} className="animate-spin" />
              Processing...
            </>
          ) : (
            <>
              Process Video
              <ArrowRight size={16} />
            </>
          )}
        </button>
      </form>

      {/* Status */}
      <AnimatePresence>
        {isProcessing && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="mt-4 overflow-hidden"
          >
            <ProcessingStatus status={processingStatus} message={statusMessages[processingStatus]} />
          </motion.div>
        )}
        {processingError && !isProcessing && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-3 flex items-start gap-2 p-3 rounded-lg bg-red-950/30 border border-red-800/30"
          >
            <AlertCircle size={14} className="text-red-400 flex-shrink-0 mt-0.5" />
            <p className="text-red-400 text-xs">{processingError}</p>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  )
}

function ProcessingStatus({ status, message }) {
  return (
    <div className="flex items-center gap-2 p-3 rounded-lg bg-primary-900/20 border border-primary-700/20">
      <Loader2 size={14} className="text-primary-400 animate-spin flex-shrink-0" />
      <p className="text-primary-300 text-xs">{message}</p>
    </div>
  )
}

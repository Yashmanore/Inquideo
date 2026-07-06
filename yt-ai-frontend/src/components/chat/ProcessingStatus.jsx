import React from 'react'
import { useChat } from '../../context/ChatContext.jsx'
import { CheckCircle, Loader2, AlertCircle, Layers } from 'lucide-react'

export default function ProcessingStatus() {
  const { processingStatus, processingError, chunksProcessed, videoId, isProcessing } = useChat()

  if (processingStatus === 'idle') return null

  const statusConfig = {
    fetching:  { icon: Loader2, color: 'text-blue-400',   bg: 'bg-blue-950/30 border-blue-800/30',  spin: true,  label: 'Fetching transcript...' },
    chunking:  { icon: Loader2, color: 'text-yellow-400', bg: 'bg-yellow-950/30 border-yellow-800/30', spin: true, label: 'Chunking with sliding window...' },
    embedding: { icon: Loader2, color: 'text-purple-400', bg: 'bg-purple-950/30 border-purple-800/30', spin: true, label: 'Generating Gemini embeddings...' },
    complete:  { icon: CheckCircle, color: 'text-green-400', bg: 'bg-green-950/30 border-green-800/30', spin: false, label: `Ready! ${chunksProcessed} chunks indexed` },
    error:     { icon: AlertCircle, color: 'text-red-400', bg: 'bg-red-950/30 border-red-800/30', spin: false, label: processingError || 'An error occurred' },
  }

  const cfg = statusConfig[processingStatus] || statusConfig.fetching
  const Icon = cfg.icon

  return (
    <div className={`flex items-center gap-2.5 p-3 rounded-xl border text-xs ${cfg.bg}`}>
      <Icon size={14} className={`flex-shrink-0 ${cfg.color} ${cfg.spin ? 'animate-spin' : ''}`} />
      <span className={cfg.color}>{cfg.label}</span>
      {processingStatus === 'complete' && (
        <div className="ml-auto flex items-center gap-1">
          <Layers size={11} className="text-green-500" />
          <span className="text-green-500 font-mono">{videoId}</span>
        </div>
      )}
    </div>
  )
}

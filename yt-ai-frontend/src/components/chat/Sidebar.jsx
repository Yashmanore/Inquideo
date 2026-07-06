import React from 'react'
import { motion } from 'framer-motion'
import VideoInputCard from './VideoInputCard.jsx'
import ProcessingStatus from './ProcessingStatus.jsx'
import ChatHistoryList from './ChatHistoryList.jsx'
import CleanupButton from './CleanupButton.jsx'
import { useChat } from '../../context/ChatContext.jsx'
import { Layers } from 'lucide-react'

export default function Sidebar() {
  const { chunksProcessed, videoId, isSessionActive } = useChat()

  return (
    <motion.aside
      initial={{ x: -20, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      className="w-72 flex-shrink-0 border-r border-slate-800 flex flex-col pt-14 overflow-y-auto"
      style={{ background: 'rgba(15,23,42,0.95)' }}
    >
      <div className="p-4 flex-1 space-y-4">

        {/* Header */}
        <div className="flex items-center gap-2 py-2">
          <Layers size={14} className="text-primary-400" />
          <span className="text-white font-semibold text-sm">RAG Pipeline</span>
        </div>

        {/* Video input */}
        <VideoInputCard />

        {/* Processing status */}
        <ProcessingStatus />

        {/* Session info */}
        {isSessionActive && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="p-3 rounded-xl bg-green-950/20 border border-green-800/20 text-xs space-y-1"
          >
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
              <span className="text-green-400 font-medium">Session Active</span>
            </div>
            <p className="text-slate-500 font-mono truncate">{videoId}</p>
            <p className="text-slate-500">{chunksProcessed} chunks indexed</p>
          </motion.div>
        )}

        {/* Chat history */}
        <ChatHistoryList />

        {/* Cleanup */}
        <CleanupButton />
      </div>
    </motion.aside>
  )
}

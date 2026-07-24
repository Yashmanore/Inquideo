import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import VideoInputCard from './VideoInputCard.jsx'
import ProcessingStatus from './ProcessingStatus.jsx'
import ChatHistoryList from './ChatHistoryList.jsx'
import CleanupButton from './CleanupButton.jsx'
import { useChat } from '../../context/ChatContext.jsx'
import { Layers, X } from 'lucide-react'

export default function Sidebar({ isOpen, onClose }) {
  const { chunksProcessed, videoId, isSessionActive } = useChat()

  return (
    <>
      {/* Mobile Backdrop Overlay */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 z-40 bg-black/60 md:hidden backdrop-blur-sm"
          />
        )}
      </AnimatePresence>

      <motion.aside
        className={`fixed md:static inset-y-0 left-0 z-50 md:z-auto w-72 flex-shrink-0 border-r border-slate-800 flex flex-col pt-14 overflow-y-auto transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
        `}
        style={{ background: 'rgba(15,23,42,0.95)' }}
      >
        {/* Mobile Header with Close Button */}
        <div className="md:hidden flex items-center justify-between px-4 py-3 border-b border-slate-800/80 bg-slate-950/40">
          <div className="flex items-center gap-2">
            <Layers size={14} className="text-primary-400" />
            <span className="text-white font-semibold text-xs">RAG Pipeline</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors focus:outline-none"
            aria-label="Close menu"
          >
            <X size={16} />
          </button>
        </div>

        <div className="p-4 flex-1 space-y-4">
          {/* Desktop Header */}
          <div className="hidden md:flex items-center gap-2 py-2">
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
    </>
  )
}

import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Trash2, AlertTriangle, Loader2 } from 'lucide-react'
import { useChatActions } from '../../hooks/useChat.js'

export default function CleanupButton() {
  const { sessionId, isCleaning, cleanup, isSessionActive } = useChatActions()

  if (!isSessionActive) return null

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.9 }}
        className="mt-4"
      >
        <button
          id="cleanup-session-btn"
          onClick={cleanup}
          disabled={isCleaning}
          className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-sm
                     font-medium text-red-400 border border-red-800/40 bg-red-950/20
                     hover:bg-red-950/40 hover:border-red-700/60
                     transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isCleaning ? (
            <>
              <Loader2 size={14} className="animate-spin" />
              Cleaning up...
            </>
          ) : (
            <>
              <Trash2 size={14} />
              End Session
            </>
          )}
        </button>
        <p className="text-slate-600 text-[10px] text-center mt-1.5">
          Deletes Pinecone vectors + clears history
        </p>
      </motion.div>
    </AnimatePresence>
  )
}

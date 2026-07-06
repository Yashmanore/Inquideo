import React from 'react'
import { motion } from 'framer-motion'
import { Bot } from 'lucide-react'

export default function TypingIndicator() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 8 }}
      className="flex gap-3 justify-start"
    >
      {/* Avatar */}
      <div className="flex-shrink-0 w-8 h-8 rounded-xl bg-gradient-to-br from-primary-600 to-secondary-500
                      flex items-center justify-center shadow-glow-purple">
        <Bot size={14} className="text-white" />
      </div>

      {/* Dots */}
      <div className="glass px-4 py-3 rounded-2xl rounded-tl-sm flex items-center gap-1.5">
        <span className="typing-dot" />
        <span className="typing-dot" />
        <span className="typing-dot" />
        <span className="ml-2 text-slate-500 text-xs">Thinking...</span>
      </div>
    </motion.div>
  )
}

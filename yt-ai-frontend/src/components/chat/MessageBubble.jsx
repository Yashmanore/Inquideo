import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { User, Bot, ChevronDown, ChevronUp } from 'lucide-react'
import SourceCitationCard from './SourceCitationCard.jsx'

export default function MessageBubble({ message }) {
  const isUser = message.role === 'user'
  const [showSources, setShowSources] = useState(false)

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={`flex gap-3 ${isUser ? 'justify-end' : 'justify-start'}`}
    >
      {/* Avatar — model side */}
      {!isUser && (
        <div className="flex-shrink-0 w-8 h-8 rounded-xl bg-gradient-to-br from-primary-600 to-secondary-500
                        flex items-center justify-center mt-1 shadow-glow-purple">
          <Bot size={14} className="text-white" />
        </div>
      )}

      <div className={`max-w-[80%] space-y-3 ${isUser ? 'items-end' : 'items-start'} flex flex-col`}>
        {/* Bubble */}
        <div
          className={`px-4 py-3 rounded-2xl text-sm leading-relaxed ${
            isUser
              ? 'bg-gradient-to-br from-primary-600 to-secondary-600 text-white rounded-tr-sm'
              : 'glass text-slate-200 rounded-tl-sm'
          }`}
        >
          {isUser ? (
            <p>{message.text}</p>
          ) : (
            <div className="prose-chat">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {message.text}
              </ReactMarkdown>
            </div>
          )}
        </div>

        {/* Timestamp */}
        <span className="text-slate-600 text-[10px] px-1">
          {new Date(message.timestamp).toLocaleTimeString([], {
            hour: '2-digit', minute: '2-digit'
          })}
        </span>

        {/* Sources Toggle */}
        {!isUser && message.sources && message.sources.length > 0 && (
          <div className="space-y-2 w-full">
            <button
              onClick={() => setShowSources(!showSources)}
              className="text-primary-400 hover:text-primary-300 text-xs px-1 font-medium 
                         flex items-center gap-1.5 transition-colors focus:outline-none"
            >
              <span>{showSources ? 'Hide Sources' : `Show Sources (${message.sources.length})`}</span>
              {showSources ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
            </button>
            <AnimatePresence>
              {showSources && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="space-y-2 pt-1 overflow-hidden"
                >
                  {message.sources.map((source, i) => (
                    <SourceCitationCard key={i} source={source} />
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        )}
      </div>

      {/* Avatar — user side */}
      {isUser && (
        <div className="flex-shrink-0 w-8 h-8 rounded-xl bg-background-700 border border-slate-700
                        flex items-center justify-center mt-1">
          <User size={14} className="text-slate-400" />
        </div>
      )}
    </motion.div>
  )
}

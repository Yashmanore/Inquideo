import React from 'react'
import { motion } from 'framer-motion'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { User, Bot } from 'lucide-react'
import SourceCitationCard from './SourceCitationCard.jsx'

export default function MessageBubble({ message }) {
  const isUser = message.role === 'user'

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

        {/* Sources */}
        {!isUser && message.sources && message.sources.length > 0 && (
          <div className="space-y-2 w-full">
            <p className="text-slate-500 text-xs px-1 font-medium">Sources</p>
            {message.sources.map((source, i) => (
              <SourceCitationCard key={i} source={source} />
            ))}
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

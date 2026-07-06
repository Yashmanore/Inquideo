import React from 'react'
import { useChat } from '../../context/ChatContext.jsx'
import { MessageSquare, Clock } from 'lucide-react'

export default function ChatHistoryList() {
  const { messages } = useChat()

  const questionMessages = messages.filter(m => m.role === 'user')

  if (questionMessages.length === 0) return null

  return (
    <div className="mt-4 space-y-1">
      <p className="text-slate-500 text-xs font-medium uppercase tracking-wide mb-2">
        This Session
      </p>
      {questionMessages.map((msg, i) => (
        <div
          key={msg.id}
          className="flex items-start gap-2 p-2.5 rounded-lg hover:bg-background-800/60
                     transition-colors cursor-default group"
        >
          <MessageSquare size={12} className="text-slate-600 flex-shrink-0 mt-0.5 group-hover:text-primary-400 transition-colors" />
          <div className="min-w-0 flex-1">
            <p className="text-slate-400 text-xs truncate group-hover:text-slate-300 transition-colors">
              {msg.text}
            </p>
            <div className="flex items-center gap-1 mt-0.5">
              <Clock size={9} className="text-slate-600" />
              <span className="text-slate-600 text-[10px]">
                {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

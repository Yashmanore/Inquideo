import React, { useRef, useEffect } from 'react'
import { useChat } from '../../context/ChatContext.jsx'
import MessageBubble from './MessageBubble.jsx'
import TypingIndicator from './TypingIndicator.jsx'
import { Bot, Sparkles } from 'lucide-react'

export default function ChatWindow() {
  const { messages, isTyping, isSessionActive } = useChat()
  const bottomRef = useRef(null)

  // Auto-scroll to latest message
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isTyping])

  if (!isSessionActive) {
    return (
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-primary-900/30 border border-primary-700/30
                          flex items-center justify-center mx-auto">
            <Bot size={28} className="text-primary-400" />
          </div>
          <h3 className="text-white font-semibold">Process a video to start chatting</h3>
          <p className="text-slate-500 text-sm max-w-xs">
            Enter a YouTube URL in the sidebar and click "Process Video" to begin.
          </p>
        </div>
      </div>
    )
  }

  if (messages.length === 0) {
    return (
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-primary-900/30 border border-primary-700/30
                          flex items-center justify-center mx-auto animate-pulse-glow">
            <Sparkles size={28} className="text-primary-400" />
          </div>
          <h3 className="text-white font-semibold">Video processed! Ask a question</h3>
          <p className="text-slate-500 text-sm max-w-xs">
            Ask anything about this video — the AI will cite timestamps in its answers.
          </p>
          <div className="flex flex-wrap justify-center gap-2 mt-2">
            {['What is the main topic?', 'Summarize the key points', 'What happens at the start?'].map(q => (
              <span key={q} className="badge-purple text-xs cursor-default">{q}</span>
            ))}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth">
      {messages.map((message) => (
        <MessageBubble key={message.id} message={message} />
      ))}
      {isTyping && <TypingIndicator />}
      <div ref={bottomRef} />
    </div>
  )
}

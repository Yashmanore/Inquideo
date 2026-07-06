import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Send, AlertCircle } from 'lucide-react'
import Navbar from '../components/chat/Navbar.jsx'
import Sidebar from '../components/chat/Sidebar.jsx'
import ChatWindow from '../components/chat/ChatWindow.jsx'
import { useChatActions } from '../hooks/useChat.js'
import { useChat } from '../context/ChatContext.jsx'

export default function ChatPage() {
  const [input, setInput] = useState('')
  const { send, isTyping, chatError, isSessionActive } = useChatActions()
  const { messages } = useChat()

  const handleSend = async () => {
    if (!input.trim() || isTyping) return
    const question = input.trim()
    setInput('')
    await send(question)
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      <Navbar />

      {/* Sidebar */}
      <Sidebar />

      {/* Main chat area */}
      <div className="flex-1 flex flex-col pt-14 min-w-0 min-h-0">

        {/* Chat window */}
        <div className="flex-1 flex flex-col overflow-hidden min-h-0">
          <ChatWindow />
        </div>

        {/* Error banner */}
        <AnimatePresence>
          {chatError && (
            <motion.div
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 8 }}
              className="mx-4 mb-2 flex items-center gap-2 p-3 rounded-xl
                         bg-red-950/30 border border-red-800/30 text-xs text-red-400"
            >
              <AlertCircle size={14} />
              {chatError}
            </motion.div>
          )}
        </AnimatePresence>

        {/* Input area */}
        <div className="border-t border-slate-800 p-4">
          <div className="max-w-4xl mx-auto">
            <div className="glass flex items-end gap-3 p-3">
              <textarea
                id="chat-input"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={
                  isSessionActive
                    ? "Ask a question about the video... (Enter to send)"
                    : "Process a video first to start chatting"
                }
                disabled={!isSessionActive || isTyping}
                rows={1}
                className="flex-1 bg-transparent resize-none text-white placeholder-slate-500
                           text-sm focus:outline-none leading-relaxed
                           disabled:opacity-40 disabled:cursor-not-allowed"
                style={{ maxHeight: '120px', overflowY: 'auto' }}
              />
              <button
                id="send-message-btn"
                onClick={handleSend}
                disabled={!input.trim() || !isSessionActive || isTyping}
                className="flex-shrink-0 w-10 h-10 rounded-xl
                           bg-gradient-to-br from-primary-600 to-secondary-500
                           flex items-center justify-center
                           hover:from-primary-500 hover:to-secondary-400
                           transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed
                           shadow-glow-purple hover:shadow-glow-purple"
              >
                <Send size={16} className="text-white" />
              </button>
            </div>
            <p className="text-slate-600 text-[10px] text-center mt-2">
              Inquideo uses Gemini 2.5 Flash · Pinecone vector search · Top-5 semantic retrieval
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

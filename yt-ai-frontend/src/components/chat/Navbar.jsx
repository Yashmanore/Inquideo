import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Youtube, ArrowLeft } from 'lucide-react'
import { useChat } from '../../context/ChatContext.jsx'

export default function Navbar() {
  const { isSessionActive, sessionId, chunksProcessed } = useChat()
  const location = useLocation()
  const isChat = location.pathname === '/chat'

  return (
    <header className="fixed top-0 left-0 right-0 z-50 border-b border-slate-800/60"
            style={{ background: 'rgba(15,23,42,0.85)', backdropFilter: 'blur(16px)' }}>
      <nav className="max-w-screen-2xl mx-auto px-4 h-14 flex items-center justify-between">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-600 to-secondary-500
                          flex items-center justify-center shadow-glow-purple
                          group-hover:scale-110 transition-transform duration-200">
            <Youtube size={16} className="text-white" />
          </div>
          <span className="font-bold text-white text-lg">YT-AI</span>
        </Link>

        {/* Center — session info */}
        {isSessionActive && isChat && (
          <div className="hidden sm:flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
            <span className="text-slate-400 text-xs font-mono">
              Session active · {chunksProcessed} chunks indexed
            </span>
          </div>
        )}

        {/* Right */}
        <div className="flex items-center gap-3">
          {isChat ? (
            <Link to="/" className="btn-ghost text-sm px-3 py-1.5">
              <ArrowLeft size={14} />
              Home
            </Link>
          ) : (
            <Link to="/chat" className="btn-primary text-sm px-4 py-2">
              <Youtube size={14} />
              Open Chat
            </Link>
          )}
        </div>
      </nav>
    </header>
  )
}

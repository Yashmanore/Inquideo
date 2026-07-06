import React from 'react'
import { Github, Youtube } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="border-t border-slate-800 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">

          {/* Brand */}
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-600 to-secondary-500 flex items-center justify-center">
              <Youtube size={16} className="text-white" />
            </div>
            <span className="font-bold text-white">YT-AI</span>
          </div>

          {/* Links */}
          <div className="flex items-center gap-6 text-sm text-slate-500">
            <span>Built by <span className="text-primary-400 font-medium">Yash Manore</span></span>
            <span className="text-slate-700">·</span>
            <span>Spring Boot + React RAG</span>
            <span className="text-slate-700">·</span>
            <span>Gemini + Pinecone</span>
          </div>

          {/* Social */}
          <a
            href="https://github.com"
            target="_blank"
            rel="noopener noreferrer"
            className="p-2 rounded-lg text-slate-500 hover:text-white hover:bg-primary-900/50 transition-colors"
          >
            <Github size={20} />
          </a>
        </div>
      </div>
    </footer>
  )
}

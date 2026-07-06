import React from 'react'
import { motion } from 'framer-motion'
import { useNavigate } from 'react-router-dom'
import { Youtube, ArrowRight, Play, Sparkles } from 'lucide-react'

export default function HeroSection() {
  const navigate = useNavigate()

  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden px-4">
      {/* Animated background orbs */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary-600/20 rounded-full blur-3xl animate-float" />
        <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-accent-400/10 rounded-full blur-3xl animate-float"
             style={{ animationDelay: '-3s' }} />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px]
                        bg-secondary-500/5 rounded-full blur-3xl" />
        {/* Grid overlay */}
        <div className="absolute inset-0 opacity-5"
             style={{
               backgroundImage: 'linear-gradient(rgba(124,58,237,0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(124,58,237,0.5) 1px, transparent 1px)',
               backgroundSize: '60px 60px'
             }} />
      </div>

      <div className="relative z-10 max-w-5xl mx-auto text-center">

        {/* Badge */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex justify-center mb-6"
        >
          <span className="badge-purple text-sm">
            <Sparkles size={14} className="text-primary-400" />
            Powered by Gemini 2.5 Flash + Pinecone
          </span>
        </motion.div>

        {/* Headline */}
        <motion.h1
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
          className="text-5xl sm:text-6xl md:text-7xl font-black leading-tight mb-6"
        >
          Chat With Any{' '}
          <span className="gradient-text block">YouTube Video</span>
        </motion.h1>

        {/* Subheading */}
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="text-lg sm:text-xl text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed"
        >
          Paste any YouTube URL and start asking questions. Our RAG pipeline fetches the transcript,
          creates semantic embeddings, and returns{' '}
          <span className="text-accent-400 font-medium">timestamp-cited answers</span>{' '}
          using Gemini AI.
        </motion.p>

        {/* CTA Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16"
        >
          <button
            id="hero-cta-start"
            onClick={() => navigate('/chat')}
            className="btn-primary text-lg px-8 py-4 animate-pulse-glow"
          >
            <Play size={20} />
            Start Chatting
            <ArrowRight size={18} />
          </button>
          <a
            href="#how-it-works"
            className="btn-ghost text-base"
          >
            How it works
          </a>
        </motion.div>

        {/* Demo card */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.4 }}
          className="glass p-6 max-w-2xl mx-auto border-gradient"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="w-3 h-3 rounded-full bg-red-500" />
            <div className="w-3 h-3 rounded-full bg-yellow-500" />
            <div className="w-3 h-3 rounded-full bg-green-500" />
            <span className="text-slate-500 text-sm font-mono ml-2">ytai.chat</span>
          </div>
          <div className="space-y-3">
            <div className="flex items-center gap-3 p-3 rounded-xl bg-background-800/60">
              <Youtube size={18} className="text-red-400 flex-shrink-0" />
              <span className="text-slate-400 text-sm font-mono truncate">
                https://youtube.com/watch?v=dQw4w9WgXcQ
              </span>
            </div>
            <div className="p-3 rounded-xl bg-primary-900/30 border border-primary-700/30">
              <p className="text-slate-300 text-sm">
                <span className="text-primary-400 font-semibold">You: </span>
                What happens at the beginning of the video?
              </p>
            </div>
            <div className="p-3 rounded-xl bg-background-800/60">
              <p className="text-slate-300 text-sm">
                <span className="text-accent-400 font-semibold">AI: </span>
                At{' '}
                <span className="badge-cyan text-xs px-1.5 py-0.5">0:00 - 0:30</span>
                {' '}the video opens with an introduction...
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}

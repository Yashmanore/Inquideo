import React from 'react'
import { motion } from 'framer-motion'
import { Link2, Scissors, Cpu, Database, Search, MessageSquare, Trash2 } from 'lucide-react'

const steps = [
  { icon: Link2, number: '01', title: 'Paste YouTube URL', desc: 'Enter any YouTube video URL or bare video ID. The system automatically extracts the 11-character video ID.', color: '#7C3AED' },
  { icon: Scissors, number: '02', title: 'Transcript Fetched & Chunked', desc: 'The transcript is fetched and split into 30-second overlapping chunks (5s overlap) using a sliding window algorithm.', color: '#A855F7' },
  { icon: Cpu, number: '03', title: 'Gemini Embeddings Generated', desc: 'Each chunk is embedded into a 768-dimensional vector using gemini-embedding-001 with RETRIEVAL_DOCUMENT task type.', color: '#8B5CF6' },
  { icon: Database, number: '04', title: 'Stored in Pinecone', desc: 'All 768-dim vectors are upserted into your Pinecone index with text and timestamp metadata.', color: '#6D28D9' },
  { icon: Search, number: '05', title: 'Semantic Retrieval', desc: 'Your question is embedded (RETRIEVAL_QUERY) and Pinecone finds the top-5 most semantically similar chunks.', color: '#22D3EE' },
  { icon: MessageSquare, number: '06', title: 'Gemini Generates Answer', desc: 'Gemini 2.5 Flash receives your question, conversation history, and the retrieved context to generate a cited answer.', color: '#06B6D4' },
  { icon: Trash2, number: '07', title: 'Session Cleanup', desc: 'When done, all Pinecone vectors are deleted and the session is cleared — ready for the next video.', color: '#34D399' },
]

export default function HowItWorksSection() {
  return (
    <section id="how-it-works" className="py-24 px-4">
      <div className="max-w-4xl mx-auto">

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <span className="badge-purple mb-4 inline-flex">How It Works</span>
          <h2 className="section-title mb-4">
            The complete{' '}
            <span className="gradient-text">RAG pipeline</span>
          </h2>
          <p className="text-slate-400 text-lg">
            7 steps from YouTube URL to intelligent, cited answer
          </p>
        </motion.div>

        {/* Steps */}
        <div className="relative">
          {/* Connecting line */}
          <div className="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-primary-600 via-secondary-500 to-accent-400 opacity-30" />

          <div className="space-y-6">
            {steps.map((step, i) => (
              <motion.div
                key={step.number}
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.08 }}
                className="relative flex gap-6 group"
              >
                {/* Icon circle */}
                <div
                  className="relative z-10 flex-shrink-0 w-16 h-16 rounded-2xl flex items-center justify-center
                              transition-all duration-300 group-hover:scale-110 group-hover:shadow-glow-purple"
                  style={{
                    background: `${step.color}20`,
                    border: `1px solid ${step.color}40`,
                  }}
                >
                  <step.icon size={22} style={{ color: step.color }} />
                </div>

                {/* Content */}
                <div className="glass flex-1 p-5 group-hover:border-primary-600/30 transition-all duration-300">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="text-xs font-mono text-slate-500">{step.number}</span>
                    <h3 className="font-semibold text-white">{step.title}</h3>
                  </div>
                  <p className="text-slate-400 text-sm leading-relaxed">{step.desc}</p>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

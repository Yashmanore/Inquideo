import React from 'react'
import { motion } from 'framer-motion'
import { Brain, Clock, Zap, MessageSquare, Shield, Layers } from 'lucide-react'

const features = [
  {
    icon: Brain,
    title: 'Semantic RAG Pipeline',
    description: 'Retrieval-Augmented Generation using Gemini embeddings and Pinecone vector search — only the most relevant transcript chunks are used to answer your question.',
    color: 'text-primary-400',
    glow: 'rgba(124,58,237,0.2)',
  },
  {
    icon: Clock,
    title: 'Timestamp Citations',
    description: 'Every answer includes precise [M:SS] timestamps linking you to the exact moment in the video where the information was found.',
    color: 'text-accent-400',
    glow: 'rgba(34,211,238,0.15)',
  },
  {
    icon: MessageSquare,
    title: 'Multi-turn Conversation',
    description: 'Full conversation history maintained per session — ask follow-up questions naturally without losing context from earlier in the chat.',
    color: 'text-secondary-400',
    glow: 'rgba(168,85,247,0.2)',
  },
  {
    icon: Zap,
    title: 'Sliding Window Chunking',
    description: '30-second chunks with 5-second overlap ensure no sentence is ever split across boundaries, preserving full semantic meaning.',
    color: 'text-yellow-400',
    glow: 'rgba(250,204,21,0.15)',
  },
  {
    icon: Layers,
    title: '768-dim Embeddings',
    description: 'Uses gemini-embedding-001 with asymmetric task types — RETRIEVAL_DOCUMENT for storage, RETRIEVAL_QUERY for search — for maximum retrieval quality.',
    color: 'text-green-400',
    glow: 'rgba(74,222,128,0.15)',
  },
  {
    icon: Shield,
    title: 'Session Isolation',
    description: 'Each session uses its own Pinecone namespace, enabling concurrent users to process different videos simultaneously without interference.',
    color: 'text-rose-400',
    glow: 'rgba(251,113,133,0.15)',
  },
]

export default function FeaturesSection() {
  return (
    <section id="features" className="py-24 px-4">
      <div className="max-w-6xl mx-auto">

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-16"
        >
          <span className="badge-purple mb-4 inline-flex">Features</span>
          <h2 className="section-title mb-4">
            Everything you need to{' '}
            <span className="gradient-text">understand any video</span>
          </h2>
          <p className="text-slate-400 text-lg max-w-2xl mx-auto">
            A production-grade RAG pipeline that transforms any YouTube video into an intelligent,
            queryable knowledge base.
          </p>
        </motion.div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature, i) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.1 }}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
              className="glass p-6 group cursor-default"
              style={{ boxShadow: '0 4px 24px rgba(0,0,0,0.3)' }}
            >
              <div
                className="w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-all duration-300 group-hover:scale-110"
                style={{ background: feature.glow }}
              >
                <feature.icon size={24} className={feature.color} />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">{feature.title}</h3>
              <p className="text-slate-400 text-sm leading-relaxed">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}

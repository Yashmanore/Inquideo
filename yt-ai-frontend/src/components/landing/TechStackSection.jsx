import React from 'react'
import { motion } from 'framer-motion'

const technologies = [
  { name: 'Gemini 2.5 Flash', role: 'Answer Generation', emoji: '✨' },
  { name: 'gemini-embedding-001', role: 'Semantic Embeddings', emoji: '🧠' },
  { name: 'Pinecone', role: 'Vector Database', emoji: '🌲' },
  { name: 'Spring Boot 3', role: 'Java Backend', emoji: '☕' },
  { name: 'React + Vite', role: 'Frontend', emoji: '⚛️' },
  { name: 'Tailwind CSS', role: 'Styling', emoji: '🎨' },
  { name: 'youtube-transcript', role: 'Transcript Fetching', emoji: '📹' },
  { name: 'Framer Motion', role: 'Animations', emoji: '🎬' },
]

export default function TechStackSection() {
  return (
    <section id="tech" className="py-24 px-4">
      <div className="max-w-5xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <span className="badge-purple mb-4 inline-flex">Tech Stack</span>
          <h2 className="section-title mb-4">
            Built with{' '}
            <span className="gradient-text">production-grade tools</span>
          </h2>
        </motion.div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {technologies.map((tech, i) => (
            <motion.div
              key={tech.name}
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 0.4, delay: i * 0.07 }}
              whileHover={{ y: -4, scale: 1.03 }}
              className="glass p-4 text-center group cursor-default"
            >
              <div className="text-3xl mb-3 group-hover:scale-125 transition-transform duration-300">
                {tech.emoji}
              </div>
              <h3 className="text-white font-semibold text-sm mb-1">{tech.name}</h3>
              <p className="text-slate-500 text-xs">{tech.role}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}

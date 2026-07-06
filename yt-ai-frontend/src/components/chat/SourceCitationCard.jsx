import React from 'react'
import { Clock } from 'lucide-react'

export default function SourceCitationCard({ source }) {
  return (
    <div className="glass-strong p-3 rounded-xl border border-primary-700/20 text-xs">
      <div className="flex items-center gap-2 mb-2">
        <Clock size={11} className="text-accent-400" />
        <span className="badge-cyan">
          {source.startTime} – {source.endTime}
        </span>
        {source.score > 0 && (
          <span className="text-slate-600 ml-auto font-mono">
            {(source.score * 100).toFixed(0)}% match
          </span>
        )}
      </div>
      <p className="text-slate-400 leading-relaxed line-clamp-3">{source.text}</p>
    </div>
  )
}

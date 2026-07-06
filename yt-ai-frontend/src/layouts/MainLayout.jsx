import React from 'react'
import Navbar from '../components/chat/Navbar.jsx'

export default function MainLayout({ children }) {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="pt-14">
        {children}
      </main>
    </div>
  )
}

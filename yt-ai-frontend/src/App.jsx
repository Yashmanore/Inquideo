import React from 'react'
import { BrowserRouter } from 'react-router-dom'
import { ChatProvider } from './context/ChatContext.jsx'
import AppRoutes from './routes/AppRoutes.jsx'

export default function App() {
  return (
    <BrowserRouter>
      <ChatProvider>
        <AppRoutes />
      </ChatProvider>
    </BrowserRouter>
  )
}

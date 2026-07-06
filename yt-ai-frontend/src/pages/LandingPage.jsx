import React from 'react'
import Navbar from '../components/chat/Navbar.jsx'
import HeroSection from '../components/landing/HeroSection.jsx'
import FeaturesSection from '../components/landing/FeaturesSection.jsx'
import HowItWorksSection from '../components/landing/HowItWorksSection.jsx'
import TechStackSection from '../components/landing/TechStackSection.jsx'
import Footer from '../components/landing/Footer.jsx'

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="pt-14">
        <HeroSection />
        <FeaturesSection />
        <HowItWorksSection />
        <TechStackSection />
      </main>
      <Footer />
    </div>
  )
}

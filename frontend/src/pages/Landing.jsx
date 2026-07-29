import Navbar from "../components/landing/Navbar";
import Hero from "../components/landing/Hero";
import HeroSplit from "../components/landing/HeroSplit";
import Features from "../components/landing/Features";
import HowItWorks from "../components/landing/HowItWorks";
import AISpotlight from "../components/landing/AISpotlight";
import FinalCTA from "../components/landing/FinalCTA";
import Footer from "../components/landing/Footer";

const Landing = () => (
  <div className="min-h-screen overflow-x-clip bg-white">
    <Navbar />
    {/* Hero variation A — centered headline + overlapping cards (original) */}
    <Hero />
    {/* Hero variation B — copy left, animated kanban board right */}
    {/* <HeroSplit /> */}
    <Features />
    <HowItWorks />
    <AISpotlight />
    <FinalCTA />
    <Footer />
  </div>
);

export default Landing;

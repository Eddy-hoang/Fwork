import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Zap } from "lucide-react";
import Button from "../ui/Button";

const navLinks = [
  ["Features", "#features"],
  ["How it works", "#how-it-works"],
  ["AI", "#ai"],
];

const Navbar = () => {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll(); // sync on mount in case the page loads already scrolled
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-30 transition-all duration-300 ease-spring ${
        scrolled ? "px-3 pt-3 sm:px-4" : "glass border-b border-line"
      }`}
    >
      <div
        className={`mx-auto flex items-center justify-between transition-all duration-300 ease-spring ${
          scrolled
            ? "glass mt-3 max-w-5xl rounded-full border border-line px-5 py-2.5 shadow-[var(--shadow-soft)]"
            : "max-w-6xl px-6 py-3.5"
        }`}
      >
        <Link to="/" className="flex items-center gap-2.5 font-semibold">
          <div className="brand-gradient flex h-9 w-9 items-center justify-center rounded-xl shadow-[var(--shadow-brand)]">
            <Zap className="h-4.5 w-4.5 fill-white text-white" />
          </div>
          <span className="font-display text-lg font-bold tracking-tight">Flowboard</span>
        </Link>
        <nav className="hidden items-center gap-1 md:flex">
          {navLinks.map(([label, href]) => (
            <a key={href} href={href} className="rounded-full px-3.5 py-2 text-sm font-medium text-muted transition-colors hover:bg-surface-2 hover:text-ink">
              {label}
            </a>
          ))}
        </nav>
        <div className="flex items-center gap-2">
          <Link to="/login">
            <Button variant="ghost" size="sm">Log in</Button>
          </Link>
          <Link to="/register">
            <Button size="sm">Get started</Button>
          </Link>
        </div>
      </div>
    </header>
  );
};

export default Navbar;

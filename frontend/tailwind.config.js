/** @type {import('tailwindcss').Config} */
// Mesmos tokens do design aprovado (Lovable): cores definidas como variaveis CSS em src/styles.css.
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif']
      },
      colors: {
        background: 'var(--background)',
        foreground: 'rgb(var(--foreground-rgb) / <alpha-value>)',
        card: 'var(--card)',
        primary: { DEFAULT: 'rgb(var(--primary-rgb) / <alpha-value>)', foreground: 'var(--primary-foreground)', soft: 'var(--primary-soft)' },
        muted: { DEFAULT: 'var(--muted)', foreground: 'var(--muted-foreground)' },
        accent: { DEFAULT: 'var(--accent)', foreground: 'var(--accent-foreground)' },
        destructive: { DEFAULT: 'rgb(var(--destructive-rgb) / <alpha-value>)', soft: 'var(--error-soft)' },
        success: { DEFAULT: 'rgb(var(--success-rgb) / <alpha-value>)', soft: 'var(--success-soft)' },
        warning: { DEFAULT: 'rgb(var(--warning-rgb) / <alpha-value>)', soft: 'var(--warning-soft)' },
        border: 'var(--border)',
        input: 'var(--border)',
        ring: 'var(--primary)',
        riasec: {
          r: 'var(--riasec-r)', i: 'var(--riasec-i)', a: 'var(--riasec-a)',
          s: 'var(--riasec-s)', e: 'var(--riasec-e)', c: 'var(--riasec-c)'
        }
      },
      borderRadius: { md: '0.5rem', lg: '0.75rem', xl: '0.75rem' },
      boxShadow: { lifted: '0 2px 8px rgb(17 17 17 / 6%)' }
    }
  },
  plugins: []
};

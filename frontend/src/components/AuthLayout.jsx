import { Link } from 'react-router-dom';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { BlurFade } from '@/components/ui/blur-fade';
import { TextAnimate } from '@/components/ui/text-animate';

/** Two columns on desktop: the calm panel with the product line, then the form. On phones the panel is a short header. */
export default function AuthLayout({ title, intro, error, children, footer }) {
  return (
    <div className="grid min-h-dvh lg:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      <header className="relative flex flex-col justify-between gap-6 overflow-hidden border-b border-line bg-hero px-5 py-6 sm:px-8 lg:border-r lg:border-b-0 lg:py-10">
        <Link to="/" className="relative w-fit font-display text-2xl font-extrabold text-mist">
          EventSphere
        </Link>
        <TextAnimate
          as="p"
          by="word"
          animation="blurInUp"
          once
          startOnView={false}
          className="relative max-w-md font-display text-4xl leading-tight font-extralight text-mist sm:text-5xl"
        >
          Book seats at campus talks, workshops and fests.
        </TextAnimate>
      </header>

      <main className="flex items-start justify-center px-5 py-10 sm:px-8 lg:items-center">
        <BlurFade delay={0.15} className="w-full max-w-md">
          <h1 className="font-display text-5xl font-extrabold tracking-tight text-mist">{title}</h1>
          <p className="mt-3 text-ash">{intro}</p>

          {error && (
            <Alert role="alert" className="mt-6 border-clay/50 bg-clay/10">
              <AlertDescription className="text-mist">{error}</AlertDescription>
            </Alert>
          )}

          <div className="mt-8">{children}</div>
          <div className="mt-6 space-y-3 text-sm text-ash">{footer}</div>
        </BlurFade>
      </main>
    </div>
  );
}

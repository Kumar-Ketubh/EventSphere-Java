import { InteractiveGridPattern } from '@/components/ui/interactive-grid-pattern';

/**
 * The site-wide background: a black field with a faint interactive grid, fixed behind every page.
 * The square under the pointer lights up in sand; the grid fades out towards the edges.
 * It never takes clicks (pointer-events-none); it follows the pointer through a window listener.
 */
export default function PageGrid() {
  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-void">
      <InteractiveGridPattern
        trackWindow
        width={40}
        height={40}
        squares={[64, 40]}
        className="border-none [mask-image:radial-gradient(900px_circle_at_50%_30%,white,transparent)]"
        squaresClassName="stroke-mist/[0.06]"
        activeClassName="fill-sand/25"
      />
    </div>
  );
}

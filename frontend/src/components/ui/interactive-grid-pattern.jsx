import React, { useEffect, useRef, useState } from "react"

import { cn } from "@/lib/utils"

/**
 * The InteractiveGridPattern component.
 *
 * EventSphere change: with `trackWindow`, the lit square follows the pointer anywhere in the
 * window (via a window mousemove listener), so the grid can sit behind page content and still
 * respond, while the content keeps every click. `activeClassName` styles the lit square.
 */
export function InteractiveGridPattern({
  width = 40,
  height = 40,
  squares = [24, 24],
  className,
  squaresClassName,
  activeClassName = "fill-gray-300/30",
  trackWindow = false,
  ...props
}) {
  const [horizontal, vertical] = squares
  const [hoveredSquare, setHoveredSquare] = useState(null)
  const svgRef = useRef(null)

  useEffect(() => {
    if (!trackWindow) return undefined
    const onMove = (e) => {
      const rect = svgRef.current?.getBoundingClientRect()
      if (!rect) return
      const col = Math.floor((e.clientX - rect.left) / width)
      const row = Math.floor((e.clientY - rect.top) / height)
      const inside = col >= 0 && row >= 0 && col < horizontal && row < vertical
      setHoveredSquare(inside ? row * horizontal + col : null)
    }
    const onLeave = () => setHoveredSquare(null)
    window.addEventListener("mousemove", onMove, { passive: true })
    document.addEventListener("mouseleave", onLeave)
    return () => {
      window.removeEventListener("mousemove", onMove)
      document.removeEventListener("mouseleave", onLeave)
    }
  }, [trackWindow, width, height, horizontal, vertical])

  return (
    <svg
      ref={svgRef}
      width={width * horizontal}
      height={height * vertical}
      className={cn(
        "absolute inset-0 h-full w-full border border-gray-400/30",
        className
      )}
      {...props}
    >
      {Array.from({ length: horizontal * vertical }).map((_, index) => {
        const x = (index % horizontal) * width
        const y = Math.floor(index / horizontal) * height
        return (
          <rect
            key={index}
            x={x}
            y={y}
            width={width}
            height={height}
            className={cn(
              "stroke-gray-400/30 transition-all duration-100 ease-in-out not-[&:hover]:duration-1000",
              hoveredSquare === index ? activeClassName : "fill-transparent",
              squaresClassName
            )}
            onMouseEnter={trackWindow ? undefined : () => setHoveredSquare(index)}
            onMouseLeave={trackWindow ? undefined : () => setHoveredSquare(null)}
          />
        )
      })}
    </svg>
  )
}

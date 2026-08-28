import type { ReactEventHandler } from 'react';

export interface SvgIconProps {
  size?: number;
  title?: string;
  className?: string;
}

/** Bare <img> wrapper with the shared sizing/alignment classes. */
export function SvgImg({
  src,
  alt,
  title,
  size = 24,
  className,
  onError,
}: {
  src: string;
  alt: string;
  title?: string;
  size?: number;
  className?: string;
  onError?: ReactEventHandler<HTMLImageElement>;
}) {
  return (
    <img
      src={src}
      alt={alt}
      title={title || alt}
      width={size}
      height={size}
      className={`jt:inline-block jt:align-middle ${className || ''}`}
      onError={onError}
    />
  );
}

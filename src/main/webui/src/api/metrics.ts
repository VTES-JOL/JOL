import { api } from './client';
import type { HeatmapCell, MetricBucket, MetricGrain, MetricSeries, MetricTotals } from './types';

// Public analytics over metric_event (MetricsResource). `from`/`to` are
// YYYY-MM-DD (UTC, `to` inclusive); omit both for the trailing 90 days or pass
// `all: true` for everything. `tz` is an IANA id and only affects bucketing.
export interface MetricQuery {
  from?: string;
  to?: string;
  all?: boolean;
  tz?: string;
  tournamentOnly?: boolean;
}

export interface MetricSeriesQuery extends MetricQuery {
  grain?: MetricGrain;
}

export interface MetricDimensionQuery extends MetricSeriesQuery {
  limit?: number;
}

function qs(params: Record<string, string | number | boolean | undefined>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') search.set(key, String(value));
  }
  const s = search.toString();
  return s ? `?${s}` : '';
}

export function getMetricsTimeseries(q: MetricSeriesQuery = {}): Promise<MetricBucket[]> {
  return api.get<MetricBucket[]>(`/metrics/timeseries${qs({ ...q })}`);
}

export function getMetricsByPlayer(q: MetricDimensionQuery = {}): Promise<MetricSeries[]> {
  return api.get<MetricSeries[]>(`/metrics/by-player${qs({ ...q })}`);
}

export function getMetricsByGame(q: MetricDimensionQuery = {}): Promise<MetricSeries[]> {
  return api.get<MetricSeries[]>(`/metrics/by-game${qs({ ...q })}`);
}

export function getMetricsHeatmap(q: MetricQuery = {}): Promise<HeatmapCell[]> {
  return api.get<HeatmapCell[]>(`/metrics/heatmap${qs({ ...q })}`);
}

export function getMetricsTotals(q: MetricQuery = {}): Promise<MetricTotals> {
  return api.get<MetricTotals>(`/metrics/totals${qs({ ...q })}`);
}

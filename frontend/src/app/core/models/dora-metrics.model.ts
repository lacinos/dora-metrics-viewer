export interface TimeWindow {
  start: string; // ISO-8601
  end: string;   // ISO-8601
}

export interface ScanRequest {
  repoUrl: string;
  timeWindow: TimeWindow;
}

export interface DoraMetricsResult {
  leadTimeForChanges: string; // "PT48H"
  deploymentFrequency: number; // e.g., 0.28 (deployments per day)
  changeFailureRate: number;   // e.g., 50.0 (percent)
  timeToRestoreService: string; // "PT4H"
}
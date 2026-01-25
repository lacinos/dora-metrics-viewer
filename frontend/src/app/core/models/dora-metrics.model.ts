export interface TimeWindow {
  start: string; // ISO-8601
  end: string;   // ISO-8601
}

export interface ScanRequest {
  repoUrl: string;
  timeWindow: TimeWindow;
}

export interface DoraMetricsResult {
  // leadTimeForChanges comes as a Duration string (PT...) or seconds depending on config.
  // We'll handle it as string for safety as that's the default ISO serialization for Duration unless changed.
  // If it comes as number, we'll adapt.
  leadTimeForChanges: string | number;
  
  // Future proofing for other metrics
  deploymentFrequency?: string | number;
  changeFailureRate?: number;
  timeToRestoreService?: string | number;
}

package com.gemini.dorametricsviewer.domain;

import java.time.Instant;

public record TimeWindow(Instant start, Instant end) {
}

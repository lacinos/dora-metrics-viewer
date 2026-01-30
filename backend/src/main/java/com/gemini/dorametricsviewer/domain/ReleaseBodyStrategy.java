package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.util.List;
import java.util.Optional;

public class ReleaseBodyStrategy implements MatchStrategy {
    @Override
    public Optional<Deployment> findDeployment(Change change, List<Deployment> deployments) {
        String prNumber = extractPrNumber(change.id());
        if (prNumber == null) {
            return Optional.empty();
        }

        return deployments.stream()
                .filter(d -> d.description() != null)
                .filter(d -> containsPrReference(d.description(), prNumber))
                .findFirst();
    }

    private String extractPrNumber(String changeId) {
        // Expected format: .../pr/{number}
        int idx = changeId.lastIndexOf("/pr/");
        if (idx != -1) {
            return changeId.substring(idx + 4);
        }
        return null;
    }

    private boolean containsPrReference(String description, String prNumber) {
        // Check for #123
        if (description.contains("#" + prNumber)) return true;
        // Check for /pull/123
        if (description.contains("/pull/" + prNumber)) return true;
        return false;
    }
}

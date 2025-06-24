package com.multikube_rest_service.common.enums;

/**
 * Defines the type of a workload template.
 */
public enum TemplateType {
    /**
     * A standard Kubernetes YAML manifest.
     */
    YAML,
    /**
     * A Helm Chart (for future implementation).
     */
    HELM_CHART
}
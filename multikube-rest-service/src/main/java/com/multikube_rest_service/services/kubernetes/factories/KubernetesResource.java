package com.multikube_rest_service.services.kubernetes.factories;

/**
 * A simple data carrier record to hold the essential details of a Kubernetes resource manifest.
 *
 * @param k8sName The name of the resource from its metadata.
 * @param k8sKind The kind of the resource (e.g., 'Deployment', 'NetworkPolicy').
 * @param yaml    The full YAML content of the resource.
 */
public record KubernetesResource(String k8sName, String k8sKind, String yaml) {
}
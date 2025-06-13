package com.multikube_rest_service.services.kubernetes;

import com.multikube_rest_service.entities.provider.KubernetesCluster;
import io.kubernetes.client.openapi.ApiException;

import java.io.IOException;

/**
 * A service for interacting with a Kubernetes cluster via its API.
 * This abstracts the complexities of the Kubernetes Java client.
 */
public interface KubernetesClientService {

    /**
     * Creates a new namespace in the specified cluster.
     *
     * @param cluster The target KubernetesCluster entity, containing connection details.
     * @param namespaceName The name of the namespace to create.
     * @throws io.kubernetes.client.openapi.ApiException if the API call fails.
     */
    void createNamespace(KubernetesCluster cluster, String namespaceName) throws ApiException;

    /**
     * Applies a YAML manifest to a specific namespace within a cluster.
     * This is designed to be idempotent, similar to `kubectl apply`. It will attempt to
     * create the resource, and if it already exists, it will attempt to replace it.
     *
     * @param cluster The target KubernetesCluster entity.
     * @param namespace The namespace where the resource will be applied.
     * @param yamlContent A string containing the full YAML manifest of the resource.
     * @throws io.kubernetes.client.openapi.ApiException if the API call fails.
     * @throws java.io.IOException if the YAML content is invalid.
     */
    void apply(KubernetesCluster cluster, String namespace, String yamlContent) throws IOException, ApiException;
}
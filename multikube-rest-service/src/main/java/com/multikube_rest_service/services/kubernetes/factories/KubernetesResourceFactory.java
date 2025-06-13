package com.multikube_rest_service.services.kubernetes.factories;

/**
 * A factory for creating default Kubernetes resource manifests and parsing user-provided YAML.
 */
public interface KubernetesResourceFactory {

    /**
     * Creates a default NetworkPolicy that denies all ingress traffic to a namespace by default.
     *
     * @param namespace The name of the namespace.
     * @return A KubernetesResource containing the NetworkPolicy details.
     */
    KubernetesResource createDefaultNetworkPolicy(String namespace);

    /**
     * Creates a default Role with administrative privileges within a namespace.
     *
     * @param namespace The name of the namespace.
     * @return A KubernetesResource containing the Role details.
     */
    KubernetesResource createDefaultAdminRole(String namespace);

    /**
     * Creates a RoleBinding to grant the default admin role to a specific user.
     *
     * @param namespace The name of the namespace.
     * @param username  The username of the user to bind the role to.
     * @return A KubernetesResource containing the RoleBinding details.
     */
    KubernetesResource createAdminRoleBinding(String namespace, String username);

    /**
     * Parses a raw YAML string to extract its kind and name.
     *
     * @param yamlContent The raw YAML string.
     * @return A KubernetesResource containing the parsed details and original YAML.
     * @throws IllegalArgumentException if the YAML is invalid or missing required fields.
     */
    KubernetesResource parseYaml(String yamlContent);
}
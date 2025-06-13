package com.multikube_rest_service.services.kubernetes.factories;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Objects;

@Component
public class KubernetesResourceFactoryImpl implements KubernetesResourceFactory {

    @Override
    public KubernetesResource createDefaultNetworkPolicy(String namespace) {
        final String name = "default-deny-ingress";
        final String kind = "NetworkPolicy";
        final String yaml = """
            apiVersion: networking.k8s.io/v1
            kind: NetworkPolicy
            metadata:
              name: %s
              namespace: %s
            spec:
              podSelector: {} # An empty podSelector selects all pods in the namespace.
              policyTypes:
              - Ingress
              # No ingress rules are defined, so all ingress traffic is denied by default.
              # Egress traffic is not specified, so it is allowed by default.
            """.formatted(name, namespace);
        return new KubernetesResource(name, kind, yaml);
    }

    @Override
    public KubernetesResource createDefaultAdminRole(String namespace) {
        final String name = "namespace-admin-role";
        final String kind = "Role";
        final String yaml = """
            apiVersion: rbac.authorization.k8s.io/v1
            kind: Role
            metadata:
              name: %s
              namespace: %s
            rules:
            - apiGroups: ["*"] # "" indicates the core API group
              resources: ["*"]
              verbs: ["*"]
            """.formatted(name, namespace);
        return new KubernetesResource(name, kind, yaml);
    }

    @Override
    public KubernetesResource createAdminRoleBinding(String namespace, String username) {
        final String name = "admin-binding-" + username.replaceAll("[^a-zA-Z0-9-]", "-");
        final String kind = "RoleBinding";
        final String yaml = """
            apiVersion: rbac.authorization.k8s.io/v1
            kind: RoleBinding
            metadata:
              name: %s
              namespace: %s
            subjects:
            - kind: User
              name: %s
              apiGroup: rbac.authorization.k8s.io
            roleRef:
              kind: Role
              name: namespace-admin-role
              apiGroup: rbac.authorization.k8s.io
            """.formatted(name, namespace, username);
        return new KubernetesResource(name, kind, yaml);
    }

    @Override
    public KubernetesResource parseYaml(String yamlContent) {
        try {
            Yaml yamlParser = new Yaml();
            Map<String, Object> data = yamlParser.load(yamlContent);

            String kind = (String) Objects.requireNonNull(data.get("kind"), "YAML must contain a 'kind' field.");

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) Objects.requireNonNull(data.get("metadata"), "YAML must contain a 'metadata' block.");
            String name = (String) Objects.requireNonNull(metadata.get("name"), "YAML metadata must contain a 'name' field.");

            return new KubernetesResource(name, kind, yamlContent);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse provided YAML: " + e.getMessage(), e);
        }
    }
}
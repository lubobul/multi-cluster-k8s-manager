package com.multikube_rest_service.services.kubernetes;

import com.multikube_rest_service.common.encryption.KubeconfigEncryptor;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Service
public class KubernetesClientServiceImpl implements KubernetesClientService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesClientServiceImpl.class);

    private final KubeconfigEncryptor kubeconfigEncryptor;

    public KubernetesClientServiceImpl(KubeconfigEncryptor kubeconfigEncryptor) {
        this.kubeconfigEncryptor = kubeconfigEncryptor;
    }

    @Override
    public void createNamespace(KubernetesCluster cluster, String namespaceName) throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient(cluster));

        V1Namespace namespace = new V1Namespace();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(namespaceName);
        namespace.setMetadata(metadata);

        api.createNamespace(namespace).execute();
    }

    @Override
    public void apply(KubernetesCluster cluster, String namespace, String yamlContent) throws IOException, ApiException {
        ApiClient apiClient = getApiClient(cluster);
        Object obj = Yaml.load(yamlContent);

        // Based on the 'kind' of the loaded object, delegate to the correct API group.
        if (obj instanceof V1Role) {
            applyRole(apiClient, namespace, (V1Role) obj);
        } else if (obj instanceof V1RoleBinding) {
            applyRoleBinding(apiClient, namespace, (V1RoleBinding) obj);
        } else if (obj instanceof V1NetworkPolicy) {
            applyNetworkPolicy(apiClient, namespace, (V1NetworkPolicy) obj);
        } else if (obj instanceof V1ResourceQuota) {
            applyResourceQuota(apiClient, namespace, (V1ResourceQuota) obj);
        } else if (obj instanceof V1LimitRange) {
            applyLimitRange(apiClient, namespace, (V1LimitRange) obj);
        } else if (obj instanceof V1Deployment) { // <-- ADD THIS NEW CONDITION
            applyDeployment(apiClient, namespace, (V1Deployment) obj);
        } else {
            logger.warn("Unsupported Kind for apply: {}", obj.getClass().getSimpleName());
            throw new IllegalArgumentException("Unsupported Kind for apply: " + obj.getClass().getSimpleName());
        }
    }

    // --- Private Helper Methods ---

    private void applyRole(ApiClient apiClient, String namespace, V1Role role) throws ApiException {
        RbacAuthorizationV1Api rbacApi = new RbacAuthorizationV1Api(apiClient);
        try {
            rbacApi.createNamespacedRole(namespace, role).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) { // 409 Conflict indicates the resource already exists
                logger.debug("Role {} already exists in namespace {}. Replacing.", role.getMetadata().getName(), namespace);
                rbacApi.replaceNamespacedRole(role.getMetadata().getName(), namespace, role).execute();
            } else {
                throw e;
            }
        }
    }

    private void applyRoleBinding(ApiClient apiClient, String namespace, V1RoleBinding roleBinding) throws ApiException {
        RbacAuthorizationV1Api rbacApi = new RbacAuthorizationV1Api(apiClient);
        try {
            rbacApi.createNamespacedRoleBinding(namespace, roleBinding).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                logger.debug("RoleBinding {} already exists in namespace {}. Replacing.", roleBinding.getMetadata().getName(), namespace);
                rbacApi.replaceNamespacedRoleBinding(roleBinding.getMetadata().getName(), namespace, roleBinding).execute();
            } else {
                throw e;
            }
        }
    }

    private void applyNetworkPolicy(ApiClient apiClient, String namespace, V1NetworkPolicy networkPolicy) throws ApiException {
        NetworkingV1Api networkingApi = new NetworkingV1Api(apiClient);
        try {
            networkingApi.createNamespacedNetworkPolicy(namespace, networkPolicy).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                logger.debug("NetworkPolicy {} already exists in namespace {}. Replacing.", networkPolicy.getMetadata().getName(), namespace);
                networkingApi.replaceNamespacedNetworkPolicy(networkPolicy.getMetadata().getName(), namespace, networkPolicy).execute();
            } else {
                throw e;
            }
        }
    }

    private void applyResourceQuota(ApiClient apiClient, String namespace, V1ResourceQuota resourceQuota) throws ApiException {
        CoreV1Api coreApi = new CoreV1Api(apiClient);
        try {
            coreApi.createNamespacedResourceQuota(namespace, resourceQuota).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                assert resourceQuota.getMetadata() != null;
                logger.debug("ResourceQuota {} already exists in namespace {}. Replacing.", resourceQuota.getMetadata().getName(), namespace);
                assert resourceQuota.getMetadata().getName() != null;
                coreApi.replaceNamespacedResourceQuota(resourceQuota.getMetadata().getName(), namespace, resourceQuota).execute();
            } else {
                throw e;
            }
        }
    }

    private void applyLimitRange(ApiClient apiClient, String namespace, V1LimitRange limitRange) throws ApiException {
        CoreV1Api coreApi = new CoreV1Api(apiClient);
        try {
            coreApi.createNamespacedLimitRange(namespace, limitRange).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                logger.debug("LimitRange {} already exists in namespace {}. Replacing.", limitRange.getMetadata().getName(), namespace);
                coreApi.replaceNamespacedLimitRange(limitRange.getMetadata().getName(), namespace, limitRange).execute();
            } else {
                throw e;
            }
        }
    }

    private void applyDeployment(ApiClient apiClient, String namespace, V1Deployment deployment) throws ApiException {
        AppsV1Api appsApi = new AppsV1Api(apiClient);
        try {
            // Attempt to create the deployment
            appsApi.createNamespacedDeployment(namespace, deployment).execute();
        } catch (ApiException e) {
            if (e.getCode() == 409) { // 409 Conflict means it already exists
                logger.debug("Deployment {} already exists in namespace {}. Replacing.", deployment.getMetadata().getName(), namespace);
                // If it exists, replace it
                appsApi.replaceNamespacedDeployment(deployment.getMetadata().getName(), namespace, deployment).execute();
            } else {
                // If it's another error, re-throw it
                throw e;
            }
        }
    }

    /**
     * Creates a configured ApiClient from the encrypted kubeconfig stored in the cluster entity.
     * @param cluster The cluster entity.
     * @return A configured ApiClient.
     * @throws RuntimeException if the kubeconfig cannot be processed.
     */
    private ApiClient getApiClient(KubernetesCluster cluster) {
        try {
            String decryptedKubeconfig = kubeconfigEncryptor.decrypt(cluster.getKubeconfigEncrypted());
            return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(decryptedKubeconfig))).build();
        } catch (IOException e) {
            logger.error("Failed to process kubeconfig for cluster ID: {}", cluster.getId(), e);
            throw new RuntimeException("Failed to process kubeconfig for cluster " + cluster.getName(), e);
        }
    }
}
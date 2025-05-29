package com.multikube_rest_service.schedulers;

import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.repositories.provider.KubernetesClusterRepository;
import com.multikube_rest_service.services.provider.ProviderClusterService; // Ensure this import is correct
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; // Still a Spring-managed service/component
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled service to periodically check and update the status of registered Kubernetes clusters.
 */
@Service // It's still a service/component that Spring needs to manage
public class ClusterStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusScheduler.class);

    private final KubernetesClusterRepository clusterRepository;
    private final ProviderClusterService providerClusterService;

    /**
     * Constructs the ClusterStatusScheduler.
     *
     * @param clusterRepository      The repository for Kubernetes cluster data.
     * @param providerClusterService The service containing cluster verification logic.
     */
    public ClusterStatusScheduler(KubernetesClusterRepository clusterRepository,
                                  ProviderClusterService providerClusterService) {
        this.clusterRepository = clusterRepository;
        this.providerClusterService = providerClusterService;
    }

    /**
     * Periodically checks the status of all registered Kubernetes clusters.
     * This method is scheduled to run at a fixed rate.
     * It fetches all clusters that are not in a terminal error state or pending verification indefinitely.
     */
    @Scheduled(fixedRateString = "${multikube.cluster.status.check.rate.ms:300000}")
    @Transactional
    public void updateClusterStatuses() {
        logger.info("Starting scheduled task to update cluster statuses.");

        List<KubernetesCluster> clustersToCheck = clusterRepository.findAll();

        if (clustersToCheck.isEmpty()) {
            logger.info("No clusters found to check status.");
            return;
        }

        logger.info("Found {} clusters to check.", clustersToCheck.size());

        for (KubernetesCluster cluster : clustersToCheck) {
            if (cluster.getStatus() == ClusterStatus.ACTIVE ||
                cluster.getStatus() == ClusterStatus.UNREACHABLE ||
                cluster.getStatus() == ClusterStatus.PENDING_VERIFICATION ||
                cluster.getStatus() == ClusterStatus.ERROR) {

                logger.debug("Checking status for cluster ID: {}, Name: {}", cluster.getId(), cluster.getName());
                try {
                    providerClusterService.tryToVerifyConnectivity(cluster);
                    logger.info("Status check complete for cluster ID: {}. New status: {}", cluster.getId(), cluster.getStatus());
                } catch (Exception e) {
                    logger.error("Unexpected error during status check for cluster ID: {}: {}", cluster.getId(), e.getMessage(), e);
                }
            } else {
                logger.debug("Skipping status check for cluster ID: {} with current status: {}", cluster.getId(), cluster.getStatus());
            }
        }
        logger.info("Finished scheduled task to update cluster statuses.");
    }
}
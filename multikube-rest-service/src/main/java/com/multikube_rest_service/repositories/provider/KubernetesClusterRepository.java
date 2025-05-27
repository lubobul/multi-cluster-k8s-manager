package com.multikube_rest_service.repositories.provider;

import com.multikube_rest_service.entities.provider.KubernetesCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KubernetesClusterRepository extends JpaRepository<KubernetesCluster, Long> {
    Optional<KubernetesCluster> findByName(String name);
}
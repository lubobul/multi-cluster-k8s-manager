package com.multikube_rest_service.entities.tenant;

import com.multikube_rest_service.common.enums.TemplateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
// No longer importing Set or HashSet

/**
 * Represents a reusable workload template (e.g., for a microservice, a database, etc.).
 * Each template belongs to a TemplateCatalog.
 */
@Entity
@Getter
@Setter
@Table(name = "workload_templates")
public class WorkloadTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private TemplateType templateType;

    @Lob
    @Column(name = "yaml_content", nullable = false, columnDefinition = "TEXT")
    private String yamlContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_catalog_id", nullable = false)
    private TemplateCatalog templateCatalog;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
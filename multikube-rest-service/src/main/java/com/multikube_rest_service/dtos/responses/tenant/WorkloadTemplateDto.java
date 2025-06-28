package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.TemplateType;
import lombok.Data;
import java.sql.Timestamp;

/**
 * A detailed DTO for a WorkloadTemplate, including the full YAML content.
 * Intended for single-item views (e.g., GET /catalogs/{catalogId}/templates/{templateId}).
 */
@Data
public class WorkloadTemplateDto {
    private Long id;
    private String name;
    private String description;
    private TemplateType templateType;
    private String yamlContent; // Included for detail view
    private CatalogSummaryDto catalog;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
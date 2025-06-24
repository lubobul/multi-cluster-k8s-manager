package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.TemplateType;
import lombok.Data;
import java.sql.Timestamp;

/**
 * A lightweight summary DTO for a WorkloadTemplate.
 * It omits the yamlContent to keep paginated list views fast and responsive.
 */
@Data
public class WorkloadTemplateSummaryDto {
    private Long id;
    private String name;
    private String description;
    private TemplateType templateType;
    private Long catalogId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
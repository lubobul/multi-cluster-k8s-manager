import {EntityReference} from '../../ReferenceType';

/**
 * Represents the type of a workload template.
 * This should match the TemplateType enum in the backend.
 */
export enum TemplateType {
    "YAML" = "YAML",
    "HELM_CHART" = "HELM_CHART",
}

/**
 * Represents a detailed view of a workload template, including its full YAML content.
 * This interface matches the WorkloadTemplateDto from the backend.
 */
export interface WorkloadTemplate {
    /**
     * The unique identifier for the template.
     */
    id: number;

    /**
     * The user-friendly name of the template.
     */
    name: string;

    /**
     * An optional description for the template.
     */
    description?: string;

    /**
     * The type of the template (e.g., YAML or HELM_CHART).
     */
    templateType: TemplateType;

    /**
     * The full YAML content of the template.
     */
    yamlContent?: string;

    /**
     * The ID of the parent catalog this template belongs to.
     */
    catalog: EntityReference;

    /**
     * The timestamp of when the template was created, as an ISO 8601 string.
     */
    createdAt: string;

    /**
     * The timestamp of the last update to the template, as an ISO 8601 string.
     */
    updatedAt: string;
}

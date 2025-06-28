import {TemplateType} from '../responses/WorkloadTemplate';

export interface CreateWorkloadTemplateRequest {
    /**
     * The ID of the parent catalog where this template will be created.
     */
    catalogId: number;

    /**
     * The user-friendly name of the template.
     */
    name: string;

    /**
     * An optional description for the template. Can be null or an empty string.
     */
    description?: string | null;

    /**
     * The type of the template (e.g., YAML or HELM_CHART).
     */
    templateType: TemplateType;

    /**
     * The full YAML content of the template.
     */
    yamlContent: string;
}

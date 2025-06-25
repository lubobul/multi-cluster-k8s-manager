/**
 * Represents a catalog for grouping workload templates.
 * This interface matches the TemplateCatalogDto from the backend.
 */
export interface TemplateCatalog {
  /**
   * The unique identifier for the catalog.
   */
  id: number;

  /**
   * The user-friendly name of the catalog.
   */
  name: string;

  /**
   * An optional description for the catalog.
   */
  description: string | null;

  /**
   * A flag indicating if this is a system-default catalog (true)
   * or a tenant-owned catalog (false).
   */
  systemDefault: boolean;

  /**
   * The number of templates contained within this catalog.
   */
  templatesCount: number;
}

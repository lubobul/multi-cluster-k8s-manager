## Multikube: A Declarative Kubernetes Multi-Cluster & Multi-Tenant Management Platform

### 1. The Problem: The Complexity of Enterprise Kubernetes

Kubernetes has become the de facto standard for container orchestration, but its power and flexibility come with significant operational complexity. For mid-to-large-sized organizations, managing a growing fleet of Kubernetes clusters presents several major challenges:

* **High Expertise Barrier:** Properly configuring, securing, and managing Kubernetes requires deep and specialized DevOps expertise. This knowledge is often scarce and expensive, creating a bottleneck for development teams who just want to deploy their applications.
* **Lack of Secure Multi-Tenancy:** Natively, Kubernetes does not offer a simple, built-in solution for securely isolating multiple teams, departments, or customers (tenants) on a shared cluster. Implementing this requires a complex combination of RBAC, Network Policies, and resource quotas, which is error-prone and difficult to manage at scale.
* **Inconsistent Environments:** Without a centralized management plane, each team may end up with slightly different cluster configurations, leading to inconsistent security postures, unpredictable performance, and a high operational burden for the central platform or infrastructure team.
* **Resource Governance:** Allocating and tracking resource usage across different teams is a significant challenge, making it difficult to enforce budgets and ensure fair resource distribution.

### 2. The Business Case: Empowering Organizations Through Simplified Management

Multikube is designed for mid-to-large-sized businesses and platform engineering teams who need to provide a stable, secure, and easy-to-use Kubernetes-as-a-Service experience to their internal developers or external customers.

By using Multikube, an organization can:

* **Democratize Kubernetes Access:** Abstract away the underlying complexity, allowing development teams to self-serve secure, pre-configured namespaces without needing to be Kubernetes experts.
* **Enforce Security & Governance:** Ensure that every environment provisioned through the platform adheres to the company's security and governance standards from day one.
* **Improve Operational Efficiency:** Centralize the management of clusters and tenants, reducing the operational overhead on the platform team and enabling them to manage a larger infrastructure footprint with a smaller team.
* **Provide Clear Tenancy Models:** Offer clear, isolated environments to different business units or customers, ensuring security and preventing resource contention.

### 3. The Solution: The Multikube Platform

Multikube is a centralized management plane that sits on top of a fleet of Kubernetes clusters. It provides a clear, role-based interface for administrators to manage the infrastructure and for tenants to consume it safely and efficiently.

The core workflow is based on two main personas:

* **The Provider (Platform Admin):** An administrator who onboards existing Kubernetes clusters into Multikube. They are responsible for creating tenant organizations and allocating one or more **dedicated clusters** to each tenant.
* **The Tenant (Development Team / Customer):** A tenant receives exclusive access to their allocated clusters. The **Tenant Admin** can then create sandboxed **Namespaces** within their clusters. These namespaces come pre-configured with security defaults, and the Tenant Admin can then grant access to their developers (**Tenant Users**) to deploy and manage applications.

### 4. Application Architecture

Multikube is built on a set of robust and scalable architectural principles designed for a production environment.

#### **a. Secure Multi-Tenancy Model**

The platform's tenancy model is founded on the **dedicated cluster** principle, where each tenant is assigned one or more entire Kubernetes clusters for their exclusive use. Within their own cluster, Tenant Admins can create multiple namespaces. This provides the strongest possible security and resource isolation between tenants.

#### **b. Hybrid State Management: "Detect and Report Drift"**

This is a cornerstone of the Multikube architecture, defining how it interacts with the live Kubernetes clusters.

* **Source of Intent:** The Multikube database is the single source of truth for what the user *intended* to configure. It stores the YAML manifests for all the resources it manages (e.g., the default NetworkPolicy, a user-defined ResourceQuota).
* **Source of Truth:** The live Kubernetes cluster is always considered the absolute source of truth for what is *currently running*.
* **Drift Detection:** Multikube does not enforce a rigid, top-down configuration. Instead, a background process is designed to periodically compare the "intended state" in its database with the "actual state" in Kubernetes. If a user manually changes a resource using `kubectl`, Multikube will flag this as **"Drift Detected"** in the UI.
* **User-Driven Reconciliation:** The platform will not automatically revert external changes. Instead, it empowers the user by showing them the drift and offering two clear choices:
    1.  **Revert:** Re-apply the configuration from the Multikube database to bring the cluster back to the intended state.
    2.  **Adopt:** Update the record in the Multikube database with the changes from the cluster, making the external change the new "intended state."

#### **c. "Day 1" vs. "Day 2" Operations**

To ensure all environments are secure and well-governed by default, Multikube differentiates between initial setup and ongoing management:

* **Day 1 (Automated Provisioning):** When a Tenant Admin creates a new namespace, Multikube automatically provisions it with a set of secure defaults, including:
    * A default **NetworkPolicy** that isolates the namespace from other namespaces.
    * A default admin **Role** with a broad set of permissions within that namespace.
    * A **RoleBinding** that assigns this admin Role to the user who created the namespace.
* **Day 2 (Self-Service Management):** After the namespace is created, the Tenant Admin can use Multikube's API and UI to perform "Day 2" operations, such as creating additional, more specific `NetworkPolicies` or managing `RoleBindings` for other users in their tenant.

#### **d. Separation of Concerns: Configurations vs. Workloads**

The data model and API are explicitly designed to differentiate between two types of resources, each with its own permission model:

* **Namespace Configurations:** These are resources that define the environment, security, and policies of a namespace (`ResourceQuota`, `NetworkPolicy`, `Role`, `RoleBinding`). These are stored in the `tenant_namespace_configurations` table and can **only be managed by a `TENANT_ADMIN`**.
* **Workloads:** These are the applications and their supporting resources that run inside the namespace (`Deployment`, `Service`, `StatefulSet`, `ConfigMap`, `Ingress`). These are stored in the `tenant_workloads` table and can be managed by **both `TENANT_ADMIN` and `TENANT_USER` roles**.

This separation ensures that regular users cannot change the fundamental security or resource boundaries of their environment, while still giving them the freedom to manage their own applications.

### 5. Technologies Used

* **Backend:** A robust and secure API built with **Java** and the **Spring Boot** framework.
* **Security:** Authentication and authorization are handled using **Spring Security** with a **JSON Web Token (JWT)**-based flow.
* **Database:** A **relational database** (e.g., PostgreSQL) with schema migrations managed by **Flyway**.
* **Kubernetes Integration:** The backend communicates with Kubernetes clusters using the official **Kubernetes Java client**.
* **Frontend:** The user interface is planned as a modern single-page application built with **Angular**.
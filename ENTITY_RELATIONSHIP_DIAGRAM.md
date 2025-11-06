# Entity Relationship Diagram

```
┌─────────────────┐
│    Customer     │
└────────┬────────┘
         │ 1
         │
         │ *
┌────────▼────────┐
│    Vehicle      │
│                 │
│  - v_Id         │
│  - regNo        │
│  - brandName    │
│  - model        │
│  - capacity     │
└────────┬────────┘
         │ 1
         │
         │ *
    ┌────▼───────────────────────────────┐
    │             Job                    │
    │                                    │
    │  - jobId                           │
    │  - type (SERVICE/PROJECT) ◄────────┼────── JobType enum
    │  - typeId                          │
    │  - status                          │
    │  - arrivingDate                    │
    │  - cost                            │
    │  - vehicle_id (FK)                 │
    │  - service_id (nullable)           │
    │  - project_id (nullable)           │
    └───┬──────────────────────────────────┘
        │
        │ *
        │
┌───────▼──────┐      ┌──────────────────┐
│   Service    │      │     Project      │────┐
│              │      │                  │    │
│  - serviceId │      │  - projectId     │    │ 1
│  - title     │      │  - title         │    │
│  - desc      │      │  - description   │    │ *
│  - category  │      │  - estimatedHours│    │
│  - imageUrl  │      │  - cost          │    │
│  - estHours  │      │  - status        │    │
│  - cost      │      └──────────────────┘    │
└──────────────┘                       ┌──────▼─────────┐
                                       │     Task       │
                                       │                │
                                       │  - tId         │
                                       │  - taskTitle   │
                                       │  - taskDesc    │
                                       │  - status      │
                                       │  - estimated   │
                                       │  - completedAt │
                                       │  - project_id  │
                                       └────────────────┘


┌──────────────────────────────────────────────┐
│         ManageAssignJob                      │
│                                              │
│  - manageAssignJob_Id                        │
│  - job_id (FK → Job)                         │
│  - employee_id (FK → Employee)               │
│  - manager_id (FK → Employee)                │
│  - createdAt                                 │
│  - updatedAt                                 │
└──────────────────────────────────────────────┘
            ▲                    ▲
            │                    │
       ┌────┴────┐          ┌───┴────┐
       │   Job   │          │Employee│
       │         │          │        │
       └─────────┘          └────────┘


═══════════════════════════════════════════════
Key Relationships:
═══════════════════════════════════════════════

1. Customer → Vehicle (1:*)
   - One customer can own multiple vehicles

2. Vehicle → Job (1:*)
   - One vehicle can have multiple jobs
   - Each job belongs to one vehicle
   - **Vehicle does NOT have direct relationship with Project**

3. Job → Service/Project (Polymorphic)
   - Job.type indicates SERVICE or PROJECT
   - Job.typeId references either Service.serviceId or Project.projectId
   - Job has optional FK to both Service and Project tables
   - **Vehicle connects to Projects THROUGH Jobs**

4. Service → Job (1:*)
   - One service template can have multiple job instances

5. Project → Job (1:*)
   - One project can have multiple jobs
   - Project does NOT have direct relationship with Vehicle
   - Project gets vehicle association through Job

6. Project → Task (1:*)
   - One project can have multiple tasks
   - Tasks belong ONLY to projects (not to service jobs)

7. ManageAssignJob (Junction/Assignment Table)
   - Links Job to Employee (staff assigned to work)
   - Links Job to Manager (who assigned it)
   - Tracks assignment metadata


═══════════════════════════════════════════════
Flow Diagram:
═══════════════════════════════════════════════

Customer → Vehicle → Job (type: SERVICE or PROJECT)
                      ├─→ if SERVICE → Service
                      └─→ if PROJECT → Project → Tasks

**Key Point: Vehicle has Jobs, not Projects directly**


═══════════════════════════════════════════════
Job Type Examples:
═══════════════════════════════════════════════

SERVICE JOB:
  type = "SERVICE"
  typeId = 5 (references Service.serviceId = 5)
  service_id = 5
  project_id = NULL
  vehicle_id = 10
  
  Example: Oil change, tire rotation, brake service
  Note: Service jobs do NOT have tasks
  Flow: Vehicle → Job(SERVICE) → Service

PROJECT JOB:
  type = "PROJECT"
  typeId = 12 (references Project.projectId = 12)
  service_id = NULL
  project_id = 12
  vehicle_id = 10
  
  Example: Custom modification, restoration work, major repairs
  Note: Project jobs can have multiple tasks through the Project entity
  Flow: Vehicle → Job(PROJECT) → Project → Tasks
  
**Important: A vehicle can have multiple project jobs, but it accesses projects 
through jobs, not directly. The vehicle-project relationship is INDIRECT via jobs.**
```

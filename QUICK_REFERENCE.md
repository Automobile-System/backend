# Corrected Entity Structure - Quick Reference

## The Rule:
**Customer → Vehicle → Jobs → (Service OR Project) → Tasks (only for Projects)**

## Simplified Relationships:

```
Customer
   └─── has many ───> Vehicle
                         └─── has many ───> Job (can be Service or Project type)
                                              ├─── references ───> Service (when type=SERVICE)
                                              └─── references ───> Project (when type=PROJECT)
                                                                      └─── has many ───> Task

Job
   ├─── belongs to ONE ───> Vehicle
   ├─── can reference ───> Service (when type = SERVICE)
   └─── can reference ───> Project (when type = PROJECT)
```

## Key Points:

### ✅ Correct Structure:
1. **Customer has Vehicles** ✓
2. **Vehicle has Jobs** ✓ (one vehicle has many jobs)
3. **Job belongs to one Vehicle** ✓
4. **Job can be Service or Project** ✓ (identified by type and typeId)
5. **Only Projects have Tasks** ✓ (not Jobs directly)
6. **Vehicle does NOT have direct relationship with Project** ✓

### ❌ What Changed:
1. ~~Job has many Vehicles~~ → **Job belongs to ONE Vehicle**
2. ~~Job has Tasks~~ → **Only Project has Tasks**
3. ~~Vehicle ↔ Job ManyToMany~~ → **Vehicle → Job OneToMany**
4. ~~Vehicle has Projects~~ → **Vehicle accesses Projects THROUGH Jobs**

## Database Tables:

### jobs
- job_id (PK)
- type (SERVICE or PROJECT)
- type_id (references service_id or project_id)
- **vehicle_id (FK → vehicle.v_id)** ← ONE vehicle per job
- service_id (FK → services.service_id, nullable)
- project_id (FK → projects.project_id, nullable)
- status
- arriving_date
- cost
- created_at
- updated_at

### projects
- project_id (PK)
- title
- description
- estimated_hours
- cost
- status
- created_at
- updated_at
- **No vehicle_id** ← Project does NOT directly reference vehicle

### tasks
- t_id (PK)
- task_title
- task_description
- status
- estimated_hours
- completed_at
- **project_id (FK → projects.project_id, NOT NULL)** ← Only projects have tasks
- created_at
- updated_at

### vehicle
- v_id (PK)
- registration_no
- brand_name
- model
- capacity
- customer_id (FK)
- created_by

### services
- service_id (PK)
- title
- description
- category
- image_url
- estimated_hours
- cost
- created_at
- updated_at

## Usage Flow:

### For a Service Job:
1. Customer brings vehicle for oil change (Service)
2. Create Job with type=SERVICE, typeId=serviceId, vehicle=customer's vehicle
3. Manager assigns Job to Employee via ManageAssignJob
4. Employee completes the job
5. **No tasks** - service jobs are atomic

### For a Project Job:
1. Customer brings vehicle for custom modification (Project)
2. Create Project with vehicle reference
3. Create Job with type=PROJECT, typeId=projectId, vehicle=customer's vehicle
4. **Create Tasks under the Project** (paint work, custom parts, testing, etc.)
5. Manager assigns Job to Employee via ManageAssignJob
6. Employee works on tasks within the project
7. Track progress through task completion

## Code Example:

```java
// Customer brings vehicle for both service and project

// 1. SERVICE JOB (Quick service - no tasks)
Service oilChange = serviceRepository.findById(1L).orElseThrow();
Vehicle customerVehicle = vehicleRepository.findById(vehicleId).orElseThrow();

Job serviceJob = Job.builder()
    .vehicle(customerVehicle)  // Belongs to this vehicle
    .status("PENDING")
    .cost(new BigDecimal("50.00"))
    .build();
serviceJob.setServiceType(oilChange);
jobRepository.save(serviceJob);

// 2. PROJECT JOB (Complex project - has tasks)
Vehicle sameVehicle = vehicleRepository.findById(vehicleId).orElseThrow();

// Create a project (no vehicle reference needed)
Project customPaint = Project.builder()
    .title("Custom Paint Job")
    .description("Full body custom paint")
    .status("PENDING")
    .cost(5000.0)
    .build();
projectRepository.save(customPaint);

Job projectJob = Job.builder()
    .vehicle(sameVehicle)  // Job references the vehicle
    .status("PENDING")
    .cost(new BigDecimal("5000.00"))
    .build();
projectJob.setProjectType(customPaint);
jobRepository.save(projectJob);

// Add tasks to PROJECT (not to job)
Task task1 = Task.builder()
    .taskTitle("Strip old paint")
    .status("PENDING")
    .estimatedHours(8.0)
    .build();
customPaint.addTask(task1);

Task task2 = Task.builder()
    .taskTitle("Apply primer")
    .status("PENDING")
    .estimatedHours(4.0)
    .build();
customPaint.addTask(task2);

projectRepository.save(customPaint);
```

This structure ensures:
- Simple services remain simple (no task overhead)
- Complex projects can be broken down into tasks
- **One vehicle can have multiple jobs** (both service and project types)
- **Vehicle accesses projects indirectly through jobs**
- Clear ownership: Job → Vehicle (many:1), Vehicle → Jobs (1:many)
- **No direct Vehicle-Project relationship** (cleaner separation)

# Entity Refactoring Summary

## Overview
Refactored the entity relationships to properly separate Service and Project entities while maintaining a unified Job system that can handle both types.

## Key Changes

### 1. New Enum: `JobType`
- **Location**: `com.TenX.Automobile.enums.JobType`
- **Values**: `SERVICE`, `PROJECT`
- **Purpose**: Identifies whether a job is for a service or a project

### 2. Updated `Job` Entity

#### New Fields:
- `type` (JobType): Indicates if the job is for a SERVICE or PROJECT
- `typeId` (Long): References either `serviceId` or `projectId` depending on the type
- `service` (Service): ManyToOne relationship to Service entity (when type is SERVICE)
- `project` (Project): ManyToOne relationship to Project entity (when type is PROJECT)
- `vehicle` (Vehicle): ManyToOne relationship - each job belongs to one vehicle

#### Removed:
- `vehicles` (ManyToMany relationship) - replaced with single vehicle
- `tasks` (OneToMany relationship) - tasks now belong only to projects

#### Helper Methods:
- `setServiceType(Service service)`: Sets the job as a SERVICE type
- `setProjectType(Project project)`: Sets the job as a PROJECT type
- `isServiceJob()`: Returns true if job type is SERVICE
- `isProjectJob()`: Returns true if job type is PROJECT

#### Database Schema:
```sql
ALTER TABLE jobs ADD COLUMN type VARCHAR(20) NOT NULL;
ALTER TABLE jobs ADD COLUMN type_id BIGINT NOT NULL;
ALTER TABLE jobs ADD COLUMN vehicle_id VARCHAR(255) NOT NULL;
ALTER TABLE jobs ADD COLUMN service_id BIGINT;
ALTER TABLE jobs ADD COLUMN project_id BIGINT;

ALTER TABLE jobs ADD CONSTRAINT fk_job_vehicle 
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(v_id);

ALTER TABLE jobs ADD CONSTRAINT fk_job_service 
    FOREIGN KEY (service_id) REFERENCES services(service_id);
    
ALTER TABLE jobs ADD CONSTRAINT fk_job_project 
    FOREIGN KEY (project_id) REFERENCES projects(project_id);

-- Drop the old many-to-many table if it exists
DROP TABLE IF EXISTS job_vehicle;
```

### 3. Updated `Project` Entity

#### Changes:
- Fixed primary key name from `jobId` to `projectId`
- Added `status` field for project status tracking
- **Removed `vehicle` relationship** - Projects do NOT directly reference vehicles
- Added `jobs` (List<Job>): OneToMany relationship - one project can have multiple jobs
- Added `tasks` (List<Task>): OneToMany relationship - **one project can have multiple tasks**
- Added helper methods: `addTask()`, `removeTask()`

#### Database Schema:
```sql
ALTER TABLE projects ADD COLUMN status VARCHAR(50);
-- No vehicle_id column in projects table
```

### 4. Updated `Service` Entity

#### Changes:
- Added `jobs` (List<Job>): OneToMany relationship - one service can have multiple jobs
- Added `AuditingEntityListener` for proper audit tracking

### 5. Updated `Vehicle` Entity

#### Changes:
- Added `projects` (List<Project>): OneToMany relationship - **one vehicle can have several projects**

## Relationship Summary

### Customer → Vehicle
- **One customer can have multiple vehicles** (OneToMany from Customer to Vehicle)

### Vehicle → Job
- **One vehicle can have multiple jobs** (OneToMany from Vehicle to Job)
- **Each job belongs to one vehicle** (ManyToOne from Job to Vehicle)
- **Vehicle does NOT have direct relationship with Project**

### Vehicle → Project (INDIRECT)
- **Vehicle accesses projects THROUGH Jobs**
- A vehicle can have multiple project jobs
- Get vehicle's projects by querying: `vehicle.getJobs()` filtered by `type=PROJECT`

### Job ↔ Service/Project
- **One service can have multiple jobs** (OneToMany from Service to Job)
- **One project can have multiple jobs** (OneToMany from Project to Job)
- **Each job belongs to either one service OR one project** (determined by `type` and `typeId`)

### Project → Task
- **One project can have multiple tasks** (OneToMany from Project to Task)
- **Each task belongs to one project** (ManyToOne from Task to Project)
- **Tasks only exist for projects, not for service jobs**

### Job Assignment (Manager → Employee)
- **Managed through `ManageAssignJob` entity**
- Manager (Employee with MANAGER role) assigns jobs to employees
- One job can be assigned to multiple employees
- Tracks who assigned the job and when

## Usage Examples

### Creating a Service Job:
```java
Service service = serviceRepository.findById(serviceId).orElseThrow();
Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow();

Job job = Job.builder()
    .vehicle(vehicle)
    .status("PENDING")
    .arrivingDate(LocalDateTime.now())
    .cost(new BigDecimal("500.00"))
    .build();
job.setServiceType(service);
jobRepository.save(job);
```

### Creating a Project Job with Tasks:
```java
// Create a project (no vehicle reference)
Project project = Project.builder()
    .title("Custom Engine Modification")
    .description("Turbocharger installation and tuning")
    .status("PENDING")
    .cost(5000.0)
    .build();
projectRepository.save(project);

// Create job and link to vehicle
Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow();

Job job = Job.builder()
    .vehicle(vehicle)  // Job connects project to vehicle
    .status("PENDING")
    .arrivingDate(LocalDateTime.now())
    .cost(new BigDecimal("5000.00"))
    .build();
job.setProjectType(project);
jobRepository.save(job);

// Add tasks to the project (not the job)
Task task = Task.builder()
    .taskTitle("Remove existing engine components")
    .taskDescription("Carefully remove stock parts")
    .status("PENDING")
    .estimatedHours(8.0)
    .build();
project.addTask(task);
projectRepository.save(project);
```

### Checking Job Type:
```java
Job job = jobRepository.findById(jobId).orElseThrow();
if (job.isServiceJob()) {
    Service service = job.getService();
    // Handle service job - no tasks
    System.out.println("Processing service: " + service.getTitle());
} else if (job.isProjectJob()) {
    Project project = job.getProject();
    // Handle project job - can have tasks
    System.out.println("Processing project: " + project.getTitle());
    List<Task> tasks = project.getTasks();
    // Process tasks...
}
```

### Assigning a Job to an Employee:
```java
ManageAssignJob assignment = ManageAssignJob.builder()
    .job(job)
    .employee(employee)
    .manager(manager)
    .build();
manageAssignJobRepository.save(assignment);
```

## Migration Considerations

1. **Data Migration**: Existing jobs need to be classified as either SERVICE or PROJECT
2. **Type Assignment**: Populate `type` and `typeId` fields for existing jobs
3. **Foreign Keys**: Add service_id or project_id based on the job type
4. **Project-Vehicle Relationship**: Assign vehicles to existing projects
5. **Application Code**: Update all services/controllers that create or manage jobs

## Benefits

1. ✅ Clear separation between Service and Project entities
2. ✅ Unified Job management system
3. ✅ Type-safe job classification
4. ✅ Proper vehicle-job relationship (1 vehicle = many jobs, 1 job = 1 vehicle)
5. ✅ **No direct Vehicle-Project coupling** - cleaner architecture
6. ✅ **Vehicle accesses projects indirectly through jobs** - better separation of concerns
7. ✅ **Tasks only exist in projects** - clearer responsibility
8. ✅ Maintains job assignment tracking through ManageAssignJob
9. ✅ Service jobs are simpler (no task management overhead)
10. ✅ Easy to query jobs by type and vehicle
11. ✅ Helper methods for type checking and setting

## Next Steps

1. Update repositories to handle new relationships
2. Update DTOs to include type and typeId fields
3. Update services to use helper methods for job creation
4. Update controllers to accept job type in requests
5. Create database migration scripts
6. Update existing data to match new schema
7. Update API documentation

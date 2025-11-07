# Job Type Usage Guide

## How the Type System Works

The `Job` entity uses a polymorphic approach where:
- **`type`** field indicates whether it's a SERVICE or PROJECT job
- **`typeId`** field stores the actual ID (either `serviceId` or `projectId`)
- **`service`** and **`project`** fields are the actual entity references

### Database Structure:
```sql
jobs table:
  - type (VARCHAR) → 'SERVICE' or 'PROJECT'
  - type_id (BIGINT) → references either service_id or project_id
  - service_id (BIGINT) → FK to services table (NULL if PROJECT)
  - project_id (BIGINT) → FK to projects table (NULL if SERVICE)
```

## Usage Examples

### 1. Creating a Service Job

```java
// Fetch the service template
Service oilChangeService = serviceRepository.findById(5L).orElseThrow();
Vehicle customerVehicle = vehicleRepository.findById(vehicleId).orElseThrow();

// Create the job
Job serviceJob = Job.builder()
    .vehicle(customerVehicle)
    .status("PENDING")
    .arrivingDate(LocalDateTime.now())
    .cost(new BigDecimal("75.00"))
    .build();

// Set as SERVICE type - this automatically sets:
// - type = SERVICE
// - typeId = 5 (the service ID)
// - service = oilChangeService entity
// - project = null
serviceJob.setServiceType(oilChangeService);

jobRepository.save(serviceJob);
```

**Result in Database:**
```
job_id: 101
type: SERVICE
type_id: 5
service_id: 5
project_id: NULL
vehicle_id: 10
status: PENDING
```

### 2. Creating a Project Job

```java
// Create or fetch the project
Project customModification = Project.builder()
    .title("Engine Upgrade")
    .description("Install turbocharger and performance parts")
    .vehicle(customerVehicle)
    .status("PENDING")
    .cost(5000.0)
    .build();
projectRepository.save(customModification); // project_id = 12

// Create the job
Job projectJob = Job.builder()
    .vehicle(customerVehicle)
    .status("PENDING")
    .arrivingDate(LocalDateTime.now())
    .cost(new BigDecimal("5000.00"))
    .build();

// Set as PROJECT type - this automatically sets:
// - type = PROJECT
// - typeId = 12 (the project ID)
// - project = customModification entity
// - service = null
projectJob.setProjectType(customModification);

jobRepository.save(projectJob);
```

**Result in Database:**
```
job_id: 102
type: PROJECT
type_id: 12
service_id: NULL
project_id: 12
vehicle_id: 10
status: IN_PROGRESS
```

### 3. Retrieving and Checking Job Type

```java
Job job = jobRepository.findById(jobId).orElseThrow();

// Method 1: Check the type field
if (job.getType() == JobType.SERVICE) {
    System.out.println("This is a service job");
}

// Method 2: Use helper methods
if (job.isServiceJob()) {
    Long serviceId = job.getTypeId();
    Service service = job.getService(); // Lazy loaded
    System.out.println("Service: " + service.getTitle());
    System.out.println("Service ID from typeId: " + serviceId);
}

if (job.isProjectJob()) {
    Long projectId = job.getTypeId();
    Project project = job.getProject(); // Lazy loaded
    System.out.println("Project: " + project.getTitle());
    System.out.println("Project ID from typeId: " + projectId);
    
    // Projects can have tasks
    List<Task> tasks = project.getTasks();
    System.out.println("Number of tasks: " + tasks.size());
}

// Method 3: Get the related entity dynamically
Object relatedEntity = job.getRelatedEntity();
if (relatedEntity instanceof Service) {
    Service service = (Service) relatedEntity;
    // Work with service
} else if (relatedEntity instanceof Project) {
    Project project = (Project) relatedEntity;
    // Work with project
}
```

### 4. Querying Jobs by Type

```java
// In your JobRepository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // Find all service jobs
    List<Job> findByType(JobType type);
    
    // Find job by type and typeId
    Optional<Job> findByTypeAndTypeId(JobType type, Long typeId);
    
    // Find all jobs for a specific service
    List<Job> findByTypeAndServiceId(JobType type, Long serviceId);
    
    // Find all jobs for a specific project
    List<Job> findByTypeAndProjectId(JobType type, Long projectId);
    
    // Find all jobs for a vehicle
    List<Job> findByVehicleId(UUID vehicleId);
}

// Usage:
List<Job> allServiceJobs = jobRepository.findByType(JobType.SERVICE);
List<Job> allProjectJobs = jobRepository.findByType(JobType.PROJECT);

// Find specific job for a service
Optional<Job> serviceJob = jobRepository.findByTypeAndTypeId(JobType.SERVICE, 5L);

// Find all jobs related to a specific service template
List<Job> oilChangeJobs = jobRepository.findByTypeAndServiceId(JobType.SERVICE, 5L);
```

### 5. Using typeId to Fetch Related Entity

```java
// When you only have the job
Job job = jobRepository.findById(jobId).orElseThrow();

// Use typeId to query the correct repository
if (job.isServiceJob()) {
    Long serviceId = job.getTypeId();
    Service service = serviceRepository.findById(serviceId)
        .orElseThrow(() -> new RuntimeException("Service not found"));
    
    System.out.println("Service: " + service.getTitle());
    System.out.println("Category: " + service.getCategory());
    System.out.println("Cost: " + service.getCost());
}

if (job.isProjectJob()) {
    Long projectId = job.getTypeId();
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new RuntimeException("Project not found"));
    
    System.out.println("Project: " + project.getTitle());
    System.out.println("Description: " + project.getDescription());
    
    // Get all tasks for this project
    List<Task> tasks = project.getTasks();
    for (Task task : tasks) {
        System.out.println("Task: " + task.getTaskTitle() + " - Status: " + task.getStatus());
    }
}
```

### 6. Service Layer Example

```java
@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    public JobDTO getJobDetails(Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        JobDTO dto = new JobDTO();
        dto.setJobId(job.getJobId());
        dto.setType(job.getType().name());
        dto.setStatus(job.getStatus());
        dto.setVehicleId(job.getVehicle().getV_Id());
        
        // Use typeId to get the correct entity
        if (job.isServiceJob()) {
            Service service = serviceRepository.findById(job.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
            dto.setServiceTitle(service.getTitle());
            dto.setServiceCategory(service.getCategory());
        } else if (job.isProjectJob()) {
            Project project = projectRepository.findById(job.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            dto.setProjectTitle(project.getTitle());
            dto.setProjectDescription(project.getDescription());
            dto.setTaskCount(project.getTasks().size());
        }
        
        return dto;
    }
    
    public Job createServiceJob(Long serviceId, UUID vehicleId, BigDecimal cost) {
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        
        Job job = Job.builder()
            .vehicle(vehicle)
            .status("PENDING")
            .arrivingDate(LocalDateTime.now())
            .cost(cost)
            .build();
        
        job.setServiceType(service); // Sets type, typeId, and service reference
        
        return jobRepository.save(job);
    }
    
    public Job createProjectJob(Long projectId, UUID vehicleId, BigDecimal cost) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        
        Job job = Job.builder()
            .vehicle(vehicle)
            .status("PENDING")
            .arrivingDate(LocalDateTime.now())
            .cost(cost)
            .build();
        
        job.setProjectType(project); // Sets type, typeId, and project reference
        
        return jobRepository.save(job);
    }
}
```

## Key Points

### ✅ Benefits of Using typeId:

1. **Single field lookup**: Check `type` to know which entity to query, use `typeId` to fetch it
2. **Database efficiency**: Can query by typeId without joining Service or Project tables
3. **Flexibility**: Easy to add statistics or reports based on type and typeId
4. **Type safety**: Enum ensures only valid types (SERVICE or PROJECT)

### ⚠️ Important Rules:

1. **Always use helper methods**: Use `setServiceType()` or `setProjectType()` to ensure consistency
2. **typeId must match**: When type=SERVICE, typeId must equal service_id; when type=PROJECT, typeId must equal project_id
3. **Null constraints**: 
   - If type=SERVICE → service_id has value, project_id is NULL
   - If type=PROJECT → project_id has value, service_id is NULL
4. **Tasks only in Projects**: Only PROJECT jobs have tasks (through the Project entity)

### 📊 Querying Pattern:

```java
// CORRECT: Use typeId for efficient lookup
Job job = jobRepository.findById(jobId).orElseThrow();
if (job.isServiceJob()) {
    Service service = serviceRepository.findById(job.getTypeId()).orElseThrow();
    // Use service
}

// ALSO CORRECT: Use the lazy-loaded relationship
if (job.getService() != null) {
    Service service = job.getService(); // Triggers lazy load
    // Use service
}

// BEST PRACTICE: Combine both for efficiency
if (job.isServiceJob()) {
    // Option 1: Use typeId (no extra query if service not needed immediately)
    Long serviceId = job.getTypeId();
    
    // Option 2: Access entity directly (triggers lazy load)
    Service service = job.getService();
}
```

This approach gives you the best of both worlds: efficient lookups via typeId and convenient entity relationships!

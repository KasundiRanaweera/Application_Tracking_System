# Test script for "View Recruiter's Jobs (Filter by Search)" feature

$API_URL = "http://localhost:8080/api"

Write-Output "=== Testing View Recruiter's Jobs (Filter by Search) ==="
Write-Output ""

# Step 1: Register a recruiter
Write-Output "[1] Registering a recruiter account..."
$registerResponse = Invoke-RestMethod -Uri "$API_URL/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body @{
        name = "John Recruiter"
        email = "recruiter@test.com"
        password = "Password123"
        role = "RECRUITER"
    } | ConvertTo-Json

Write-Output "Register Response: $registerResponse"
Write-Output ""

# Step 2: Login to get token
Write-Output "[2] Logging in to get authentication token..."
$loginResponse = Invoke-RestMethod -Uri "$API_URL/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body @{
        email = "recruiter@test.com"
        password = "Password123"
    } | ConvertTo-Json

Write-Output "Login Response: $loginResponse"

# Extract token from response
$loginData = $loginResponse | ConvertFrom-Json
$token = $loginData.token
Write-Output "Token: $token"
Write-Output ""

# Create headers with token
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 3: Create several jobs
Write-Output "[3] Creating test jobs..."
$jobTitles = @(
    "Senior Java Developer",
    "React Frontend Developer",
    "Java Backend Engineer",
    "DevOps Engineer",
    "Java Full Stack Developer"
)

foreach ($title in $jobTitles) {
    $jobData = @{
        title = $title
        description = "Test job description for $title"
        location = "New York"
        workMode = "REMOTE"
        employmentType = "FULL_TIME"
        salaryMin = 80000
        salaryMax = 120000
        requiredSkills = "Java, Spring, REST APIs"
        closingDate = "2026-12-31"
    } | ConvertTo-Json

    try {
        $createJobResponse = Invoke-RestMethod -Uri "$API_URL/jobs" `
            -Method Post `
            -Headers $headers `
            -Body $jobData
        Write-Output "  ✓ Created job: $title"
    } catch {
        Write-Output "  ✗ Failed to create job: $title"
        Write-Output "  Error: $_"
    }
}
Write-Output ""

# Step 4: Test search filter - search for "Java"
Write-Output "[4] Testing search filter for 'Java'..."
try {
    $searchResponse = Invoke-RestMethod -Uri "$API_URL/jobs/manage/all?search=Java&page=0&size=10" `
        -Method Get `
        -Headers $headers

    Write-Output "  Found $(($searchResponse.content).Count) jobs matching 'Java'"
    foreach ($job in $searchResponse.content) {
        Write-Output "    - $($job.title)"
    }
} catch {
    Write-Output "  ✗ Search filter test failed"
    Write-Output "  Error: $_"
}
Write-Output ""

# Step 5: Test search filter - search for "React"
Write-Output "[5] Testing search filter for 'React'..."
try {
    $searchResponse = Invoke-RestMethod -Uri "$API_URL/jobs/manage/all?search=React&page=0&size=10" `
        -Method Get `
        -Headers $headers

    Write-Output "  Found $(($searchResponse.content).Count) jobs matching 'React'"
    foreach ($job in $searchResponse.content) {
        Write-Output "    - $($job.title)"
    }
} catch {
    Write-Output "  ✗ Search filter test failed"
    Write-Output "  Error: $_"
}
Write-Output ""

# Step 6: Test without search filter
Write-Output "[6] Testing list all recruiter jobs (no filter)..."
try {
    $allJobsResponse = Invoke-RestMethod -Uri "$API_URL/jobs/manage/all?page=0&size=10" `
        -Method Get `
        -Headers $headers

    Write-Output "  Found $(($allJobsResponse.content).Count) total jobs"
    foreach ($job in $allJobsResponse.content) {
        Write-Output "    - $($job.title)"
    }
} catch {
    Write-Output "  ✗ List all jobs test failed"
    Write-Output "  Error: $_"
}

Write-Output ""
Write-Output "=== Test Complete ==="


# Test Register API
$body = @{
    email = "newuser@example.com"
    password = "Test1234"
    firstName = "New"
    lastName = "User"
    role = "CLIENT_USER"
} | ConvertTo-Json

Write-Host "Testing Register API..."
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json"
$response | ConvertTo-Json -Depth 5


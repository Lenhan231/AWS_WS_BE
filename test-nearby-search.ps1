# Test Nearby Search API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST 1: Search Gyms within 5km radius" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Example: Hanoi coordinates (21.0285, 105.8542)
$uri1 = "http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=5&type=gym"

try {
    $response1 = Invoke-RestMethod -Uri $uri1 -Method GET -ContentType "application/json"
    Write-Host "✓ Success!" -ForegroundColor Green
    $response1 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 2: Search PTs within 10km radius" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$uri2 = "http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=10&type=pt"

try {
    $response2 = Invoke-RestMethod -Uri $uri2 -Method GET -ContentType "application/json"
    Write-Host "✓ Success!" -ForegroundColor Green
    $response2 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 3: Search ALL (Gyms + PTs) within 3km" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$uri3 = "http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=3"

try {
    $response3 = Invoke-RestMethod -Uri $uri3 -Method GET -ContentType "application/json"
    Write-Host "✓ Success!" -ForegroundColor Green
    $response3 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 4: Search with pagination" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$uri4 = "http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=5&page=0&size=10"

try {
    $response4 = Invoke-RestMethod -Uri $uri4 -Method GET -ContentType "application/json"
    Write-Host "✓ Success!" -ForegroundColor Green
    Write-Host "Total results: $($response4.pagination.total)" -ForegroundColor Cyan
    Write-Host "Page: $($response4.pagination.page) / Total Pages: $($response4.pagination.totalPages)" -ForegroundColor Cyan
    $response4 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 5: Invalid parameters (should fail)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$uri5 = "http://localhost:8080/api/v1/search/nearby?lat=200&lon=105.8542&radius=5"

try {
    $response5 = Invoke-RestMethod -Uri $uri5 -Method GET -ContentType "application/json"
    Write-Host "✗ Should have failed with invalid coordinates!" -ForegroundColor Red
} catch {
    Write-Host "✓ Correctly rejected invalid coordinates!" -ForegroundColor Green
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}


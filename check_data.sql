-- Check payment data
SELECT 
    DATE_TRUNC('month', created_at) as month,
    COUNT(*) as payment_count,
    SUM(payment_amount) as total_revenue
FROM payment
WHERE created_at >= NOW() - INTERVAL '6 months'
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month DESC;

-- Check job costs
SELECT 
    DATE_TRUNC('month', created_at) as month,
    COUNT(*) as job_count,
    SUM(cost) as total_cost
FROM jobs
WHERE created_at >= NOW() - INTERVAL '6 months'
  AND cost IS NOT NULL
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month DESC;

-- Check all payments (any date)
SELECT COUNT(*) as total_payments, SUM(payment_amount) as total_amount
FROM payment;

-- Check all jobs with costs
SELECT COUNT(*) as total_jobs, SUM(cost) as total_cost
FROM jobs
WHERE cost IS NOT NULL;

-- Check date range of payments
SELECT MIN(created_at) as earliest_payment, MAX(created_at) as latest_payment
FROM payment;

-- Check date range of jobs
SELECT MIN(created_at) as earliest_job, MAX(created_at) as latest_job
FROM jobs;

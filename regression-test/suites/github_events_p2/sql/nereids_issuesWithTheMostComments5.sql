SET enable_nereids_planner=TRUE;
SET enable_fallback_to_original_planner=FALSE;
SELECT
    repo_name,
    number,
    count() AS comments
FROM github_events
WHERE event_type = 'IssueCommentEvent' AND (action = 'created') AND (number > 10)
GROUP BY repo_name, number
ORDER BY comments DESC, repo_name, number
LIMIT 50

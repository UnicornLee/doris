SET enable_nereids_planner=TRUE;
SET enable_fallback_to_original_planner=FALSE;
-- SELECT
--     repo_name,
--     number,
--     count() AS comments,
--     count(distinct actor_login) AS authors
-- FROM github_events
-- WHERE event_type = 'IssueCommentEvent' AND (action = 'created') AND (number > 10)
-- GROUP BY repo_name, number
-- HAVING authors >= 10
-- ORDER BY comments DESC
-- LIMIT 50

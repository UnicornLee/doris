SET enable_nereids_planner=TRUE;
SET enable_fallback_to_original_planner=FALSE;
-- SELECT
--     repo_name,
--     count(distinct actor_login) AS u,
--     sum(star) AS stars
-- FROM
-- (
--     SELECT
--         repo_name,
--         CASE WHEN event_type = 'PushEvent' THEN actor_login ELSE NULL END AS actor_login,
--         CASE WHEN event_type = 'WatchEvent' THEN 1 ELSE 0 END AS star
--     FROM github_events WHERE event_type IN ('PushEvent', 'WatchEvent') AND repo_name != '/'
-- ) t
-- GROUP BY repo_name ORDER BY u DESC LIMIT 50

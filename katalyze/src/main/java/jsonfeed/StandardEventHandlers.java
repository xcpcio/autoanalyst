package jsonfeed;

import io.EntityOperation;
import model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardEventHandlers {
    static private Logger log = LogManager.getLogger(StandardEventHandlers.class);

    Map<String, JsonEventHandler> handlers = new ConcurrentHashMap<>();
    Map<String, JsonEventHandler> unknownEvents = new ConcurrentHashMap<>();

    public class NullEventHandler implements JsonEventHandler {

        @Override
        public void process(Contest contest, JsonEvent src) throws Exception {
            // Do nothing.
        }
    }


    public StandardEventHandlers() {

        handlers.put("teams", (contest, src) -> {
            String organizationId = src.getString("organization_id");
            Organization org = (organizationId != null) ? (contest.getOrganization(organizationId)) : null;
            String[] group_ids = src.getStringArray("group_ids");
            ArrayList<Group> groups = new ArrayList<>();
            for (String group_id : group_ids) {
                Group group = contest.getGroup(group_id);
                if (group != null) {
                    groups.add(group);
                }
            }

            String[] desktops = src.getUrlArray("desktop");
            String[] webcams = src.getUrlArray("webcam");
            boolean hidden = src.tryGetBoolean("hidden", false);

            contest.registerTeam(src.getString("id"), src.getString("name"), org, groups.toArray(new Group[groups.size()]),
                    webcams, desktops, hidden, src.getOp());
        });

        JsonEventHandler contestEventHandler = (contest, src)  -> {
            ContestProperties properties = ContestProperties.fromJSON(src.getRawData());
            contest.init(properties);
        };

        handlers.put("contests", contestEventHandler);
        handlers.put("contest", contestEventHandler); // Add compatibility for 2022.07 revision

        handlers.put("state", (contest, src) -> {
            ContestState newState = new ContestState(
                    src.getTimestamp("started"),
                    src.getTimestamp("ended"),
                    src.getTimestamp("frozen"),
                    src.getTimestamp("finalized"),
                    src.getTimestamp("thawed")
            );
            contest.updateState(newState);
        });

        handlers.put("languages", (contest, src) -> contest.addLanguage(new Language(src.getString("id"), src.getString("name"))));
        handlers.put("organizations", (contest,src) -> {
	    if (src.getOp() == EntityOperation.DELETE) return; /// HACK
            contest.addOrganization(
                    new Organization(src.getString("id"), src.getString("name"), src.getStringOrNull("formal_name"), src.getStringOrNull("country"), src.getStringOrNull("twitter_hashtag"))
            );
        });
        handlers.put("problems", (contest, src) -> contest.addProblem(
                new Problem(src.getString("id"), src.getString("name"), src.getString("label"), src.getStringOrNull("rgb"))));
        handlers.put("judgement-types", (contest, src) -> {
            JudgementType newJudgementType = new JudgementType(src.getString("id"), src.getBoolean("solved"), src.getBoolean("penalty"));
            contest.addJudgementType(newJudgementType);
        });
        handlers.put("submissions", (contest, src) -> {
            String submissionId = src.getString("id");
            String teamId = src.getString("team_id");
            String problemId = src.getString("problem_id");
            Problem problem = contest.getProblem(problemId);
            Team team = contest.getTeam(teamId);

            long contestTimeMilliseconds = (long) (src.getTimespan("contest_time"));
            Instant timestamp = OffsetDateTime.parse(src.getString("time")).toInstant();
            contest.updateTime(contestTimeMilliseconds);
            team.freshSubmission(new InitialSubmission(submissionId, team, problem, src.getString("language_id"),
                    contestTimeMilliseconds, timestamp));
        });
        handlers.put("groups", (contest, src) -> {
            String groupId = src.getString("id");
            String groupName = src.getString("name");
            boolean hidden = src.tryGetBoolean("hidden", false);
            contest.registerGroup(groupId, groupName, hidden);
        });

        handlers.put("team-members", (contest, src) -> {
            String teamMemberId = src.getString("id");
            String teamId = src.getString("team_id");
            String firstName = src.getString("first_name");
            String lastName = src.getString("last_name");
            String role = src.getString("role");

            TeamMember newMember = new TeamMember(teamMemberId, teamId, firstName +" "+lastName, role);
            contest.registerTeamMember(newMember, src.getOp());
        });

        handlers.put("judgements", (contest, src) -> {

            Analyzer analyzer = contest.getAnalyzer();

            String judgementId = src.getString("id");
            String submissionId = src.getString("submission_id");

            InitialSubmission submission = analyzer.submissionById(submissionId);

            if (submission == null) {
                log.warn(String.format("Judgment '%s' that references an non-existing submission '%s'",judgementId, submissionId));
            }

            String verdictId = src.getStringOrNull("judgement_type_id");
            JudgementType verdict = contest.getJudgementType(verdictId);
            if (verdict != null) {
                if (submission == null) {
                    log.error(String.format("Lost judgement '%s' due to missing judgement %s", judgementId, submissionId));
                } else {
                    long judgementContestTime = src.getTimespan("end_contest_time");
                    contest.updateTime(judgementContestTime);
                    if (judgementContestTime == -1) {
                        // Apparently server didn't send a time. For now, use the submission time
                        judgementContestTime = submission.contestTimeMilliseconds;
                    }
                    // Only submit if there is a verdict.
                    submission.team.submit(submission, judgementContestTime, judgementId, submission.problem, verdict.getId(), verdict.isAccepted(), verdict.hasPenalty());
                }
            }

        });
    }

    public JsonEventHandler getHandlerFor(JsonEvent src) {
        String srcType = src.getType();
        JsonEventHandler handler = handlers.get(srcType);
        if (handler == null) {
            handler = unknownEvents.get(srcType);
            if (handler == null) {
                handler = new NullEventHandler();
                log.warn(String.format("Encountered unexpected event type '%s'. This and subsequent messages of the same type will be ignored", srcType));
                unknownEvents.put(srcType, handler);
            }
        }

        return handler;

    }





}

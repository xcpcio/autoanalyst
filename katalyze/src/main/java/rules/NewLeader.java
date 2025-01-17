package rules;

import model.EventImportance;
import model.LoggableEvent;
import model.Score;
import model.Judgement;
import model.Team;

public class NewLeader extends StateComparingRuleBase implements StandingsUpdatedEvent {

    private int breakingPrioRanks;
    private int normalPrioRanks;
	
	public NewLeader(int breakingPrioRanks, int normalPrioRanks) {
        this.breakingPrioRanks = breakingPrioRanks;
        this.normalPrioRanks = normalPrioRanks;
    }
	
	private EventImportance fromRank(int rank) {
		if (rank <= breakingPrioRanks) {
			return EventImportance.Breaking;
		}
		if (rank <= normalPrioRanks) {
			return EventImportance.Normal;
		}
		return EventImportance.Whatever;
	}
	
	private String problemsAsText(int nProblems) {
		if (nProblems == 1) {
			return "1 problem";
		} else {
			return String.format("%d problems", nProblems);
		}
	}


	
	@Override
	public void onStandingsUpdated(StandingsTransition transition) {
		
		Judgement submission = transition.judgement;

		Team team = submission.getTeam();

		Score scoreBefore = transition.before.scoreOf(team);
		Score scoreAfter = transition.after.scoreOf(team);
		if (scoreBefore.isSolved(submission.getProblem())==scoreAfter.isSolved(submission.getProblem())) {
			// No problem solution status was changed.
			return;
		}
				
		
		int rankBefore = transition.before.rankOf(team);
		int rankAfter = transition.after.rankOf(team);

		int solvedProblemCount = scoreAfter.solvedProblemCount();
		EventImportance importance = fromRank(rankAfter);

		String solvedProblemsText = problemsAsText(solvedProblemCount);
		
		LoggableEvent event = null;
		

		if (solvedProblemCount == 1) {
			event = transition.createEvent(String.format("{team} solves its first problem: {problem}"), importance);
		} else if (rankAfter == 1 && rankBefore == 1) {
			event = transition.createEvent(String.format("{team} extends its lead by solving {problem}. It has now solved %s", solvedProblemsText),  importance);
		} else if (rankAfter < rankBefore) {
			if (rankAfter == 1) {
				event = transition.createEvent(String.format("{team} now leads the competition after solving {problem}. It has now solved %s", solvedProblemsText),  importance);
			} else {
				event = transition.createEvent(String.format("{team} solves {problem}. It has solved %s and is %s", solvedProblemsText, rankString(rankAfter, rankBefore)), importance);
			}
		} else if (rankAfter == rankBefore) {
			event = transition.createEvent(String.format("{team} solves {problem}. It is %s and has %s solved", rankString(rankAfter, rankBefore), solvedProblemsText), importance);
		} else {
			if (rankAfter > rankBefore) {
				event = transition.createEvent(String.format("{team} is now %s after {problem} was rejudged.", rankString(rankAfter, rankBefore)), EventImportance.Breaking);
			} else {
				logger.error("Wierd! why did we end up here?");
			}

		}

		notify(event);
	}

	public String toString() {
		return String.format("NewLeader (breaking <= %d, normal <= %d)", breakingPrioRanks, normalPrioRanks);
	}

}

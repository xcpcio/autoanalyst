package rules;

import model.Analyzer;
import model.EventImportance;
import model.LoggableEvent;
import model.Standings;
import model.Judgement;

public class StandingsTransition {
	public final Standings before;
	public final Standings after;
	public final Judgement submission;
	private final Analyzer analyzer;
	
	public StandingsTransition(Analyzer analyzer, Standings before, Standings after, Judgement submission) {
		this.analyzer = analyzer;
		this.before = before;
		this.after = after;
		this.submission = submission;
	}
	
	public LoggableEvent createEvent(String message, EventImportance importance) {
		return analyzer.createEvent(submission.getInitialSubmission(), submission.getJudgementTimeMillis(),
				message, importance);
	}
	
	

}

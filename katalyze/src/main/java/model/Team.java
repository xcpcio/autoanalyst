package model;

public class Team {
	private final String name;
	private final String teamId;
	private final TeamProgress progress;
	private final Contest contest;
	private final String shortName;


	public Team(Contest contest, String teamId, String name, String shortName) {
		this.contest = contest;
		this.teamId = teamId;
		this.name = name;
		this.shortName = shortName;
		this.progress = new TeamProgress(this);
	}
	
	public String getTeamId() {
		return teamId;
	}
	
	@Override
	public String toString() {
		return String.format("#t%s", teamId);
	}
	
	public Submission submit(InitialSubmission initialSubmission, String judgementId, Problem problem, int minutesFromStart, String judgement, Boolean accepted, Boolean penalty) {
		
		Analyzer analyzer = contest.getAnalyzer();
		TestCaseExecution judgeOutcome = analyzer.getFailureInfo(initialSubmission);
		
		Submission newSubmission = new Submission(initialSubmission, judgementId, this, minutesFromStart, problem, judgement, accepted, penalty, judgeOutcome);
		contest.processSubmission(newSubmission);
		
		return newSubmission;
	}


	public void registerJudgement(Submission judgement) {
		progress.register(judgement);
	}
	
	public void freshSubmission(InitialSubmission submission) {
		progress.registerInitialSubmission(submission);
		contest.getAnalyzer().freshSubmission(submission);
	}
	
	public Score getCurrentScore() {
		return progress.calculateScore();
	}
	
	public Contest getContest() {
		return this.contest;
	}
	
	public String languageFor(Problem problem) {
		return progress.languageFor(problem);
	}
	
	public String getMainLanguage() {
		return progress.getMainLanguage();
	}

	public String getName() {
		return this.name;
	}

	public String getShortName() {
		return this.shortName;
	}
}
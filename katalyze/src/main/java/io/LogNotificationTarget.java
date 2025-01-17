package io;

import model.EventImportance;
import model.InitialSubmission;
import model.LoggableEvent;
import model.NotificationTarget;
import org.apache.log4j.Logger;

public class LogNotificationTarget implements NotificationTarget {
	
	static Logger logger = Logger.getLogger(LogNotificationTarget.class);
	final boolean useHashTags;
	
	public LogNotificationTarget(boolean useHashTags) {
		this.useHashTags = useHashTags;
	}

	@Override
	public void notify(LoggableEvent event) {
		String messageText = useHashTags ? event.icatMessage : event.message;

		InitialSubmission submission = event.submission;
		String submissionId = (submission != null) ? submission.id : "-";

		String fullMessage = String.format("[%d][%s] %s", event.contestTimeMinutes(), submissionId, messageText);
		if (event.importance.ordinal() <= EventImportance.Normal.ordinal()) {
			logger.info(fullMessage);
		} else {
			logger.debug(fullMessage);
		}
	}
	

}

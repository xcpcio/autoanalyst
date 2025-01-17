package messageHandlers;

import model.Contest;
import legacyfeed.SimpleMessage;

public abstract class SingleMessageHandler implements MessageHandler {
	String tag;
	Contest contest;
	
	public SingleMessageHandler(String tag) {
		this.tag = tag;
	}
	
	/* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#connectTo(model.Contest)
	 */
	@Override
	public void connectTo(Contest contest) {
		this.contest = contest;
	}
	
	/* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#supports(legacyfeed.SimpleMessage)
	 */
	@Override
	public Boolean supports(SimpleMessage message) {
		return tag.equals(message.getName());
	}
	
	/* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#process(legacyfeed.SimpleMessage)
	 */
	@Override
	public abstract void process(SimpleMessage message);
	
	public void error(String errorMessage) {
		System.out.println(errorMessage);
		
	}
	
	public void close() {
		// do nothing
	}

}

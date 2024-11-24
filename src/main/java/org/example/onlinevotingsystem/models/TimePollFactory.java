package org.example.onlinevotingsystem.models;

public class TimePollFactory implements PollFactory {
	@Override
	public Poll createPoll(PollRequest request) {

		TimePoll timePoll = new TimePoll();
		timePoll.setTitle(request.getTitle());
		timePoll.setOptions(request.getOptions());

		timePoll.setCategory(request.getCategory());
		timePoll.setStartTime(request.getStartTime());
		timePoll.setEndTime(request.getEndTime());
		timePoll.setVotingStrategy(request.getVotingStrategy());

		return timePoll;
	}

}

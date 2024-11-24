package org.example.onlinevotingsystem.models;

public class OpenPollFactory implements PollFactory {
	@Override
	public Poll createPoll(PollRequest request) {

		OpenPoll openPoll = new OpenPoll();
		openPoll.setTitle(request.getTitle());
		openPoll.setOptions(request.getOptions());
		openPoll.setCategory(request.getCategory());
		openPoll.setAdmin(request.getAdmin());
		openPoll.setVotingStrategy(request.getVotingStrategy());
		return openPoll;
	}

	 
}
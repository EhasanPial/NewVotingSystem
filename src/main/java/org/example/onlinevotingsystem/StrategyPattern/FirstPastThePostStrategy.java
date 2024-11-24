package org.example.onlinevotingsystem.StrategyPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.onlinevotingsystem.models.Option;
import org.example.onlinevotingsystem.models.Poll;

public class FirstPastThePostStrategy implements VotingStrategy {
	@Override
	public PollResult calculateResults(Poll poll) {

		// get the winner
 		List<Option> options = poll.getOptions();
		Option winnerOpt = null;
		for (Option option : options) {
			if (winnerOpt == null) {
				winnerOpt = option;
			} else if (option.getVoteCount() > winnerOpt.getVoteCount()) {
				winnerOpt = option;
			}
		}

		String winner = winnerOpt.getTitle();

		// get the vote counts for each option
		Map<String, Integer> voteCounts = new HashMap<>();
		for (Option option : options) {
			voteCounts.put(option.getTitle(), option.getVoteCount());

		}
		
		// get the percentages for each option
		Map<String, Double> percentages = new HashMap<>();
		 
		for (Option option : options) {
			double percentageCalculate = (option.getVoteCount() * 100.0) / poll.getTotalVote();
			// set the percentage with 2 decimal places
			percentageCalculate = Math.round(percentageCalculate * 100.0) / 100.0;
			percentages.put(option.getTitle(), percentageCalculate);
			
		}

		PollResult pollResult = new PollResult();
		pollResult.setWinner(winner);
		pollResult.setVoteCounts(voteCounts);
		pollResult.setVotePercentages(percentages);
		pollResult.setTotalVotes(poll.getTotalVote());
		pollResult.setPublished(false);
		return pollResult;	}
}

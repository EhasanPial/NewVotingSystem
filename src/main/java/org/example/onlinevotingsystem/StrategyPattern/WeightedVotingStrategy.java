package org.example.onlinevotingsystem.StrategyPattern;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.onlinevotingsystem.models.Option;
import org.example.onlinevotingsystem.models.Poll;

public class WeightedVotingStrategy implements VotingStrategy {
	private Map<String, Double> optionWeights;

	public WeightedVotingStrategy(Map<String, Double> weights) {
		this.optionWeights = weights;
	}

	public PollResult calculateResults(Poll poll) {
		if (optionWeights == null) {
			optionWeights = new HashMap<>();
		}
		List<Option> options = poll.getOptions();
		if (poll.getWeight() != null) {
			String[] weightTiles = poll.getWeight().split("-");

			// convert to double array
			double[] weights = new double[weightTiles.length];
			for (int i = 0; i < weightTiles.length; i++) {
				weights[i] = Double.parseDouble(weightTiles[i]);
			}

			double[] normalizedWeights = normalizeWeights(weights);
			for (int i = 0; i < normalizedWeights.length; i++) {
				System.out.println(options.get(i).getTitle() + " : " + normalizedWeights);
				optionWeights.put(options.get(i).getTitle(), normalizedWeights[i]);
			}

		}

		if (options == null || options.isEmpty()) {
			throw new IllegalArgumentException("Poll must have options");
		}

		// Calculate weighted votes and find winner
		Option winnerOpt = null;
		double maxWeightedVotes = 0;
		Map<String, Double> weightedVotesMap = new HashMap<>();

		// First pass: calculate weighted votes for each option
		for (Option option : options) {

			double weight = optionWeights.getOrDefault(option.getTitle(), 1.0);
			if (weight <= 0) {
				throw new IllegalArgumentException("Weight must be positive for option: " + option.getTitle());
			}

			double weightedVotes = option.getVoteCount() * weight;
			weightedVotesMap.put(option.getTitle(), weightedVotes);

			if (winnerOpt == null || weightedVotes > maxWeightedVotes) {
				winnerOpt = option;
				maxWeightedVotes = weightedVotes;
			}
		}

		// Calculate total weighted votes
		double totalWeightedVotes = weightedVotesMap.values().stream().mapToDouble(Double::doubleValue).sum();

		// Calculate vote counts and percentages
		Map<String, Integer> voteCounts = new HashMap<>();
		Map<String, Double> percentages = new HashMap<>();

		for (Option option : options) {
			double weightedVotes = weightedVotesMap.get(option.getTitle());

			// Round weighted votes to nearest integer for count
			int weightedCount = (int) Math.round(weightedVotes);
			voteCounts.put(option.getTitle(), weightedCount);

			// Calculate percentage with proper precision
			double percentage = totalWeightedVotes > 0 ? (weightedVotes * 100.0) / totalWeightedVotes : 0.0;

			// Round to 2 decimal places using proper rounding
			BigDecimal bd = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
			percentages.put(option.getTitle(), bd.doubleValue());
		}

		// Validate that percentages sum to approximately 100%
		double totalPercentage = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
		if (totalWeightedVotes > 0 && (totalPercentage < 99.99 || totalPercentage > 100.01)) {
			throw new IllegalStateException("Percentage calculation error: total = " + totalPercentage);
		}

		PollResult pollResult = new PollResult();
		pollResult.setWinner(winnerOpt != null ? winnerOpt.getTitle() : null);
		pollResult.setVoteCounts(voteCounts);
		pollResult.setVotePercentages(percentages);
		pollResult.setTotalVotes(poll.getTotalVote());
		pollResult.setPublished(false);

		return pollResult;
	}

	public static double[] normalizeWeights(double[] weights) {

		double totalSum = Arrays.stream(weights).sum();

		double[] normalizedWeights = new double[weights.length];
		for (int i = 0; i < weights.length; i++) {
			normalizedWeights[i] = weights[i] / totalSum;
		}

		return normalizedWeights;
	}

}

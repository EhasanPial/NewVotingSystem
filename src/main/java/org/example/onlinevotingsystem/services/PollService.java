package org.example.onlinevotingsystem.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.onlinevotingsystem.StrategyPattern.FirstPastThePostStrategy;
import org.example.onlinevotingsystem.StrategyPattern.PollResult;
import org.example.onlinevotingsystem.StrategyPattern.VotingStrategy;
import org.example.onlinevotingsystem.StrategyPattern.WeightedVotingStrategy;
import org.example.onlinevotingsystem.models.Constants;
import org.example.onlinevotingsystem.models.OpenPollFactory;
import org.example.onlinevotingsystem.models.Option;
import org.example.onlinevotingsystem.models.Poll;
import org.example.onlinevotingsystem.models.PollFactory;
import org.example.onlinevotingsystem.models.PollRequest;
import org.example.onlinevotingsystem.models.TimePollFactory;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.repositories.OptionRepository;
import org.example.onlinevotingsystem.repositories.PollRepository;
import org.example.onlinevotingsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PollService {

	@Autowired
	private PollRepository pollRepository;

	@Autowired
	private OptionRepository optionRepository;

	@Autowired
	private UserService adminService;

	@Autowired
	private UserRepository voterRepository;

	@Autowired
	private NotificationService notificationService;

	private final Map<String, PollFactory> factories;
	private final Map<String, VotingStrategy> votingStrategies;

	public PollService() {

		factories = new HashMap<>();
		factories.put("OPEN", new OpenPollFactory());
		factories.put("TIME", new TimePollFactory());

		votingStrategies = new HashMap<>();
		votingStrategies.put(Constants.TRADITIONAL_METHOD, new FirstPastThePostStrategy());
		votingStrategies.put(Constants.WEIGHTED_METHOD, new WeightedVotingStrategy(null));
	}

	public List<Poll> getAllPolls() {
		return pollRepository.findAll();
	}

	public void createPollWithOptions(PollRequest poll, List<String> optionTitles, String type) {

		Optional<User> adminUser = adminService.getVoterByUsername(Constants.ADMIN_TYPE_1_USER_NAME);

		if (adminUser.isPresent()) {
			poll.setAdmin(adminUser.get());
		}

		PollFactory factory = factories.get(type);
		Poll newPoll = factory.createPoll(poll);
		Poll savedPoll = pollRepository.save(newPoll);

		for (String title : optionTitles) {
			Option option = new Option();
			option.setTitle(title);
			option.setVoteCount(0);
			option.setVotePercentage(0);
			option.setPoll(savedPoll);
			optionRepository.save(option);
		}
	}

	public void castVote(int optionId, String username) {
		Option option = optionRepository.findById(optionId).orElseThrow(() -> new RuntimeException("Option not found"));

		// Increment vote count
		option.setVoteCount(option.getVoteCount() + 1);

		// Update poll's total vote count
		Poll poll = option.getPoll();
		poll.setTotalVote(poll.getTotalVote() + 1);

		// Recalculate percentages based on voting strategy
		VotingStrategy votingStrategy = votingStrategies.get(poll.getVotingStrategy());
		PollResult result = votingStrategy.calculateResults(poll);

		// check poll result is already save or not in db
		if (poll.getPollResults() == null) {
			result.setTotalVotes(poll.getTotalVote()); // Ensure totalVotes is set
			poll.setPollResults(result);
			result.setPoll(poll);
		} else {
			PollResult existingResult = poll.getPollResults();
			existingResult.setTotalVotes(poll.getTotalVote());
			existingResult.setVoteCounts(result.getVoteCounts());
			existingResult.setVotePercentages(result.getVotePercentages());
			existingResult.setWinner(result.getWinner());
		}
		result.setPoll(poll);
		poll = pollRepository.save(poll);

		Optional<User> voter = voterRepository.findByUsername(username);
		if (voter.isPresent()) {
			List<Poll> voterPolls = voter.get().getVotedPolls();
			if (voterPolls == null) {
				voterPolls = new ArrayList<>();
			}
			voterPolls.add(poll);
			voterRepository.save(voter.get());
			poll.getVoters().add(voter.get());
		}
		Poll updatedPoll = pollRepository.save(poll);
		option.setVotePercentage(result.getVotePercentages().get(option.getTitle()));

		// update all options with new vote percentage
		List<Option> options = updatedPoll.getOptions();
		for (Option opt : options) {
			opt.setVotePercentage(result.getVotePercentages().get(opt.getTitle()));
			opt.setVoteCount(result.getVoteCounts().get(opt.getTitle()));

			if (option.getOptionId() == opt.getOptionId()) {
				if (opt.getUsers() == null) {
					opt.setUsers(new ArrayList<>());
				}
				if (voter.isPresent()) {
					opt.getUsers().add(voter.get());
				}
			}
			optionRepository.save(opt);
		}

		// Notify subscribed voters
		notificationService.sendNotification(updatedPoll, username);

	}

	


	public Map<Integer, Boolean> getAlreadyVottedMap(User user) {
		Map<Integer, Boolean> map = new HashMap<>();
		user.getVotedPolls().forEach(poll -> map.put(poll.getPollId(), true));

		return map;
	}

	public Map<Integer, Boolean> getVotedOptions(List<Poll> polls, Long userId) {
		Map<Integer, Boolean> votedOptions = new HashMap<>();

		// Early return if inputs are null
		if (polls == null || userId == null) {
			return votedOptions;
		}

		// First initialize all options to false
		for (Poll poll : polls) {
			if (poll.getOptions() != null) {
				for (Option option : poll.getOptions()) {
					votedOptions.put((int) option.getOptionId(), false);
				}
			}
		}

		// Then mark the options the user has voted on as true
		for (Poll poll : polls) {
			if (poll.getOptions() != null) {
				for (Option option : poll.getOptions()) {
					if (option.getUsers() != null) {
						for (User user : option.getUsers()) {
							if (userId.equals(user.getId())) {
								votedOptions.put((int) option.getOptionId(), true);
								break; // No need to check other users once we found a match
							}
						}
					}
				}
			}
		}

		return votedOptions;
	}
}

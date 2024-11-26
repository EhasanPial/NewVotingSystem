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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

	public void createPollWithOptions(PollRequest poll, List<String> optionTitles, List<String> optionWeights,
			String type) {

		Optional<User> adminUser = adminService.getVoterByUsername(Constants.ADMIN_TYPE_1_USER_NAME);

		if (adminUser.isPresent()) {
			poll.setAdmin(adminUser.get());
		}
		if (poll.getVotingStrategy().equals(Constants.WEIGHTED_METHOD) && optionWeights.size() == optionTitles.size()) {
			String weights = String.join("-", optionWeights);
			poll.setWeight(weights);
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
	    // Fetch the Option
	    Option option = optionRepository.findById(optionId)
	            .orElseThrow(() -> new RuntimeException("Option not found"));

	    // Increment vote count for the selected option
	    option.setVoteCount(option.getVoteCount() + 1);
	    optionRepository.save(option); // Persist updated option immediately

	    // Retrieve the associated Poll
	    Poll poll = option.getPoll();
	    poll.setTotalVote(poll.getTotalVote() + 1);

	    // Ensure poll options have updated vote counts
	    poll.getOptions().forEach(opt -> {
	        if (opt.getOptionId() == optionId) {
	            opt.setVoteCount(option.getVoteCount());
	        }
	    });

	    // Update all options' vote percentages and counts using the voting strategy
	    VotingStrategy votingStrategy = votingStrategies.get(poll.getVotingStrategy());
	    PollResult result = votingStrategy.calculateResults(poll);

	    // Save or update the poll result
	    PollResult existingResult = poll.getPollResults();
	    if (existingResult == null) {
	        result.setTotalVotes(poll.getTotalVote());
	        result.setPoll(poll);
	        poll.setPollResults(result);
	    } else {
	        existingResult.setTotalVotes(poll.getTotalVote());
	        existingResult.setVoteCounts(result.getVoteCounts());
	        existingResult.setVotePercentages(result.getVotePercentages());
	        existingResult.setWinner(result.getWinner());
	    }

	    // Persist the poll
	    pollRepository.save(poll);

	    // Update all options with the latest percentages from the voting result
	    poll.getOptions().forEach(opt -> {
	        opt.setVotePercentage(result.getVotePercentages().get(opt.getTitle()));
	        optionRepository.save(opt); // Save updated option
	    });

	    // Link the voter to the poll
	    Optional<User> voter = voterRepository.findByUsername(username);
	    voter.ifPresent(user -> {
	        if (user.getVotedPolls() == null) {
	            user.setVotedPolls(new ArrayList<>());
	        }
	        user.getVotedPolls().add(poll);
	        poll.getVoters().add(user);
	        voterRepository.save(user);
	    });

	    // Notify subscribers about the updated poll
	    notificationService.sendNotification(poll, username);
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

	public boolean toggleFavorite(Long pollId, String username) {
		Optional<Poll> pollOpt = pollRepository.findByPollId(pollId);

		if (pollOpt.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found");
		}
		Poll poll = pollOpt.get();

		Optional<User> userOpt = voterRepository.findByUsername(username);

		if (userOpt.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		User user = userOpt.get();

		if (user.getFavoritePolls().contains(poll)) {
			user.getFavoritePolls().remove(poll);
		} else {
			user.getFavoritePolls().add(poll);

		}

		voterRepository.save(user);
		return user.getFavoritePolls().contains(poll);
	}

	public Map<Integer, Boolean> getFavoritePolls(Long id) {
		Map<Integer, Boolean> favoritePolls = new HashMap<>();
		Optional<User> user = voterRepository.findById(id);
		if (user.isPresent()) {
			user.get().getFavoritePolls().forEach(poll -> favoritePolls.put(poll.getPollId(), true));
		}
		return favoritePolls;
	}

	public List<Poll> getFavoritePollsList(Long id) {

		Optional<User> user = voterRepository.findById(id);
		if (user.isPresent()) {
			return user.get().getFavoritePolls();
		}
		return new ArrayList<>();
	}

}

package org.example.onlinevotingsystem.DecoratorPattern;

import java.util.List;

import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.services.PollService;

public class FavoriteDecorator extends BasePollDecorator {

	private PollService pollService;
 
	public FavoriteDecorator(IPollDecorator pollDecorator, PollService pollService) {
		super(pollDecorator);
		this.pollService = pollService;
	}

	@Override
	public boolean performOperation(String message, String username, List<User> voters) {
		return pollService.toggleFavorite((long)poll.getPollId(), username);
	}

	
}

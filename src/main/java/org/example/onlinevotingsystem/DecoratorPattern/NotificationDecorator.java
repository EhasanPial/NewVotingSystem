package org.example.onlinevotingsystem.DecoratorPattern;

import java.util.List;

import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.services.NotificationService;

public class NotificationDecorator extends BasePollDecorator {

	private NotificationService notificationService;
 
	public NotificationDecorator(IPollDecorator pollDecorator, NotificationService notificationService) {
		super(pollDecorator);
		this.notificationService = notificationService;
	}

	@Override
	public void performOperation(String message, String username, List<User> voters) {
		notificationService.notifyVoters(message, username, voters, poll);
	}

	
}

package org.example.onlinevotingsystem.services;

import java.util.List;
import java.util.Set;

import org.example.onlinevotingsystem.DecoratorPattern.BasePollDecorator;
import org.example.onlinevotingsystem.DecoratorPattern.IPollDecorator;
import org.example.onlinevotingsystem.DecoratorPattern.NotificationDecorator;
import org.example.onlinevotingsystem.models.Notification;
import org.example.onlinevotingsystem.models.Poll;
import org.example.onlinevotingsystem.models.Role;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.repositories.NotificationRepository;
import org.example.onlinevotingsystem.repositories.PollRepository;
import org.example.onlinevotingsystem.repositories.RoleRepository;
import org.example.onlinevotingsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private PollRepository pollRepository;

	@Autowired
	private UserRepository voterRepository;

	@Autowired
	private RoleRepository roleRepository;

	public void markAsRead(Long notificationId, String username) {
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new RuntimeException("Notification not found"));

		if (!notification.getRecipient().getUsername().equals(username)) {
			throw new SecurityException("Unauthorized access to this notification.");
		}

		notification.setRead(true);
		notificationRepository.save(notification);
	}

	public List<Notification> getAllNotifications(User voter) {

		return notificationRepository.findByRecipient(voter);

	}

	public String subscribeToPoll(Long pollId, String username) {
		User voter = voterRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		Poll poll = pollRepository.findByPollId(pollId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found"));

		if (!poll.getSubscribedVoters().contains(voter)) {
			poll.subscribe(voter);
			pollRepository.save(poll);
			voter.addSubscribedPoll(poll);
			voterRepository.save(voter);
			return "Subscribed successfully to the poll.";
		}

		throw new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed to this poll.");
	}

	public String unsubscribeFromPoll(Long pollId, String username) {
		User voter = voterRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		Poll poll = pollRepository.findByPollId(pollId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found"));

		if (poll.getSubscribedVoters().contains(voter)) {
			poll.unsubscribe(voter);
			pollRepository.save(poll);
			voter.removeSubscribedPoll(poll);
			voterRepository.save(voter);
			return "Unsubscribed successfully from the poll.";
		}

		throw new ResponseStatusException(HttpStatus.CONFLICT, "You are not subscribed to this poll.");
	}

	public void sendNotification(Poll updatedPoll, String username) {
		IPollDecorator pollDecorator = new BasePollDecorator(updatedPoll);
		pollDecorator = new NotificationDecorator(pollDecorator, this);

		pollDecorator.performOperation("The poll '" + updatedPoll.getTitle() + "' has been updated.", username,
				updatedPoll.getSubscribedVoters());

	}

	public boolean notifyVoters(String message, String username, List<User> subscribedVoters, Poll poll) {

		try {
			for (User voter : subscribedVoters) {
				if (voter.getUsername().equals(username)) {
					continue;
				}
				Notification notification = new Notification(message, voter, poll,
						Notification.NotificationType.POLL_UPDATED);
				notificationRepository.save(notification);
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void sendNotificationToAdmin(String role) {

		// get admin by roles
		// get all users with role admin
		List<User> notifiedAdmins = voterRepository
				.findAllByRolesIn(Set.of(roleRepository.findByName(role).map(Role::getId).orElseThrow()));

		// send notification to role admin
		for (User admin : notifiedAdmins) {
			Notification notification = new Notification("New user registered", admin, null,
					Notification.NotificationType.NEW_USER);
			notificationRepository.save(notification);
		}

	}

}

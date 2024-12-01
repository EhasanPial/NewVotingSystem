package org.example.onlinevotingsystem.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.onlinevotingsystem.models.Category;
import org.example.onlinevotingsystem.models.Constants;
import org.example.onlinevotingsystem.models.Notification;
import org.example.onlinevotingsystem.models.Poll;
import org.example.onlinevotingsystem.models.PollRequest;
import org.example.onlinevotingsystem.models.Role;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.repositories.UserRepository;
import org.example.onlinevotingsystem.services.CategoryService;
import org.example.onlinevotingsystem.services.NotificationService;
import org.example.onlinevotingsystem.services.PollService;
import org.example.onlinevotingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private PollService pollService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserRepository voterRepository;

	@Autowired
	private UserService userService;

	@GetMapping("/admin-index")
	public String showAdminIndexPage(Model model, Principal principal) {
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("categories", categories);

		Optional<User> currentUser = voterRepository.findByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser.get());

		if (!currentUser.isPresent()) {
			return "redirect:/login";
		}
		List<Notification> notifications = notificationService.getAllNotifications(currentUser.get());

		// unread notifications count
		long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
		model.addAttribute("unreadcount", unreadCount);
		model.addAttribute("notifications", notifications);
		model.addAttribute("currentPage", "dashboard");

		List<Poll> polls = pollService.getAllPolls();
		Collections.reverse(polls);
		model.addAttribute("polls", polls);

		return "admin-index";
	}

	@GetMapping("/admin-create-poll")
 	public String showPollCreationForm(Model model, Principal principal) {
		Set<Role> role = voterRepository.findByUsername(principal.getName()).get().getRoles();
		boolean isAdminApprover = role.stream().anyMatch(r -> r.getName().equals(Constants.ROLE_ADMIN_POLL_Approver));
		if (!isAdminApprover) {
			// show alert
		    model.addAttribute("error", "You are not authorized to create a poll.");
		}
		model.addAttribute("poll", new PollRequest());
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("categories", categories);
		model.addAttribute("currentPage", "admin-create-poll");
		model.addAttribute("isAdminApprover", isAdminApprover);
		User currentUser = voterRepository.findByUsername(principal.getName()).get();
		List<Notification> notifications = notificationService.getAllNotifications(currentUser);

		// unread notifications count
		long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
		model.addAttribute("unreadcount", unreadCount);
		model.addAttribute("notifications", notifications);

		return "poll-create";
	}

	@PostMapping("/admin-create-poll")
	public String createPoll(@ModelAttribute PollRequest poll, @RequestParam List<String> optionTitles,
			@RequestParam List<String> optionWeights, RedirectAttributes redirectAttributes) {
		try {
			pollService.createPollWithOptions(poll, optionTitles, optionWeights, poll.getType());
			redirectAttributes.addFlashAttribute("message", "Poll created successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "An error occurred while creating the poll.");
			redirectAttributes.addFlashAttribute("message", "Failed to create poll!");
		}
		return "redirect:/admin-create-poll";
	}

	@GetMapping("/admin-user-list")
	public String listUsers(Model model, Principal principal) {
		// Get role
		Set<Role> role = voterRepository.findByUsername(principal.getName()).get().getRoles();
		boolean isAdminApprover = role.stream().anyMatch(r -> r.getName().equals(Constants.ROLE_ADMIN_USER_Approver));
		List<User> users = userService.findAllUsers();
		model.addAttribute("users", users);
		model.addAttribute("isAdminApprover", isAdminApprover);
		
		User currentUser = voterRepository.findByUsername(principal.getName()).get();
		List<Notification> notifications = notificationService.getAllNotifications(currentUser);

		// unread notifications count
		long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
		model.addAttribute("unreadcount", unreadCount);
		model.addAttribute("notifications", notifications);
		return "user";
	}

	@PostMapping("/admin-approve/{id}")
	public String approveUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			userService.approveUser(id);
			redirectAttributes.addFlashAttribute("successMessage", "User approved successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve user");
		}
		return "redirect:/admin-user-list";
	}

}

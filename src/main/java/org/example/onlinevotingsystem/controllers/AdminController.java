package org.example.onlinevotingsystem.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.example.onlinevotingsystem.models.Category;
import org.example.onlinevotingsystem.models.Notification;
import org.example.onlinevotingsystem.models.PollRequest;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.repositories.UserRepository;
import org.example.onlinevotingsystem.services.CategoryService;
import org.example.onlinevotingsystem.services.NotificationService;
import org.example.onlinevotingsystem.services.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

		return "admin-index";
	}

	@GetMapping("/admin-create-poll")
	public String showPollCreationForm(Model model) {
		model.addAttribute("poll", new PollRequest());
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("categories", categories);
	    model.addAttribute("currentPage", "admin-create-poll");

		return "poll-create";
	}

	@PostMapping("/admin-create-poll")
	public String createPoll(@ModelAttribute PollRequest poll, @RequestParam List<String> optionTitles,
			RedirectAttributes redirectAttributes) {
		try {
			pollService.createPollWithOptions(poll, optionTitles, poll.getType());
			redirectAttributes.addFlashAttribute("message", "Poll created successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "An error occurred while creating the poll.");
			redirectAttributes.addFlashAttribute("message", "Failed to create poll!");
		}
		return "redirect:/admin-create-poll";
	}

}

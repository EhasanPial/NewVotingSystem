package org.example.onlinevotingsystem.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.onlinevotingsystem.models.Category;
import org.example.onlinevotingsystem.models.Notification;
import org.example.onlinevotingsystem.models.Poll;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private PollService pollService;

	@Autowired
	private UserRepository voterRepository;

	@Autowired
	private NotificationService notificationService;

	@GetMapping("/admin-category-create")
	public String showCategoryForm(Model model) {
		model.addAttribute("category", new Category());
		return "category-create"; // Thymeleaf template
	}

	@PostMapping("/admin-category-create")
	public String createCategory(@ModelAttribute Category category, Model model) {
		categoryService.createCategory(category);
		model.addAttribute("message", "Category created successfully!");
		return "category-create";
	}

	@GetMapping("/categrories")
	public String showAdminIndexPage(Model model, Principal principal) {
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("categories", categories);

		return "categoriesview";
	}

	@GetMapping("/categories/{id}")
	public String getCategoryDetails(@PathVariable("id") Long id, Model model, Principal principal) {
		List<Poll> tempPolls = pollService.getAllPolls();
		// reverse polls
		Collections.reverse(tempPolls);
		List<Poll> polls = new ArrayList<Poll>();
		for (Poll poll : tempPolls) {
			if (poll.getCategory().getCategoryId() == id) {
				polls.add(poll);
			}
		}
		Optional<User> currentUser = voterRepository.findByUsername(principal.getName());

		if (currentUser.isPresent()) {
			model.addAttribute("currentUser", currentUser.get());
			List<Notification> notifications = notificationService.getAllNotifications(currentUser.get());
			Collections.sort(notifications, (n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
			// unread notifications count
			long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
			model.addAttribute("unreadcount", unreadCount);
			model.addAttribute("notifications", notifications);

			// already votted map of current user
			Map<Integer, Boolean> map;
			map = pollService.getAlreadyVottedMap(currentUser.get());
			model.addAttribute("alreadyVottedMap", map);

			Map<Integer, Boolean> votedOptions = pollService.getVotedOptions(polls, currentUser.get().getId());
			model.addAttribute("vottedOptionsMap", votedOptions);
			
			Map<Integer, Boolean> favoritePolls = pollService.getFavoritePolls(currentUser.get().getId());
			model.addAttribute("favoritePollsMap",favoritePolls);
			
			

		} else {
			model.addAttribute("currentUser", null);
			model.addAttribute("vottedOptionsMap", new HashMap<Long, Boolean>());
			model.addAttribute("alreadyVottedMap", new HashMap<Integer, Boolean>());
		}

		model.addAttribute("polls", polls);

		return "polls-view";
	}
	
	
}

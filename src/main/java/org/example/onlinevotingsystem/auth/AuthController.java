package org.example.onlinevotingsystem.auth;

import org.example.onlinevotingsystem.FacadePattern.IFacade;
import org.example.onlinevotingsystem.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@Controller
public class AuthController {
	@Autowired
	private IFacade votingSystemFacade;

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute("user") @Valid User user, Model model) {
		votingSystemFacade.createUserAndAdminApprovalNotification(user);
		model.addAttribute("message", "Registration successful. Please wait for admin approval.");
		return "login";
	}

	@GetMapping("/login")
	public String login(@RequestParam(required = false) String error, Model model) {
		model.addAttribute("error", error);
		return "login";
	}

	@GetMapping("/index")
	public String showIndexPage() {

		return "redirect:/polls";
	}
}

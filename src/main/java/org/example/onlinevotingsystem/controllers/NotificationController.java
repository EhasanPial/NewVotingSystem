package org.example.onlinevotingsystem.controllers;

import java.security.Principal;

import org.example.onlinevotingsystem.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@PostMapping("/notifications/{id}/mark-read")
	public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) throws SecurityException {
		try {
			notificationService.markAsRead(id, principal.getName());
			return ResponseEntity.ok("Notification marked as read.");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("An error occurred.");
		}
	}
}
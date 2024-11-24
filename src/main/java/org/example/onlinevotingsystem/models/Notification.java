package org.example.onlinevotingsystem.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
	public Notification(String message, User recipient, Poll relatedPoll, NotificationType type) {
		this.message = message;
		this.recipient = recipient;
		this.relatedPoll = relatedPoll;
		this.timestamp = LocalDateTime.now();
		this.type = type;
	}
 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private LocalDateTime timestamp;
	
	@Column(name = "is_read")
	private boolean read = false;

	@ManyToOne
	@JoinColumn(name = "nid")
	private User recipient;

	@ManyToOne
	@JoinColumn(name = "PollID")
	private Poll relatedPoll;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	public enum NotificationType {
		POLL_CREATED, POLL_UPDATED
	}
	
	

}

function toggleSubscription(pollId, buttonElement) {
	const isSubscribed = buttonElement.classList.contains('subscribed');
	const endpoint = isSubscribed ? `/polls/${pollId}/unsubscribe` : `/polls/${pollId}/subscribe`;

	fetch(endpoint, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		}
	})
		.then(response => {
			if (response.ok) {
				buttonElement.classList.toggle('subscribed');

				// Update tooltip text
				if (buttonElement.classList.contains('subscribed')) {
					buttonElement.title = 'Click to unsubscribe';
					showToast('Successfully subscribed to poll notifications');
				} else {
					buttonElement.title = 'Click to subscribe';
					showToast('Successfully unsubscribed from poll notifications');
				}
			} else {
				showToast('Something went wrong. Please try again.');
			}
		})
		.catch(error => {
			console.error('Error:', error);
			showToast('Something went wrong. Please try again.');
		});
}

// Toast notification function
function showToast(message) {
	const toast = document.createElement('div');
	toast.className = 'toast-notification';
	toast.textContent = message;
	document.body.appendChild(toast);

	// Trigger fade in
	setTimeout(() => {
		toast.classList.add('show');
	}, 100);

	// Remove after 3 seconds
	setTimeout(() => {
		toast.classList.remove('show');
		setTimeout(() => {
			document.body.removeChild(toast);
		}, 300);
	}, 3000);
}

function toggleNotifications() {
	const popup = document.getElementById('notificationPopup');
	popup.classList.toggle('show');
}

function markAsRead(notificationId) {
	fetch(`/notifications/${notificationId}/mark-read`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		}
	})
		.then(response => {
			if (response.ok) {
				// Find and update the notification item
				const notificationItem = document.querySelector(`[data-notification-id="${notificationId}"]`);
				if (notificationItem) {
					notificationItem.classList.remove('unread');
					const markReadButton = notificationItem.querySelector('.mark-read-button');
					if (markReadButton) {
						markReadButton.remove();
					}
				}

				// Update badge count
				updateNotificationBadge();
			}
		})
		.catch(error => console.error('Error:', error));
}

function updateNotificationBadge() {
	const unreadCount = document.querySelectorAll('.notification-item.unread').length;
	const badge = document.querySelector('.notification-badge');

	if (unreadCount > 0) {
		if (badge) {
			badge.textContent = unreadCount;
		} else {
			const newBadge = document.createElement('span');
			newBadge.className = 'notification-badge';
			newBadge.textContent = unreadCount;
			document.querySelector('.notification-button').appendChild(newBadge);
		}
	} else if (badge) {
		badge.remove();
	}
}

// Close popup when clicking outside
document.addEventListener('click', function(event) {
	const popup = document.getElementById('notificationPopup');
	const button = document.querySelector('.notification-button');

	if (!popup.contains(event.target) && !button.contains(event.target)) {
		popup.classList.remove('show');
	}
});
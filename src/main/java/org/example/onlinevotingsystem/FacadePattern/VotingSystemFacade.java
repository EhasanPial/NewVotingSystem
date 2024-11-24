package org.example.onlinevotingsystem.FacadePattern;

import org.example.onlinevotingsystem.models.Constants;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.services.NotificationService;
import org.example.onlinevotingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingSystemFacade implements IFacade {

	
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private UserService userService;
	

	@Override
	@Transactional
	public void createUserAndAdminApprovalNotification(User user) {
		// create user
		userService.registerUser(user);
		// create notification
		notificationService.sendNotificationToAdmin(Constants.ROLE_ADMIN_USER_Approver);
	}

}

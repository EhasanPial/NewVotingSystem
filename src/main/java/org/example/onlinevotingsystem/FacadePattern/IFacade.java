package org.example.onlinevotingsystem.FacadePattern;

import org.example.onlinevotingsystem.models.User;

public interface IFacade {

	void createUserAndAdminApprovalNotification(User user);

}
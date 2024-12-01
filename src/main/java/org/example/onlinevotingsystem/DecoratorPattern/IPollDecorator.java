package org.example.onlinevotingsystem.DecoratorPattern;

import java.util.List;

import org.example.onlinevotingsystem.models.Poll;
import org.example.onlinevotingsystem.models.User;
 
public interface IPollDecorator {

	boolean performOperation(String message, String username, List<User> voters);

	Poll getPoll();
}

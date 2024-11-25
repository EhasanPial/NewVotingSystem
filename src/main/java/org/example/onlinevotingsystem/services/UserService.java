package org.example.onlinevotingsystem.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.onlinevotingsystem.models.Role;
import org.example.onlinevotingsystem.models.User;
import org.example.onlinevotingsystem.repositories.RoleRepository;
import org.example.onlinevotingsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User registerUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("User Role not set."));
		user.setRoles(Set.of(userRole));

		return userRepository.save(user);
	}

	public List<User> findUsers() {
		return userRepository
				.findAllByRolesIn(Set.of(roleRepository.findByName("ROLE_USER").map(Role::getId).orElseThrow()));
	}

	public void enableUser(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		user.setEnabled(true);
		userRepository.save(user);
	}

	// Get User by NID
	public Optional<User> getVoterByNid(long nid) {
		return userRepository.findById(nid);
	}

	// Get User by Username
	public Optional<User> getVoterByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	// Get User by Email
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	@Transactional
	public void approveUser(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		user.setEnabled(true);
		userRepository.save(user);
	}	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
		System.out.println("Retrieved user: " + user.getUsername());
		System.out.println("Is user enabled? " + user.isEnabled());
		System.out.println("User roles: " + user.getRoles());
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.isEnabled(), true, true, true, mapRolesToAuthorities(user.getRoles()));
	}

	private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username).orElse(null);
	}
}

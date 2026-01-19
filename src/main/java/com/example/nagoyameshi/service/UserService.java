package com.example.nagoyameshi.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User createUser(SignupForm signupForm) {
		User user = new User();
		Role role = roleRepository.findByName("ROLE_FREE_MEMBER");

		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(false);

		if (!signupForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(signupForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		} else {
			user.setBirthday(null);
		}

		if (!signupForm.getOccupation().isEmpty()) {
			user.setOccupation(signupForm.getOccupation());
		} else {
			user.setOccupation(null);
		}

		return userRepository.save(Objects.requireNonNull(user));
	}

	@Transactional
	public void updateUser(User user, UserEditForm userEditForm) {
		user.setName(userEditForm.getName());
		user.setFurigana(userEditForm.getFurigana());
		user.setPostalCode(userEditForm.getPostalCode());
		user.setAddress(userEditForm.getAddress());
		user.setPhoneNumber(userEditForm.getPhoneNumber());
		user.setEmail(userEditForm.getEmail());

		if (!userEditForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(userEditForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		} else {
			user.setBirthday(null);
		}

		if (!userEditForm.getOccupation().isEmpty()) {
			user.setOccupation(userEditForm.getOccupation());
		} else {
			user.setOccupation(null);
		}
		userRepository.save(Objects.requireNonNull(user));
	}

	public boolean isEmailRegisterd(String email) {
		Optional<User> userOptional = userRepository.findByEmail(email);
		User user = userOptional.get();
		return user != null;

	}

	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}

	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(Objects.requireNonNull(user));
	}

	public Page<User> findAllUsers(Pageable pageable) {
		return userRepository.findAll(Objects.requireNonNull(pageable));
	}

	public Page<User> findUsersByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable) {
		return userRepository.findByNameLikeOrFuriganaLike("%" + nameKeyword + "%", "%" + furiganaKeyword + "%",
				Objects.requireNonNull(pageable));
	}

	public Optional<User> findUserById(Integer id) {
		return userRepository.findById(Objects.requireNonNull(id));
	}

	public boolean isEmailChanged(UserEditForm userEditForm, User user) {
		return !userEditForm.getEmail().equals(user.getAddress());
	}

	public Optional<User> findUserByEmail(String email) {
		return userRepository.findByEmail(Objects.requireNonNull(email));
	}

	@Transactional
	public void saveStripeCustomerId(User user, String stripeCustomerId) {
		user.setStripeCustomerId(stripeCustomerId);
		userRepository.save(Objects.requireNonNull(user));
	}

	@Transactional
	public void updateRole(User user, String roleName) {
		Role role = roleRepository.findByName(roleName);
		user.setRole(role);
		userRepository.save(Objects.requireNonNull(user));
	}

	// 認証情報のロールを更新する
	public void refreshAuthenticationByRole(String newRole) {
		// 現在の認証情報を取得する
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

		// 新しい認証情報を作成する
		List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
		simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(),
				currentAuthentication.getCredentials(), simpleGrantedAuthorities);

		// 認証情報を更新する
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
	}

	public Integer countUsersByRole_Name(String roleName) {
		return userRepository.countByRole_Name(Objects.requireNonNull(roleName));
	}

}

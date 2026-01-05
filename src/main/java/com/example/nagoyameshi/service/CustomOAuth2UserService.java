package com.example.nagoyameshi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// Googleからユーザー情報を取得
		OAuth2User oAuth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = oAuth2User.getAttributes();

		// Googleから送られてくる属性情報を取得
		String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
		// DBにユーザーがいるか確認、いなければ作成
        User user = userRepository.findByEmail(email).orElseGet(() -> 
        registerNewUser(email, name) 
    );
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

        return new UserDetailsImpl(user, authorities, attributes);
	}

	private User registerNewUser(String email, String name) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		// パスワードは不要だが、DBの制約がある場合はダミー値を入れる（エンコード済みのもの）
		user.setPassword("");
		user.setFurigana("フリガナ");       // 必須なら初期値をセット（ダミー）
	    user.setPostalCode("000-0000");   // 必須なら初期値をセット（ダミー）
	    user.setAddress("未設定");         // 必須なら初期値をセット（ダミー）
	    user.setPhoneNumber("000-0000-0000");	// 必須なら初期値をセット（ダミー）

		// ロールを「無料会員」に設定
		Role role = roleRepository.findByName("ROLE_FREE_MEMBER");
		user.setRole(role);

		// 有効フラグを立てる
		user.setEnabled(true);

		return userRepository.save(user);
	}
}
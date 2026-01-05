package com.example.nagoyameshi.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.nagoyameshi.entity.User;

public class UserDetailsImpl implements UserDetails, OAuth2User {
	private final User user;
	private final Collection<GrantedAuthority> authorities;
	private Map<String, Object> attributes;

	public UserDetailsImpl(User user, Collection<GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
	}
	
	public UserDetailsImpl(User user, Collection<GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.user = user;
        this.authorities = authorities;
        this.attributes = attributes;
    }

	public User getUser() {
		return user;
	}

	// ハッシュ化済みのパスワードを返す
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	// ログイン時に利用するユーザー名（メールアドレス）を返す
	@Override
	public String getUsername() {
		return user.getEmail();
	}

	// ロールのコレクションを返す
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	// アカウントが期限切れでなければtrueを返す
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// ユーザーがロックされていなければtrueを返す
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// ユーザーのパスワードが期限切れでなければtrueを返す
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// ユーザーが有効であればtrueを返す
	@Override
	public boolean isEnabled() {
		return user.getEnabled();
	}
	
	@Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2Userとしての名前（主キーなど）を返すが、
        // 今回はメールアドレスやGoogleのsub（ID）などを返せばOK
        return user.getEmail();
    }
}
package com.taskmanagement.security;

import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.entity.User;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

/** Реализация интерфейса UserDetails для аутентификации пользователей. */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String password;
    private String username;
    private String email;
    private RoleList role;

    /**
     * Создание UserDetailsImpl на основе пользователя.
     *
     * @param user Пользователь
     * @return UserDetailsImpl
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(), user.getPassword(), user.getUsername(),user.getEmail(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(getRole().toString());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

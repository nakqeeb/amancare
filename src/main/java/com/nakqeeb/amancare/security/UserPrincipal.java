// =============================================================================
// Updated UserPrincipal - تحديث UserPrincipal مع create method
// =============================================================================

package com.nakqeeb.amancare.security;

import com.nakqeeb.amancare.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * معلومات المستخدم المصادق عليه
 */
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Long clinicId;
    private String role;
    private String fullName;
    private boolean active;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password,
                         Long clinicId, String role, String fullName, boolean active,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.clinicId = clinicId;
        this.role = role;
        this.fullName = fullName;
        this.active = active;
        this.authorities = authorities;
    }

    /**
     * إنشاء UserPrincipal من User entity
     */
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getClinic().getId(),
                user.getRole().name(),
                user.getFullName(),
                user.getIsActive(),
                authorities
        );
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Long getClinicId() { return clinicId; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
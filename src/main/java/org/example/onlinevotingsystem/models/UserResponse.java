package org.example.onlinevotingsystem.models;

import java.util.HashSet;
import java.util.Set;

/**
 * implemented using Builder creational design pattern
 */

public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;

    public UserResponse(Long id, String name, String username, String email, Set<String> roles, boolean enabled) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.enabled = enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static class Builder {

        private Long id;
        private String name;
        private String username;
        private String email;
        private Set<String> roles = new HashSet<>();
        private boolean enabled = false;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, name, username, email, roles, enabled);
        }
    }
}

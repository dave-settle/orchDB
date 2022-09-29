/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.security;

/**
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.Role;
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.RoleRepository;
import com.banburysymphony.orchestra.jpa.UserRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

public class AppUserDetailsManager implements UserDetailsManager {

    private static final Logger log = LoggerFactory.getLogger(AppUserDetailsManager.class);

    UserRepository userRepository;

    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    public AppUserDetailsManager(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        log.debug("created application user details manager");
    }

    /**
     * Find all of the roles from the database, creating any that are not
     * present
     *
     * @param auths
     * @return
     */
    public Set<Role> collectAuthorities(Collection<? extends GrantedAuthority> auths) {
        Set<Role> result = new HashSet<>();
        for (GrantedAuthority a : auths) {
            Optional<Role> db = roleRepository.findByAuthority(a.getAuthority());
            Role r = db.isEmpty() ? roleRepository.save(new Role(a.getAuthority())) : db.get();
            result.add(r);
        }
        return result;
    }

    @Override
    public void createUser(UserDetails user) {
        /*
         * Convert the generic UserDetails to a specific User
         */
        User u = new User(user.getUsername(), passwordEncoder.encode(user.getPassword()));
        u.setRoles(collectAuthorities(user.getAuthorities()));
        userRepository.save(u);
    }

    @Override
    public void updateUser(UserDetails user) {
        Optional<User> u = userRepository.findByEmail(user.getUsername());
        if (u.isEmpty()) {
            throw new UnsupportedOperationException("User " + user.getUsername() + " not found");
        }
        log.debug("updating " + user.getUsername());
        User existing = u.get();
        existing.setPassword(passwordEncoder.encode(user.getPassword()));
        existing.setRoles(collectAuthorities(user.getAuthorities()));
        userRepository.save(existing);
    }

    @Override
    public void deleteUser(String username) {
        Optional<User> u = userRepository.findByEmail(username);
        if (u.isPresent()) {
            log.debug("deleting " + username);
            userRepository.delete(u.get());
        } else {
            throw new UnsupportedOperationException("User " + username + " not found");
        }
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> u = userRepository.findByEmail(user.getEmail());
        if (u.isEmpty()) {
            throw new UnsupportedOperationException("Current user " + user.getEmail() + " not found");
        }
        User existing = u.get();
        if (!passwordEncoder.matches(oldPassword, existing.getPassword())) {
            log.warn("old password is <" + existing.getPassword() + "> which does not match <" + oldPassword);
            throw new UnsupportedOperationException("Old password for " + user.getEmail() + " does not match");
        }
        log.debug("changing password for " + user.getEmail());
        existing.setPassword(passwordEncoder.encode(newPassword));
        existing = userRepository.save(existing);
    }

    @Override
    public boolean userExists(String username) {
        Optional<User> u = userRepository.findByEmail(username);
        return u.isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> u = userRepository.findByEmail(username);
        if (u.isPresent()) {
            return u.get();
        }
        throw new UsernameNotFoundException(" user " + username + " not found");
    }
}

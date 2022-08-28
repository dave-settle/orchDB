/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.security;

/**
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.UserRepository;
import java.util.Collection;
import java.util.Optional;
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

    PasswordEncoder passwordEncoder;

    public AppUserDetailsManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        log.debug("created application user details manager");
    }

    /**
     * Convert a list of authorities to a single string
     * @param auths
     * @return 
     */
    public String authority(Collection<? extends GrantedAuthority> auths) {
        String result = null;
        for(GrantedAuthority a: auths)
            result = a.toString();
        return result;
    }
    @Override
    public void createUser(UserDetails user) {
        userRepository.save(new User(user.getUsername(), passwordEncoder.encode(user.getPassword()), authority(user.getAuthorities())));
    }

    @Override
    public void updateUser(UserDetails user) {
        Optional<User> u = userRepository.findByEmail(user.getUsername());
        if (u.isPresent()) {
            log.debug("updating " + user.getUsername());
            User existing = u.get();
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
            existing.setRole(authority(user.getAuthorities()));
            userRepository.save(existing);
        } else {
            throw new UnsupportedOperationException("User " + user.getUsername() + " not found");
        }
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
        String currentUser = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> u = userRepository.findByEmail(currentUser);
        if (u.isEmpty()) {
            throw new UnsupportedOperationException("Current user " + currentUser + " not found");
        }
        User existing = u.get();
        if (!passwordEncoder.matches(oldPassword, existing.getPassword())) {
            log.warn("old password is <" + existing.getPassword() + "> which does not match <" + oldPassword);
            throw new UnsupportedOperationException("Old password for " + currentUser + " does not match");
        }
        log.debug("changing password for " + currentUser);
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
        if(u.isPresent())
            return u.get();
        throw new UsernameNotFoundException(" user " + username + " not found");
    }
}

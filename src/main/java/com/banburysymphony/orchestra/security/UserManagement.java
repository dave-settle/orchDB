/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.security;

/**
 *
 * @author dave.settle@osinet.co.uk on 25 Aug 2022
 */
import com.banburysymphony.orchestra.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
public class UserManagement {

    private static final Logger log = LoggerFactory.getLogger(UserManagement.class);
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordEncoder passwordEncoder;    
    
    @Bean
    public UserDetailsManager userDetailsManager() {
        log.debug("returning appDetailsManager");
        return new AppUserDetailsManager(userRepository, passwordEncoder);
    }

}

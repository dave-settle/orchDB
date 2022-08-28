/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.security;

/**
 *
 * @author dave.settle@osinet.co.uk on 25 Aug 2022
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordManagement {

    private static final Logger log = LoggerFactory.getLogger(PasswordManagement.class);
        
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("returning passwordEncoder");
        return new BCryptPasswordEncoder();
    }
    

}

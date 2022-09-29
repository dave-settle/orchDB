/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra;

/**
 *
 * @author dave.settle@osinet.co.uk on 21 Sept 2022
 */
import com.banburysymphony.orchestra.jpa.RoleRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@SpringBootTest
public class RoleRepositoryTests {

    private static final Logger log = LoggerFactory.getLogger(RoleRepositoryTests.class);
    
    @Autowired
    RoleRepository roleRepository;
    
    @Test
    public void createRoleTest() {
        log.warn("creating roles");
    }
}

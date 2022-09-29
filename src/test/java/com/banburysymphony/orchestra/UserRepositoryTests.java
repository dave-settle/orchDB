/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra;

/**
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.Role;
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.UserRepository;
import java.util.Optional;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@SpringBootTest
public class UserRepositoryTests {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryTests.class);
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    UserDetailsManager userDetailsManager;
    
    @Test
    public void testCreateUser() {
        String username = "dave.settle@osinet.co.uk";
        User user = new User(username, "pw@Corner");
        user.getRoles().add(new Role(Role.Name.ADMIN.toString()));
        /*
         * Using the userDetailsManager, create an entry if not already present
         */
        if(!userDetailsManager.userExists(username)) {
            userDetailsManager.createUser(user);
            // userDetailsManager.deleteUser(username);
        }
        /*
         * Using the direct access to the table, check that it's there
         */
        Optional<User> existingUser = userRepository.findByEmail(username);
        assertThat(existingUser.isPresent()).isTrue();
        assertThat(user.getEmail()).isEqualTo(existingUser.get().getEmail());
        /*
         * Check that a password encoding is being done when the item is stored
         */
        assertThat(passwordEncoder).isNotNull();
        assertThat(user.getPassword()).isNotEqualTo(existingUser.get().getPassword());

    }
}

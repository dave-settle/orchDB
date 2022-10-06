/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

/**
 * The user management controller. There are some sections which have been
 * defined but are not used, as I wasn't quite how I wanted everything to work.
 * In the end, new users have to be defined by existing admins, and will have to
 * remember to change their own password. TODO: enforce must change password on
 * first login
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.Role;
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.RoleRepository;
import com.banburysymphony.orchestra.jpa.UserRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    UserDetailsManager userDetailsManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Value("${bso.default.user.name}")
    String username = "yyyy";

    @Value("${bso.default.user.password}")
    String password = "xxxx";

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String newUser(Model model) {
        log.debug("Listing users");
        Iterable<User> users = userRepository.findAll(Sort.by("email"));
        model.addAttribute("users", users);
        return "listUsers";
    }

    /**
     * Create a new user - provide a blank template and supporting objects
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/new", method = RequestMethod.GET)
    public String listUsers(Model model) {
        log.debug("Creating new user");
        User user = new User("", "");
        model.addAttribute("user", user);
        model.addAttribute("available", Arrays.asList(Role.Name.values()));
        Set<String> current = new HashSet<>();
        for(Role r: user.getRoles()) {
            current.add(r.getAuthority());
        }
        model.addAttribute("current", current);
        return "editUser";
    }

    /**
     * Bootstrap the initial admin user for first use Create the first user with
     * admin privileges, if the user does not already exist
     *
     * @param model
     * @return listUsers
     */
    @RequestMapping(path = "/bootstrap", method = RequestMethod.GET)
    public String bootstrapAdmin(Model model) {

        User user = new User(username, password);
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(Role.Name.ADMIN));
        user.setRoles(roles);
        /*
         * Using the userDetailsManager, create an entry
         */
        if (!userDetailsManager.userExists(username)) {
            log.warn("creating initial bootstrap admin user " + username);
            userDetailsManager.createUser(user);
            return "redirect:/user/list";
        }
        /*
         * Default user exists - check that they have the ADMIN role
         */
        UserDetails d = userDetailsManager.loadUserByUsername(username);
        boolean isAdmin = false;
        for (GrantedAuthority a : d.getAuthorities()) {
            if (a.getAuthority().equals(Role.Name.ADMIN.toString())) {
                isAdmin = true;
            }
        }
        if (!isAdmin) {
            /*
             * Default user exists but is not admin
             */
            log.warn("Updating " + username + " to admin role");
            User u = userRepository.findByEmail(username).orElseThrow();
            Role r = UserController.this.findRole(Role.Name.ADMIN.getId());
            u.getRoles().add(r);
            userRepository.save(u);
        }
        return "redirect:/user/list";
    }
    /**
     * Convert an array of UI role IDs into a set of Role records
     * @param ids
     * @return a set of database records
     */
    public Set<Role> findRoles(int[] ids) {
        Set<Role> result = new HashSet<>();
        for (int id : ids) {
            result.add(UserController.this.findRole(id));
        }
        return result;
    }
    /**
     * Convert a role into a database object
     * @param id
     * @return 
     */
    public Role findRole(int id) {
        Role r = Role.findRole(id);
        if (r == null)
            throw new IllegalArgumentException("role name " + id + " not found");
        return findRole(r);
    }
    /**
     * Handy mechanism for finding the database record relating to the role
     * @param r
     * @return 
     */
    public Role findRole(Role r) {
        return roleRepository.findByAuthority(r.getAuthority()).orElse(roleRepository.save(r));
    }
    
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.GET)
    public String deleteUser(Model model, @PathVariable(name = "id", required = true) int id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + id));
        log.debug("deleting user " + u);
        userRepository.delete(u);
        return "redirect:/user/list";
    }

    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editUser(Model model, @PathVariable(name = "id", required = true) int id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + id));
        log.debug("editing " + u);
        Set<String> current = new HashSet<>();
        model.addAttribute("available", Arrays.asList(Role.Name.values()));
        model.addAttribute("user", u);
        for(Role r: u.getRoles()) {
            current.add(r.getAuthority());
        }
        model.addAttribute("current", current);
        return "editUser";
    }

    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute("user") User user, @RequestParam(value = "required", required = false) int[] req_roles, BindingResult result) 
    {
        if (result.hasErrors()) {
            log.warn("cannot add user " + user + ": " + result);
            return "editUser";
        }
        log.warn("Saving user " + user + " with roles [" + Arrays.toString(req_roles) + "]");
        User dbuser = userRepository.findByEmail(user.getEmail()).orElseGet(() -> {log.warn("user " + user.getEmail() +" not found");return user;});
        dbuser.setRoles(findRoles(req_roles));
        dbuser.setFirstname(user.getFirstname());
        dbuser.setLastname(user.getLastname());
        dbuser = userRepository.save(dbuser);
        return "redirect:/user/list";
    }

    /**
     * Self-registration of new users - currently this is not supported, and new
     * users need to be added by existing admins
     *
     * @param model
     * @param name
     * @param pw
     * @param role
     * @return
     */
    //@RequestMapping(path = "/register", method = RequestMethod.POST)
    public String registerUser(Model model,
            @RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "pw", required = true) String pw,
            @RequestParam(name = "role", required = true) String role) {
        User user = new User(name, pw);
        log.debug("Creating new user " + name);
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(role));
        user.setRoles(roles);
        userDetailsManager.createUser(user);
        return "redirect:/user/list";
    }

    @GetMapping(path = "/setpassword")
    public String enterPassword(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "setpassword";
    }

    @PostMapping(path = "/setpassword")
    public String setpassword(@AuthenticationPrincipal User user, Model model,
            @RequestParam(name = "new_password", required = true) String newPassword,
            @RequestParam(name = "current_password", required = true) String oldPassword) {
        log.debug("Changing password for " + user.getEmail() + ": old = " + oldPassword + ", new = " + newPassword);
        userDetailsManager.changePassword(oldPassword, newPassword);
        return "redirect:/user/list";
    }
}

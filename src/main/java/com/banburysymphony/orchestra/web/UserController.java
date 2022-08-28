/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.web;

/**
 * The user management controller. There are some sections which have been
 * defined but are not used, as I wasn't quite how I wanted everything to work.
 * In the end, new users have to be defined by existing admins, and will have
 * to remember to change their own password.
 * TODO: enforce must change password on first login
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String newUser(Model model) {
        log.debug("Listing users");
        Iterable<User> users = userRepository.findAll(Sort.by("email"));
        model.addAttribute("users", users);
        return "listUsers";
    }
    /**
     * List all of the users
     * @param model
     * @return 
     */
    @RequestMapping(path = "/new", method = RequestMethod.GET)
    public String listUsers(Model model) {
        log.debug("Registering users");
        User user = new User("", "", null);
        model.addAttribute("user", user);
        return "editUser";
    }
    /**
     * Bootstrap the initial admin user for first use
     * Create the first user with admin privileges, if the user does not already
     * exist
     * @param model
     * @return listUsers
     */
    @RequestMapping(path = "/bootstrap", method = RequestMethod.GET)
    public String bootstrapAdmin(Model model) {
        String username = "dave.settle@osinet.co.uk";
        User user = new User(username, "pw@Corner", User.ROLE_ADMIN);
        /*
         * Using the userDetailsManager, create an entry
         */
        if(!userDetailsManager.userExists(username)) {
            log.warn("creating initial bootstrap admin user " + username);
            userDetailsManager.createUser(user);
        }
        return "redirect:/user/list";
    }
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editUser(Model model, @PathVariable(name = "id", required = true) int id) {
        Optional<User> u = userRepository.findById(id);
        if(u.isEmpty())
            throw new UnsupportedOperationException("user " + id + " not found");
        model.addAttribute("user", u.get());
        return "editUser";
    }
    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public String update(@ModelAttribute("user") User user) {
        Optional<User> u = userRepository.findByEmail(user.getEmail());
        if(u.isEmpty())
            throw new UnsupportedOperationException(user.getEmail() + " not found");
        User dbuser = u.get();
        dbuser.setRole(user.getRole());
        dbuser = userRepository.save(user);
        return "redirect:/user/list";
    }
    /**
     * Registration of new users - currently this is not supported, and new
     * users need to be added by existing admins
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
           @RequestParam(name = "role", required = true) String role)
    {
        User user = new User(name, pw, role);
        log.debug("Creating new user " + name);
        userDetailsManager.createUser(user);
        return "redirect:/user/list";
    }
    
    @RequestMapping(path = "/newpassword", method = RequestMethod.POST)
    public String setpassword(Model model, 
           @RequestParam(name = "newPassword", required = true) String newPassword,
           @RequestParam(name = "oldPassword", required = true) String oldPassword)
    {
        userDetailsManager.changePassword(oldPassword, newPassword);
        log.debug("Changing user's password");
        return "redirect:/user/list";
    }
    
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String update(Model model, @PathVariable(name = "id", required = true) int id)
    {
        log.debug("deleting user " + id);
        Optional<User> u = userRepository.findById(id);
        if(u.isPresent()) {
            log.debug("deleting user " + id);
            userRepository.deleteById(id);
        }
        return "redirect:/user/list";
    }
    
}

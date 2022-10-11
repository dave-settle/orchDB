/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.web;

/**
 *
 * @author dave.settle@osinet.co.uk on 11 Oct 2022
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/")
public class RootController {

    private static final Logger log = LoggerFactory.getLogger(RootController.class);
    @ResponseBody  
    public String index() {  
        return "You made it to the BSO application!";  
    }  

    // Login form  
    @RequestMapping("/login.html")  
    public String login() {  
        return "login.html";  
    }  

    // Login form with error  
    @RequestMapping("/login-error.html")  
    public String loginError(Model model) {  
        model.addAttribute("loginError", true);  
        return "login.html";  
    }     
    
}

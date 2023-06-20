/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Default controller
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
@Controller
public class HelloController {
    
        private static final Logger log = LoggerFactory.getLogger(HelloController.class);

	@GetMapping("/")
	public String index() {
            log.info("Redirect to concert listing");
            return "redirect:/concert/list";
	}

}

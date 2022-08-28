/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

import com.banburysymphony.orchestra.data.Venue;
import com.banburysymphony.orchestra.jpa.VenueRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Control the entry and update of Venue objects
 *
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
@Controller // This means that this class is a Controller
@RequestMapping(path = "/venue")
public class VenueController {

    @Autowired
    VenueRepository venueRepository;

    private static final Logger log = LoggerFactory.getLogger(VenueController.class);

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listVenues(Model model) {
        log.info("Listing all venues");
        Iterable<Venue> venues = venueRepository.findAll();
        model.addAttribute("venues", venues);
        return "listVenues";
    }

    @RequestMapping(path = "/edit/{id}")
    public ModelAndView getVenue(@PathVariable(name = "id", required = true) int id) {
        ModelAndView mav = new ModelAndView("listVenues");
        log.info("Editing venue " + id);
        Optional<Venue> venue = venueRepository.findById(id);
        if (!venue.isPresent()) {
            return mav;
        }
        mav.addObject("venue", venue.get());
        mav.setViewName("editVenue");
        return mav;
    }

    /**
     * Update a new or existing venue
     *
     * @param venue
     * @return
     */
    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public String saveVenue(@ModelAttribute("venue") Venue venue) {
        log.info("Save venue " + venue);
        venueRepository.save(venue);
        return("redirect:/venue/list");
    }

    /**
     * Delete a specific venue
     *
     * @param id
     * @param model
     * @return listVenues to list the remaining venues
     */
    @RequestMapping(path = "/delete/{id}")
    public String deleteVenue(@PathVariable(name = "id", required = true) int id) {
        Optional<Venue> venue = venueRepository.findById(id);
        if (venue.isPresent()) {
            log.info("Deleting " + venue);
            venueRepository.delete(venue.get());
        }
        return("redirect:/venue/list");
    }

    @RequestMapping("/new")
    public String showNewProductPage(Model model) {
        Venue venue = new Venue("");
        model.addAttribute("venue", venue);
        return "editVenue";
    }

}

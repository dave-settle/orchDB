/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

import com.banburysymphony.orchestra.data.Artist;
import com.banburysymphony.orchestra.data.Venue;
import com.banburysymphony.orchestra.jpa.ArtistRepository;
import com.banburysymphony.orchestra.jpa.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
@Controller // This means that this class is a Controller
@RequestMapping(path = "/demo") // This means URL's start with /demo (after Application path)
public class DemoController {

    @Autowired
    private ArtistRepository artistRepository;
    private VenueRepository venueRepository;

    @GetMapping(path = "/artists")
    public @ResponseBody
    Iterable<Artist> getAllArtists() {
        // This returns a JSON or XML with the users
        return artistRepository.findAll();
    }
    
    @GetMapping(path = "/venues")
    public @ResponseBody
    Iterable<Venue> getAllVenues() {
        // This returns a JSON or XML with the users
        return venueRepository.findAll();
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.web;

/**
 * Controller for suggestions
 * @author dave.settle@osinet.co.uk on 31 Oct 2022
 */
import com.banburysymphony.orchestra.data.Concert;
import com.banburysymphony.orchestra.data.Piece;
import com.banburysymphony.orchestra.data.Suggestion;
import com.banburysymphony.orchestra.data.User;
import com.banburysymphony.orchestra.jpa.ConcertRepository;
import com.banburysymphony.orchestra.jpa.SuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller // This means that this class is a web Controller
@RequestMapping(path = "/suggestion")
@ConfigurationProperties(prefix = "bso.suggestion")
@ConfigurationPropertiesScan

public class SuggestionController {

    private static final Logger log = LoggerFactory.getLogger(SuggestionController.class);

    @Autowired
    SuggestionRepository suggestionRepository;
    
    @GetMapping(path = "/new")
    public String createSuggestion(@AuthenticationPrincipal User user, Model model) {
        Piece piece = new Piece("", "");
        Suggestion suggestion = new Suggestion(user, piece, "");
        model.addAttribute("suggestion", suggestion);
        return "editSuggestion";
    }
    
    /**
     * List all of the suggestion
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listSuggestions(Model model) {
        log.info("Listing all suggestions");
        model.addAttribute("concerts", suggestionRepository.findAll(Sort.by(Sort.Direction.DESC, "date")));
        return "listConcerts";
    }
    /**
     * Edit a specific suggestion
     * @param model
     * @param id
     * @return 
     */
    @GetMapping(path = "/edit/{id}")
    public String editSuggestion(Model model, @PathVariable(name = "id", required = true) int id) {
        Suggestion suggestion = suggestionRepository.findById(id).
                orElseThrow(() -> {
                    return new UnsupportedOperationException("suggestion #" + id + " not found");
                });
        model.addAttribute("suggestion", suggestion);
        return "editSuggestion";
    }
    /**
     * Delete a specific suggestion
     * @param model
     * @param id
     * @return 
     */
    @GetMapping(path = "/delete/{id}")
    public String deleteSuggestion(Model model, @PathVariable(name = "id", required = true) int id) {
        Suggestion suggestion = suggestionRepository.findById(id).
                orElseThrow(() -> {
                    return new UnsupportedOperationException("suggestion #" + id + " not found");
                });
        suggestionRepository.delete(suggestion);
        return "redirect:/suggestion/list";
    }
}

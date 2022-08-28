/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.web;

/**
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import com.banburysymphony.orchestra.data.Concert;
import com.banburysymphony.orchestra.data.Engagement;
import com.banburysymphony.orchestra.jpa.ConcertRepository;
import com.banburysymphony.orchestra.jpa.EngagementRepository;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/engagement")
public class EngagementController {

    private static final Logger log = LoggerFactory.getLogger(EngagementController.class);
    
    @Autowired
    EngagementRepository engagementRepository;
    
    @Autowired
    ConcertRepository concertRepository;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listPieces(Model model) {
        log.debug("Listing all engagements");
        Iterable<Concert> concerts = concertRepository.findAll(Sort.by("date"));
        List<Engagement> engagements = getEngagements(concerts);
        model.addAttribute("engagements", engagements);
        return "listEngagements";
    }
    @RequestMapping(path = "/listBySoloist", method = RequestMethod.GET)
    public String listConcertsBySoloist(Model model, @RequestParam(name = "id", required = true) Integer id) {
        log.info("Listing all engagements containing artist " + id);
        List<Concert> concerts = concertRepository.findAllBySoloist(id, Sort.by("date"));
        List<Engagement> engagements = getEngagements(concerts);
        model.addAttribute("concerts", concerts);
        model.addAttribute("engagements", engagements);        return "listConcerts";
    }
    
    public List<Engagement> getEngagements(Iterable<Concert> concerts) {
        List<Engagement> engagements = new LinkedList<>();
        for(Concert c: concerts) {
            for(Engagement e: c.getSoloists()) {
                log.debug("found engagement " + e);
                e.setConcert(c);
                engagements.add(e);
            }
        }
        return engagements;
    }
}

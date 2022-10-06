/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

/**
 * Controller to manage the plans for future concerts
 *
 * @author dave.settle@osinet.co.uk on 2 Oct 2022
 */
import com.banburysymphony.orchestra.data.Artist;
import com.banburysymphony.orchestra.data.Concert;
import com.banburysymphony.orchestra.data.Engagement;
import com.banburysymphony.orchestra.data.Piece;
import com.banburysymphony.orchestra.data.Venue;
import com.banburysymphony.orchestra.jpa.ArtistRepository;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/plan")
public class PlanController extends ConcertController {

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    /**
     * Default conductor
     */
    @Value("${bso.default.conductor.name:Paul Willett}")
    private String conductorName;
    /**
     * Default venue
     */
    @Value("${bso.default.venue.name:St Peter's Church, Deddington}")
    private String venueName;

    /**
     * Create a new planned concert using the defaults
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/new")
    public String newPlan(Model model) {
        Calendar future = Calendar.getInstance();
        future.set(Calendar.MONTH, future.get(Calendar.MONTH) + 1);
        Date nextMonth = new Date(future.getTime().getTime());
        Artist conductor = artistRepository.findByName(getConductorName()).orElseThrow();
        Venue venue = venueRepository.findByName(getVenueName()).orElseThrow();
        Concert concert = new Concert(venue, nextMonth, conductor);
        concert.setConductor(conductor);
        concert = concertRepository.save(concert);
        model.addAttribute("concert", concert);
        editSupport(model);
        return "redirect:/plan/edit/" + concert.getId(); 
    }

    @GetMapping(path = "/edit/{id}")
    public String editPlan(Model model, @PathVariable(name = "id", required = true) int id) {
        Concert concert = concertRepository.findById(id).
                orElseThrow(() -> {
                    return new UnsupportedOperationException("concert id " + id + " not found");
                });
        model.addAttribute("concert", concert);
        return editSupport(model);
    }

    /**
     * Shared method to collecting supporting information for concert plans
     *
     * @param model
     * @return
     */
    protected String editSupport(Model model) {
        /*
         * Provide lists of venues and potential conductors
         */
        Iterable venues = venueRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        model.addAttribute("venues", venues);
        model.addAttribute("artists", artistRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("conductors", artistRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))); // TODO: find all conductors
        /*
         * Provide default list of composers & skills
         */
        Set<String> composers = new TreeSet<>();
        for (Piece p : pieceRepository.findAll()) {
            composers.add(p.getComposer());
        }
        model.addAttribute("composers", composers);
        Set<String> skills = new TreeSet<>();
        for(Concert c: concertRepository.findAll()) {
            for(Engagement e: c.getSoloists())
                skills.add(e.getSkill());
        }
        model.addAttribute("skills", skills);
        return "editPlan";
    }

    /**
     * Save a concert with any updates which have been made
     *
     * @param concert
     * @param result
     * @return
     */
    @PostMapping(path = "/save")
    public String update(@Valid @ModelAttribute("concert") Concert concert, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("cannot save concert " + concert + ": " + result);
            return "editUser";
        }
        log.warn("Saving concert plan " + concert);
        concertRepository.save(concert);
        return "redirect:/plan/list";
    }

    @GetMapping(path = "/list")
    @Override
    public String listConcerts(Model model) {
        log.info("Listing all planned concerts");
        return super.listConcerts(model);
    }

    /**
     * Add a new piece to an existing concert plan
     *
     * @param model
     * @param planId
     * @param composer
     * @param title
     * @return
     */
    @PostMapping(path = "/addPiece")
    public String addPiece(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "composer", required = true) String composer,
            @RequestParam(name = "title", required = true) String title) {
        log.debug("Adding new piece for concert#" + planId + ", composer=[" + composer + "], title=[" + title + "]");

        Concert plan = concertRepository.findById(planId).orElseThrow();
        Piece piece = pieceRepository.checkTitle(pieceRepository.findAllByComposerOrderByTitleAsc(composer), title)
                .orElseGet(() -> {
                    return pieceRepository.save(new Piece(composer, title));
                });
        plan.getPieces().add(piece);
        log.debug("Added " + piece);
        concertRepository.save(plan);
        model.addAttribute("concert", plan);
        editSupport(model);
        return "redirect:/plan/edit/" + plan.getId();
    }

    /**
     * Remove a piece from a concert
     *
     * @param model
     * @param planId the ID of the concert
     * @param pieceId the ID of the piece
     * @return to edit the piece
     */
    @GetMapping(path = "removePiece")
    public String removePiece(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "pieceId", required = true) int pieceId) {
        log.debug("removing piece#" + pieceId + " from concert#" + planId);
        Concert plan = concertRepository.findById(planId).orElseThrow();
        Iterator<Piece> it = plan.getPieces().iterator();
        while (it.hasNext()) {
            Piece p = it.next();
            if (p.getId() == pieceId) {
                it.remove();
            }
        }
        concertRepository.save(plan);
        model.addAttribute("concert", plan);
        editSupport(model);
        return "redirect:/plan/edit/" + plan.getId();
    }

    @PostMapping(path = "/addEngagement")
    public String addSoloist(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "soloist", required = true) String soloist,
            @RequestParam(name = "skill", required = true) String skill) {
        log.debug("Adding new piece for concert#" + planId + ", soloist=[" + soloist + "], skill=[" + skill + "]");

        Concert plan = concertRepository.findById(planId).orElseThrow();
        Artist a = artistRepository.findByName(soloist).orElseGet(() -> {return artistRepository.save(new Artist(soloist));});
        Engagement e = new Engagement(a, skill);
        engagementRepository.save(e);
        plan.getSoloists().add(e);
        concertRepository.save(plan);
        editSupport(model);
        return "redirect:/plan/edit/" + plan.getId();
    }

    @GetMapping(path = "/removeEngagement")
    public String removeEngagement(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "engagementId", required = true) int engagementId) {
        log.debug("removing piece#" + engagementId + " from concert#" + planId);
        Concert plan = concertRepository.findById(planId).orElseThrow();
        Iterator<Engagement> it = plan.getSoloists().iterator();
        while (it.hasNext()) {
            Engagement e = it.next();
            if (e.getId() == engagementId) {
                it.remove();
            }
        }
        concertRepository.save(plan);
        editSupport(model);
        return "redirect:/plan/edit/" + plan.getId();
    }
    /**
     * As we are now planning future concerts, the list of concerts returned by
     * this controller should be restricted to concerts which have not yet
     * happened
     *
     * @param concerts a list of all concerts
     * @return concerts whose date is in the future
     */
    @Override
    protected List<Concert> filter(Iterable<Concert> concerts) {
        Date now = new Date(System.currentTimeMillis());
        Predicate<Concert> byDate = concert -> concert.getDate().after(now);
        return StreamSupport.stream(concerts.spliterator(), false).filter(byDate).collect(Collectors.toList());
    }

    /**
     * @return the conductorName
     */
    public String getConductorName() {
        return conductorName;
    }

    /**
     * @param conductorName the conductorName to set
     */
    public void setConductorName(String conductorName) {
        this.conductorName = conductorName;
    }

    /**
     * @return the venueName
     */
    public String getVenueName() {
        return venueName;
    }

    /**
     * @param venueName the venueName to set
     */
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}

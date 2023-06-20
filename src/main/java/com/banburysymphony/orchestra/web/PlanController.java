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
import jakarta.validation.Valid;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Edit a planned concert - now moved to superclass
     * 
     * @param model
     * @param id the ID of the concert
     * @return the template for editing
     */
    @GetMapping(path = "/edit/{id}")
    @Override
    public String editPlan(Model model, @PathVariable(name = "id", required = true) int id) {
        return super.editPlan(model, id);
    }
    /**
     * Save a concert with any updates which have been made
     *
     * @param concert
     * @param result
     * @return
     */
    @PostMapping(path = "/save")
    @Override
    public String update(@Valid @ModelAttribute("concert") Concert concert, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("cannot save concert " + concert + ": " + result);
            return "editUser";
        }
        log.info("Saving concert plan " + concert);
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
        return "redirect:/plan/edit/" + plan.getId();
    }
    
    @GetMapping(path = "/movePieceUp")
    public String movePieceUp(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "pieceIndex", required = true) int pieceIndex) {
        log.debug("movePieceUp(planId=" + planId + ", pieceIndex=" + pieceIndex + ")");
        Concert plan = concertRepository.findById(planId).orElseThrow();
        /*
         * Implementation restriction: we need to work on a copy of the list
         * or it won't get saved properly
         */
        List<Piece> pieceList = plan.getPieces().stream().collect(Collectors.toList());
        if((pieceIndex <= 0) || (pieceIndex > pieceList.size()))
            throw new IllegalArgumentException("invalid pieceIndex: " + pieceIndex);
        Collections.swap(pieceList, pieceIndex, pieceIndex - 1);
        plan.setPieces(pieceList);
        concertRepository.save(plan);
        return "redirect:/plan/edit/" + plan.getId();
    }
    @GetMapping(path = "/movePieceDown")
    public String movePieceDown(Model model,
            @RequestParam(name = "planId", required = true) int planId,
            @RequestParam(name = "pieceIndex", required = true) int pieceIndex) {
        log.debug("movePieceUp(planId=" + planId + ", pieceIndex=" + pieceIndex + ")");
        Concert plan = concertRepository.findById(planId).orElseThrow();
        /*
         * Implementation restriction: we need to work on a copy of the list
         * or it won't get saved properly
         */
        List<Piece> pieceList = plan.getPieces().stream().collect(Collectors.toList());
        if((pieceIndex < 0) || (pieceIndex >= pieceList.size()))
            throw new IllegalArgumentException("invalid pieceIndex: " + pieceIndex);
        Collections.swap(pieceList, pieceIndex, pieceIndex + 1);
        plan.setPieces(pieceList);
        concertRepository.save(plan);
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
     * Planned concerts should be ordered in reverse direction to concerts
     * @return ordering for plans
     */
    @Override
    protected Sort sortOrder() {
        return Sort.by(Sort.Direction.ASC, "date");
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

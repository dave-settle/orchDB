/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

import com.banburysymphony.orchestra.data.Piece;
import com.banburysymphony.orchestra.jpa.PieceRepository;
import java.math.BigInteger;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author dave.settle@osinet.co.uk on 19 Aug 2022
 */
@Controller // This means that this class is a Controller
@RequestMapping(path = "/piece")
public class PieceController {

    private static final Logger log = LoggerFactory.getLogger(PieceController.class);

    @Autowired
    PieceRepository pieceRepository;
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listPieces(Model model) {
        log.debug("Listing all pieces");
        Iterable<Piece> pieces;
        pieces = pieceRepository.findAll(Sort.by(Sort.Direction.ASC, "composer", "title"));
        model.addAttribute("pieces", pieces);
        /*
         * For further information, get the count of times played
         */
        log.debug("Finding frequency");
        List<Object[]> played = pieceRepository.countPiecesByConcerts();
        /*
         * And make this into maps
         */
        Map<Integer, Long> frequency = new HashMap<>();
        Map<Integer, String> lastPlayed = new HashMap<>();
        for (Object[] f : played) {
            //log.trace("played " + f + ": [0]=" + f[0].getClass().getName() + ", [1]=" + f[1].getClass().getName() + ", [3]=" + f[3].getClass().getName());
            BigInteger c = (BigInteger) f[1];
            Integer id = (Integer) f[0];
            frequency.put(id, c.longValue());
            Date lp = (Date) f[3];
            /*
             * DateFormat.getDateInstance() produces a more human-readable date, but not so sortable
             */
            lastPlayed.put(id, sdf.format(lp));
            log.debug("played " + f[2] + " " + c + " times, last was " + lp);
        }
        model.addAttribute("frequency", frequency);
        model.addAttribute("lastPlayed", lastPlayed);
        return "listPieces";
    }
    /**
     * List pieces by name of composer
     * @param model
     * @param name
     * @return 
     */
    @RequestMapping(path = "/listByComposer", method = RequestMethod.GET)
    public String listPiecesByComposer(Model model, @RequestParam(name = "name", required = true) String name) {
        log.debug("Listing all pieces by composer " + name);
        Iterable<Piece> pieces;
        pieces = pieceRepository.findAllByComposer(name, Sort.by(Sort.Direction.ASC, "title"));
        model.addAttribute("pieces", pieces);
        return "listPieces";
    }

    @RequestMapping(path = "/edit/{id}")
    public ModelAndView getPiece(@PathVariable(name = "id", required = true) int id) {
        ModelAndView mav = new ModelAndView("listPieces");
        log.info("Editing piece " + id);
        Optional<Piece> piece = pieceRepository.findById(id);
        if (!piece.isPresent()) {
            return mav;
        }
        mav.addObject("piece", piece.get());
        mav.setViewName("editPiece");
        return mav;
    }

    /**
     * Update a new or existing piece For a potentially new piece, we need to
     * map to an existing one if there is one
     *
     * @param piece
     * @return the next view to display
     */
    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public String savePiece(@ModelAttribute("piece") Piece piece) {
        log.info("Save piece " + piece);
        Optional<Piece> existing = pieceRepository.checkTitle(pieceRepository.findAllByComposerOrderByTitleAsc(piece.getComposer()), piece.getTitle());
        if (existing.isPresent()) {
            Piece existingPiece = existing.get();
            log.info("found existing piece " + existingPiece);
            if (existingPiece.getTitle().length() <= piece.getTitle().length()) {
                existingPiece.setTitle(piece.getTitle().trim());
            }
            existingPiece.setComposer(piece.getComposer().trim());
            existingPiece.setSubtitle(piece.getSubtitle().trim());
            log.info("Updating existing piece " + existingPiece);
            pieceRepository.save(existingPiece);
        } else {
            log.info("saving new piece " + piece);
            pieceRepository.save(piece);
        }
        return ("redirect:/piece/list");
    }

    /**
     * Delete a specific piece
     *
     * @param id
     * @param model
     * @return listPieces to list the remaining pieces
     */
    @RequestMapping(path = "/delete/{id}")
    public String deletePiece(@PathVariable(name = "id", required = true) int id) {
        Optional<Piece> piece = pieceRepository.findById(id);
        if (piece.isPresent()) {
            log.info("Deleting " + piece);
            pieceRepository.delete(piece.get());
        }
        return ("redirect:/piece/list");
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping("/new")
    public String showNewProductPage(Model model) {
        Piece piece = new Piece("", "");
        model.addAttribute("piece", piece);
        return "editPiece";
    }

}

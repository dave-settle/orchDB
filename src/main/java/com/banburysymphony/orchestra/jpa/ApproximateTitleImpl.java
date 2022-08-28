/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Piece;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author dave.settle@osinet.co.uk on 19 Aug 2022
 */
public class ApproximateTitleImpl implements ApproximateTitle {
    
    private static final Logger log = LoggerFactory.getLogger(ApproximateTitleImpl.class);
    
    Map<Integer, String> pieces = new HashMap<Integer, String>();
    /**
     * Perform an approximate lookup on title
     * We need to map "Symphony No. 9" to "Symphony No. 9 in E minor Op. 95"
     * and vice versa
     * @param title
     * @return an existing piece if the title seems to match
     */
    @Override
    public Optional<Piece> checkTitle(Iterable<Piece> pieces, String title) {
        String lookFor = title.toLowerCase();
        for(Piece piece: pieces) {
            String pieceTitle = piece.getTitle().toLowerCase();
            if(pieceTitle.startsWith(lookFor) || lookFor.startsWith(pieceTitle)) {
                log.debug("matching (" + title + ") to " + piece);
                return Optional.of(piece);
            }
        }
        /*
         * Not found
         */
        log.debug("title (" + title + ") not found");
        return Optional.empty();
    }
    
    

}

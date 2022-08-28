/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Piece;
import java.util.Optional;

/**
 * Definition of search by approximate title
 * @author dave.settle@osinet.co.uk on 19 Aug 2022
 */
public interface ApproximateTitle {
    
    /**
     * F
     * @param pieces
     * @param title
     * @return
     */
    public Optional<Piece> checkTitle(Iterable<Piece> pieces, String title);

}

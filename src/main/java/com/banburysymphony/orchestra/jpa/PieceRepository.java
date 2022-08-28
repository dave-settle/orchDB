/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Piece;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Retrieve Piece records from the database
 * The additional interface provides a mechanism for finding pieces by
 * approximate titles
 * @author dave.settle@osinet.co.uk on 19-Aug-2022
 */
public interface PieceRepository extends PagingAndSortingRepository<Piece, Integer>, ApproximateTitle {
    
    public List<Piece> findAllByComposer(String composer, Sort sort);
    public List<Piece> findAllByComposerOrderByTitleAsc(String composer);
    
    public List<Piece> findAllByComposerAndTitle(String composer, String title);
    
    @Query(value = "SELECT p.id, count(*), p.title, max(c.date) FROM pieces p, concert_piece cp, concerts c " + 
            "WHERE c.id = cp.fk_concert and cp.fk_piece = p.id GROUP BY p.id", nativeQuery = true)
    public List<Object[]> countPiecesByConcerts();
}

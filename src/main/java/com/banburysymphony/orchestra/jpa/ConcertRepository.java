/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Artist;
import com.banburysymphony.orchestra.data.Concert;
import com.banburysymphony.orchestra.data.Piece;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Access Concert records
 * @author dave.settle@osinet.co.uk on 20 Aug 2022
 */
public interface ConcertRepository extends CrudRepository<Concert, Integer>, PagingAndSortingRepository<Concert, Integer> {
    
    public Optional<Concert> findByDate(Date date);
    /**
     * Find all the concerts where the given piece was played
     * @param piece
     * @param sort
     * @return 
     */
    public List<Concert> findAllByPieces(Piece piece, Sort sort);

    public List<Concert> findAllByConductor(Artist conductor, Sort sort);
    /*
     * List concerts where a piece by a given composer was played
     */
    @Query("select distinct c from Concert c join c.pieces p where p.composer = ?1")
    public List<Concert> findAllByComposer(String composer, Sort sort);
    /*
     * List concerts where a given soloist was engaged
     */
    @Query("select distinct c from Concert c join c.soloists s where s.artist.id = ?1")
    public List<Concert> findAllBySoloist(Integer id, Sort sort);
    /*
     * List concerts where a given instrument was played
     */
    @Query("select distinct c from Concert c join c.soloists s where s.skill = ?1")
    public List<Concert> findAllBySkill(String skill, Sort sort);

}

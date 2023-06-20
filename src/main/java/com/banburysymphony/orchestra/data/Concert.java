/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.sql.Date;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Top-level entity representing a concert
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
@Entity
@Table(name = "concerts")
public class Concert {

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    protected Concert() {};
    
    public Concert(Venue venue, Date held, Artist conductor) {
        this.venue = venue;
        this.date = held;
        this.conductor = conductor;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    
    /**
     * The venue where the concert was held
     */
    @ManyToOne
    private Venue venue;
    
    private Date date;

    @OrderColumn
    @ManyToMany
    @JoinTable(name = "concert_piece", 
            joinColumns = { @JoinColumn(name = "fk_concert") }, 
            inverseJoinColumns = { @JoinColumn(name = "fk_piece") })
    private List<Piece> pieces = new LinkedList<Piece>();
    
    @ManyToOne
    private Artist conductor;
        
    @OneToMany
    private Set<Engagement> soloists = new TreeSet<>();
    
    private Boolean programmeAvailable = false;
    
    @Column(length = 1024)
    private String notes;
    
    //private List<Engagement> players;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the conductor
     */
    public Artist getConductor() {
        return conductor;
    }

    /**
     * @param conductor the conductor to set
     */
    public void setConductor(Artist conductor) {
        this.conductor = conductor;
    }

    /**
     * @return the pieces
     */
    public List<Piece> getPieces() {
        return pieces;
    }

    /**
     * @param pieces the pieces to set
     */
    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }

    /**
     * @return the soloists
     */
    public Set<Engagement> getSoloists() {
        return soloists;
    }

    /**
     * @param soloists the soloists to set
     */
    public void setSoloists(Set<Engagement> soloists) {
        this.soloists = soloists;
    }
    @Override
    public String toString() {
        return "Concert(date=" + DateFormat.getDateInstance().format(date) + ", venue=" + venue + ", conductor=" + getConductor() + ")";
    }

    /**
     * @return the programmeAvailable
     */
    public Boolean getProgrammeAvailable() {
        return programmeAvailable;
    }

    /**
     * @param programmeAvailable the programmeAvailable to set
     */
    public void setProgrammeAvailable(Boolean programmeAvailable) {
        this.programmeAvailable = programmeAvailable;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * A piece of music being played, such as an overture or a symphony
 * Any given piece might be played in a number of concerts
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
@Entity
@Table(name = "pieces")
public class Piece {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 
    
    private String composer;
    private String title;
    private String subtitle;   
    
    protected Piece() {}
    
    public Piece(String composer, String title) {
        this(composer, title, null);
    }
            
    public Piece(String composer, String title, String subtitle) {
        this.composer = composer;
        this.title = title;
        this.subtitle = subtitle;
    }    

    /**
     * @return the composer
     */
    public String getComposer() {
        return composer;
    }

    /**
     * @param composer the composer to set
     */
    public void setComposer(String composer) {
        this.composer = composer;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "Piece(id=" + id + ", composer=" + composer + ", title=" + title + ", subtitle=" + subtitle + ")";
    }
}

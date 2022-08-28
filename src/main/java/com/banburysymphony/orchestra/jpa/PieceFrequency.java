/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

/**
 *
 * @author dave.settle@osinet.co.uk on 21 Aug 2022
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PieceFrequency {

    private static final Logger log = LoggerFactory.getLogger(PieceFrequency.class);
    
    private Long id;
    private Long frequency;
    private String title;
    
    public PieceFrequency(Long id, Long frequency, String title) {
        this.id = id;
        this.frequency = frequency;
        this.title = title;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the frequency
     */
    public Long getFrequency() {
        return frequency;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    @Override
    public String toString() {
        return "PieceFrequency(id=" + id + ", frequency=" + frequency + ", name=" + title;
    }
}

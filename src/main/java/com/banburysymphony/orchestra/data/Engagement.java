/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



/**
 * An engagement of an artist to play in a particular concert
 * Required as some artists are engaged to do a number of things
 * @author dave.settle@osinet.co.uk on 11 Aug 2022
 */
@Entity
@Table(name = "engagements")
public class Engagement {

    /**
     * @return the concert
     */
    public Concert getConcert() {
        return concert;
    }

    /**
     * @param concert the concert to set
     */
    public void setConcert(Concert concert) {
        this.concert = concert;
    }
    protected Engagement() {}
    
    public Engagement(Artist artist, String skill) {
        this.artist = artist;
        this.skill = skill;
        
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * The artist being engaged
     */
    @ManyToOne
    private Artist artist;
    /**
     * The skill that the artist was engaged for
     */
    private String skill;
    /**
     * The concert for which the engagement was made
     */
    @ManyToOne
    private Concert concert;
    

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the artist
     */
    public Artist getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     * @return the skill
     */
    public String getSkill() {
        return skill;
    }

    /**
     * @param skill the skill to set
     */
    public void setSkill(String skill) {
        this.skill = skill;
    }
    @Override
    public String toString() {
        return "Engagement(artist=" + artist + ", skill=" + skill + ", concert=" + concert + ")";
    }
}

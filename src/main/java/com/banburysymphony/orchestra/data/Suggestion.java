/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

/**
 *
 * @author dave.settle@osinet.co.uk on 19 Oct 2022
 */
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    private static final Logger log = LoggerFactory.getLogger(Suggestion.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id = -1;
    /**
     * The piece which has been suggested
     */
    @ManyToOne
    private Piece piece = null;
    /**
     * The user who suggested it
     */
    @ManyToOne
    private User user;
    /**
     * The reason for suggesting the piece
     */
    @Column(length = 1024)
    private String reason;
    /**
     * Any comments which have been made about the suggestion
     */
    @OrderColumn
    @ManyToMany
    @JoinTable(name = "suggestion_comments",
          joinColumns = {@JoinColumn(name = "comment_fk")},
          inverseJoinColumns = {@JoinColumn(name = "suggestion_fk")}    )    
    private List<Comment> comments = new LinkedList<Comment>();
    /**
     * The state that the suggestion is currently in
     */
    private int state;

    protected Suggestion() {}
    /**
     * Public constructor
     * @param user
     * @param piece
     * @param reason 
     */
    public Suggestion(User user, Piece piece, String reason) {
        this.user = user;
        this.piece = piece;
        this.reason = reason;
    }

    /**
     * @return the piece
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * @param piece the piece to set
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * @return the comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
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

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}

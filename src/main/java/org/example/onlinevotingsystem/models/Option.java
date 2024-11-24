package org.example.onlinevotingsystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OptionID")
    private int optionId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "VoteCount")
    private int voteCount;

    @Column(name = "VotePercentage")
    private double votePercentage;

    @ManyToOne
    @JoinColumn(name = "PollID", nullable = false)
    private Poll poll;

	 

}

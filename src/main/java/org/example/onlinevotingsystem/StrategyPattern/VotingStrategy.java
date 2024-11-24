package org.example.onlinevotingsystem.StrategyPattern;

import org.example.onlinevotingsystem.models.Poll;

public interface VotingStrategy {
    
    PollResult calculateResults(Poll poll);
}	
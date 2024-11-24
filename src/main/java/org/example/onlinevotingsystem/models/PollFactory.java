package org.example.onlinevotingsystem.models;


public interface PollFactory {
    Poll createPoll(PollRequest request);
}
package org.example.onlinevotingsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.example.onlinevotingsystem.models.Poll;

@Repository
public interface PollRepository extends JpaRepository<Poll, Integer> {
}

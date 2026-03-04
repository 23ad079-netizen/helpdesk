package com.example.helpdeskapi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helpdeskapi.entity.TicketEntity;
import com.example.helpdeskapi.repository.TicketRepository;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {
    @Autowired
    private TicketRepository repo;
    @GetMapping()
    public List<TicketEntity> gettickets(){
        return repo.findAll();
    }
    @GetMapping("/{id}")
    public TicketEntity getEntity(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Invalid id"));
    }

    @PostMapping()
    public TicketEntity puttickets(@RequestBody TicketEntity request){
        return repo.save(request);
    }
    @PutMapping("/{request}")
    public TicketEntity updatetickets(@PathVariable Long request,@RequestBody TicketEntity update){
        TicketEntity current=repo.findById(request).orElseThrow(()-> new RuntimeException("Invalid id"));
        current.setIssue(update.getIssue());
        current.setDescription(update.getDescription());
        current.setStatus(update.getStatus());
        return repo.save(current);
    }
    @DeleteMapping("/{request}")
    public void deletetickets(@PathVariable Long request){
        repo.deleteById(request);
    }
}
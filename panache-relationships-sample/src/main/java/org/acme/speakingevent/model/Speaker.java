package org.acme.speakingevent.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Speaker extends PanacheEntity {

    // pulic fields

    @ManyToMany(mappedBy = "speakers")
    public Set<Event> events = new HashSet<>();
}

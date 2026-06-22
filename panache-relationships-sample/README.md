# Panache Active Record JPA Relationship Sample

This sample gives you three entities using Panache Active Record style:

- OneToMany: Venue -> Event
- ManyToOne: Event -> Venue
- ManyToMany: Event <-> Speaker

## Files

- src/main/java/org/acme/speakingevent/model/Venue.java
- src/main/java/org/acme/speakingevent/model/Event.java
- src/main/java/org/acme/speakingevent/model/Speaker.java

## Seed example (inside a transactional method)

```java
Venue cityHall = Venue.create("City Hall Auditorium", "Melbourne");
Venue techHub = Venue.create("Tech Hub Stage", "Sydney");

Speaker alice = Speaker.create("Alice Chen", "alice@example.com");
Speaker bob = Speaker.create("Bob Singh", "bob@example.com");

Event quarkusTalk = Event.create("Quarkus in Production", LocalDate.of(2026, 7, 10));
Event cloudNative = Event.create("Cloud Native Patterns", LocalDate.of(2026, 7, 11));

cityHall.addEvent(quarkusTalk);
techHub.addEvent(cloudNative);

quarkusTalk.addSpeaker(alice);
quarkusTalk.addSpeaker(bob);
cloudNative.addSpeaker(bob);

cityHall.persist();
techHub.persist();
alice.persist();
bob.persist();
```

## Query reminders

```java
List<Event> melbourneEvents = Event.list("venue.city", "Melbourne");
List<Speaker> speakers = Speaker.listAll();
List<Venue> venues = Venue.listAll();
```

## Memory tips

- Owner side writes FK: Event owns ManyToOne with venue_id.
- Inverse side uses mappedBy: Venue.events and Speaker.events.
- ManyToMany owner is Event because it defines JoinTable.

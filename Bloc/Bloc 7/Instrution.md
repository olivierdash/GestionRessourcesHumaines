# Test pour le truc : 
commentez cette partie et le service qui va avec 
```java
    @Column(name = "jours_hebdomadaires")
    private Integer joursHebdomadaires;
```

problème côté service RegimeTravailService.java
```java
regime.setJoursHebdomadaires(form.getJoursHebdomadaires());
```
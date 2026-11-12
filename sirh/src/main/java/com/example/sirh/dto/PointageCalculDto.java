package com.example.sirh.dto;

public class PointageCalculDto {

    private int minutesPresence;
    private int minutesPause;
    private int minutesTravaillees;
    private int minutesRetard;
    private int minutesSupplementaires;
    private String anomalie;

    public PointageCalculDto() {}

    public PointageCalculDto(int minutesPresence, int minutesPause, int minutesTravaillees,
                             int minutesRetard, int minutesSupplementaires, String anomalie) {
        this.minutesPresence = minutesPresence;
        this.minutesPause = minutesPause;
        this.minutesTravaillees = minutesTravaillees;
        this.minutesRetard = minutesRetard;
        this.minutesSupplementaires = minutesSupplementaires;
        this.anomalie = anomalie;
    }

    public int getMinutesPresence() { return minutesPresence; }
    public void setMinutesPresence(int minutesPresence) { this.minutesPresence = minutesPresence; }

    public int getMinutesPause() { return minutesPause; }
    public void setMinutesPause(int minutesPause) { this.minutesPause = minutesPause; }

    public int getMinutesTravaillees() { return minutesTravaillees; }
    public void setMinutesTravaillees(int minutesTravaillees) { this.minutesTravaillees = minutesTravaillees; }

    public int getMinutesRetard() { return minutesRetard; }
    public void setMinutesRetard(int minutesRetard) { this.minutesRetard = minutesRetard; }

    public int getMinutesSupplementaires() { return minutesSupplementaires; }
    public void setMinutesSupplementaires(int minutesSupplementaires) { this.minutesSupplementaires = minutesSupplementaires; }

    public String getAnomalie() { return anomalie; }
    public void setAnomalie(String anomalie) { this.anomalie = anomalie; }
}
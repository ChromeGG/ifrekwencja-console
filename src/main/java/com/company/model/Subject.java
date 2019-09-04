package com.company.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Subject {


    private String name;

    //positive
    private int obecny; //obecnosc_0
    private int zwolnionyObecny; //obecnosc_9

    //negative
    private int nieobecnyUsprawiedliwiony; //obecnosc_1
    private int nieobecny; //obecnosc_3
    private int zwolnienie; //obecnosc_4

    //neutral
    private int spozniony; //obecnosc_2
    private int nieOdbylySie; // obecnosc_5
}

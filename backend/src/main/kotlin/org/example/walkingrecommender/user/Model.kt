package org.example.walkingrecommender.user

data class Preferences(
    val greenery: Double = 0.0,
    val water: Double = 0.0,
    val traffic: Double = 0.0,
    val noise: Double = 0.0,
    val historicArchitecture: Double = 0.0,
    val modernArchitecture: Double = 0.0,
    val commercialActivity: Double = 0.0,
    val grittiness: Double = 0.0,
    val pedestrianComfort: Double = 0.0,
)
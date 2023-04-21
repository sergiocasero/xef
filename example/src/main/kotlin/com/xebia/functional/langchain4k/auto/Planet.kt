package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.agents.wikipedia
import kotlinx.serialization.Serializable

@Serializable
data class Planet(val name: String, val distanceFromSun: Double, val moons: List<Moon>)

@Serializable
data class Moon(val name: String, val distanceFromPlanet: Double)

suspend fun main() {
    val earth: Planet = ai("Information about Earth and its moon.", auto = true, agents = listOf(Agent.wikipedia()))
    val mars: Planet = ai("Information about Mars and its moons.", auto = true, agents = listOf(Agent.wikipedia()))

    fun planetInfo(planet: Planet): String {
        return """${planet.name} is ${planet.distanceFromSun} million km away from the Sun.
            |It has the following moons:
            |${planet.moons.joinToString("\n") { "  - ${it.name}: ${it.distanceFromPlanet} km away from ${planet.name}" }}
            """.trimMargin()
    }

    println("Celestial bodies information:\n\n${planetInfo(earth)}\n\n${planetInfo(mars)}")
}
package org.oreo.nodes_kd.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.oreo.nodeskd.NodesKd
import phonon.nodes.Nodes.getResident

class KillListener(private val plugin: NodesKd) : Listener {

    @EventHandler
    fun onPlayerKill(e: EntityDeathEvent) {

        val killer = e.entity.killer
        val killed = e.entity

        if (killer == null || killed !is Player) return

        val teamKills = plugin.config.getBoolean("count-nation-team-kills")

        if (teamKills) {
            addKill(killer)
            addDeath(killed)
            return
        }

        val killedResidentNation = getResident(killed)?.nation ?: return
        val killerResidentNation = getResident(killer)?.nation ?: return


        if (killedResidentNation == killerResidentNation) return

        addKill(killer)
        addDeath(killed)
    }

    /**
     * Adds a kill to the players data
     */
    private fun addKill(player: Player){

        val playerData = NodesKd.getPlayerData(player)

        if (playerData == null) {
            NodesKd.createPlayerEntry(player, 1, 0)
            return
        }

        playerData.kills++
    }

    /**
     * Adds a death to the players data
     */
    private fun addDeath(player: Player){

        val playerData = NodesKd.getPlayerData(player)

        if (playerData == null) {
            NodesKd.createPlayerEntry(player, 0, 1)
            return
        }

        playerData.deaths++
    }

}
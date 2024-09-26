package org.oreo.nodeskd.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.oreo.nodeskd.NodesKd
import org.oreo.nodeskd.NodesKd.Companion.killsList
import org.oreo.nodeskd.data.PlayerKillData
import java.util.*

class KdCommands (private val plugin: NodesKd) : CommandExecutor, TabCompleter {

    override fun onCommand(commandSender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        if (commandSender !is Player){
            commandSender.sendMessage("${ChatColor.RED}Only players can execute this command.")
        }

        val sender = commandSender as Player

        when (args?.get(0)) {
            "most" -> mostKills(sender)

            "best" -> bestKdr(sender)

            "player" -> {
                if (args.size != 2) {
                    playerKdr(sender)
                    return true
                }
                playerKdr(sender, args[1])
            }

            "save" -> saveKd(sender)

            else -> {
                sender.sendMessage("${ChatColor.RED}Unknown subcommand. Use /kd <most|player|best>")
            }
        }

        return true
    }


    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {

            val subCommands = if (sender.isOp){
                listOf("most", "best", "player", "save")
            } else {
                listOf("most", "best", "player")
            }

            return subCommands.filter { it.startsWith(args[0], ignoreCase = true) }
        } else if (args.size == 2 && args[0].equals("player", ignoreCase = true)) {
            // Provide autocomplete for player names
            val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }
            return onlinePlayers.filter { it.startsWith(args[1], ignoreCase = true) }
        }

        return emptyList()
    }


    /**
     * Sends the statistics of the top 5 players with the most kills to the sender.
     *
     * @param sender The player who initiated the command.
     */
    private fun mostKills(sender: Player) {
        val sortedList = killsList.sortedByDescending { it.kills }.take(5)

        if (sortedList.isNotEmpty()) {
            sender.sendMessage("${ChatColor.GOLD}----Top 5 players with most kills----")
            sortedList.forEachIndexed { index, entry ->

                val uuid = UUID.fromString(entry.playerUUID)

                val player = plugin.server.getPlayer(uuid) ?:
                plugin.server.getOfflinePlayer(uuid)

                val playerName = player.name ?: "Name not found."

                sender.sendMessage("${ChatColor.DARK_AQUA}${index + 1}. $playerName: ${entry.kills} kills")
            }
        } else {
            sender.sendMessage("${ChatColor.RED}No kill data available.")
        }
    }

    /**
     * Sends the statistics of the top 5 players with the best kill/death ratio (KDR) to the sender.
     *
     * @param sender The player who initiated the command.
     */
    private fun bestKdr(sender: Player) {
        val sortedList = killsList.sortedByDescending {
            it.kills.toFloat() / (it.deaths.takeIf { d -> d != 0 } ?: 1)
        }.take(5)

        if (sortedList.isNotEmpty()) {
            sender.sendMessage("${ChatColor.GOLD}----Top 5 players with best K/D ratio----")
            sortedList.forEachIndexed { index, entry ->

                val uuid = UUID.fromString(entry.playerUUID)

                val player = plugin.server.getPlayer(uuid) ?:
                    plugin.server.getOfflinePlayer(uuid)

                val playerName = player.name ?: "Name not found."

                val kdr = entry.kills.toDouble() / (entry.deaths.takeIf { d -> d != 0 } ?: 1)
                sender.sendMessage("${ChatColor.DARK_AQUA}${index + 1}. $playerName: ${"%.2f".format(kdr)} K/D")
            }
        } else {
            sender.sendMessage("${ChatColor.RED}No kill data available.")
        }
    }

    /**
     * Sends the kill/death ratio (KDR) statistics of the specified target player to the sender.
     *
     * @param sender The player who initiated the command.
     * @param target The*/
    private fun playerKdr(sender: Player , target: String) {

        val targetPlayer : String = plugin.server.getOfflinePlayer(target).uniqueId.toString()
        val targetPlayerData : PlayerKillData? = NodesKd.getPlayerData(targetPlayer)

        if (targetPlayerData == null) {
            sender.sendMessage("${ChatColor.RED}Player doesn't have kill data.")
            return
        }

        val kd = if(targetPlayerData.kills <= 0 || targetPlayerData.deaths <= 0){
            "N/A"
        } else {
            targetPlayerData.kills.toFloat() / targetPlayerData.deaths
        }

        sender.sendMessage("${ChatColor.GOLD}----$target's stats----")
        sender.sendMessage("${ChatColor.DARK_AQUA}Kills : ${targetPlayerData.kills}")
        sender.sendMessage("${ChatColor.DARK_AQUA}Deaths : ${targetPlayerData.deaths}")
        sender.sendMessage("${ChatColor.DARK_AQUA}K/D : $kd")
    }

    /**
     * Sends the kill/death ratio (KDR) statistics of the specified player to the sender.
     *
     * @param sender The player who initiated the command.
     */
    private fun playerKdr(sender: Player){
        val playerData : PlayerKillData? = NodesKd.getPlayerData(sender)

        if (playerData == null) {
            sender.sendMessage("${ChatColor.RED}You dont have kill data.")
            return
        }


        val kd = if(playerData.kills <= 0 || playerData.deaths <= 0){
            "N/A"
        } else {
            (playerData.kills.toFloat() / playerData.deaths)
        }


        sender.sendMessage("${ChatColor.GOLD}----Your stats----")
        sender.sendMessage("${ChatColor.DARK_AQUA}Kills : ${playerData.kills}")
        sender.sendMessage("${ChatColor.DARK_AQUA}Deaths : ${playerData.deaths}")
        sender.sendMessage("${ChatColor.DARK_AQUA}K/D : $kd")
    }

    /**
     * Saves the kd List
     * Mainly for debugging
     */
    private fun saveKd(sender: Player) {
        if (!sender.isOp){
            sender.sendMessage("${ChatColor.RED}Only server operators can use this command.")
            return
        }

        plugin.saveKillsList()
        sender.sendMessage("${ChatColor.AQUA}Kills list saved.")
    }

}
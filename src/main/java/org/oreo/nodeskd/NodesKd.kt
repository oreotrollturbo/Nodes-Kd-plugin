package org.oreo.nodeskd

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.nodeskd.commands.KdCommands
import org.oreo.nodeskd.data.PlayerKillData
import org.oreo.nodes_kd.listeners.KillListener
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class NodesKd : JavaPlugin() {

    private var saveFile: File? = null
    private val gson = Gson()

    override fun onLoad() {
        this.saveFile = File(dataFolder, "kills.json")
    }

    override fun onEnable() {

        saveDefaultConfig()

        loadSavedData()

        server.pluginManager.registerEvents(KillListener(this), this)

        getCommand("kd")!!.setExecutor(KdCommands(this))
    }

    override fun onDisable() {
        saveKillsList()
    }


    /**
     * The only function Gson-related that is called on server startup
     * Everything else should be called by it if needed
     */
    private fun loadSavedData() {
        if (saveFile?.exists() == false) {
            logger.info("Kd save file not found, creating a new one.")
            initializeSaveFile() // create the file with default content if it does not exist
            return
        }

        try {
            saveFile?.let {
                FileReader(it).use { reader ->
                    val listType = object : TypeToken<List<PlayerKillData>>() {}.type

                    val savedData: List<PlayerKillData> = gson.fromJson(reader, listType) ?: return

                    killsList.clear() // Clear existing data to avoid duplicates
                    killsList.addAll(savedData)
                    logger.info("Loaded Kd list successfully.")
                }
            }
        } catch (e: IOException) {
            logger.warning("File not found or could not be read, creating save file. Exception: ${e.message}")
            initializeSaveFile()
        } catch (e: JsonSyntaxException) {
            logger.severe("Json syntax error while reading save file: ${e.message}")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            logger.severe("Json structure does not match expected. Exception: ${e.message}")
            e.printStackTrace()
        }
    }


    /**
     * Saves all active objects to the file and then deletes all the in game objects
     * It first wipes the active turret list in case any residuals are present
     */
    fun saveKillsList() {

        try {
            saveFile?.let {
                FileWriter(it).use { writer ->
                    gson.toJson(killsList, writer)
                    logger.info("Kd list saved successfully.")
                }
            }
        } catch (e: IOException) {
            logger.info("Error saving Kd list.")
            e.printStackTrace()
        }
    }


    /**
     * Sets up the turret save file using Gson if it doesn't exist
     */
    private fun initializeSaveFile() {
        if (!saveFile?.exists()!!) {
            try {
                if (saveFile?.createNewFile()!!) {
                    logger.info("Created new file at: " + saveFile!!.absolutePath)
                    saveFile?.let {
                        FileWriter(it).use { writer ->
                            writer.write("[]") // Write an empty JSON array to the file
                            loadSavedData()
                        }
                    }
                }
            } catch (e: IOException) {
                logger.info("Unable to create save file.")
                e.printStackTrace()
            }
        } else {
            logger.info("Save file found.")
        }
    }

    companion object {
        val killsList = ArrayList<PlayerKillData>()

        /**
         * Retrieves the player data for the specified player UUID or Player instance.
         *
         * @param uuid The UUID of the player whose data is to be retrieved.
         * @return The player's kill data if found, otherwise null.
         */
        fun getPlayerData(uuid : String) : PlayerKillData? {
            return killsList.find { it.playerUUID == uuid }
        }
        fun getPlayerData(player : Player) : PlayerKillData? {
            val uuid = player.uniqueId.toString()
            return killsList.find { it.playerUUID == uuid }
        }

        /**
         * Registers a player's game statistics entry into the kills list.
         *
         * @param player The Player object representing the player whose entry is being created.
         * @param kills The number of kills made by the player.
         * @param deaths The number of deaths occurred to the player.
         */
        fun createPlayerEntry(player: Player, kills: Int, deaths: Int) {
            val uuid = player.uniqueId.toString()

            killsList.add(PlayerKillData(uuid, kills, deaths))
        }
        fun createPlayerEntry(uuid: String, kills: Int, deaths: Int) {
            killsList.add(PlayerKillData(uuid, kills, deaths))
        }
    }
}

package org.oreo.nodeskd.data


/**
 * This data class holds all the player data that needs to be stored on server shutdown
 for re-initialization when the server starts
 */
data class PlayerKillData(val playerUUID: String,
                          var kills: Int,
                          var deaths: Int)

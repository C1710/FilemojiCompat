package de.c1710.filemojicompat_ui.versions

interface VersionProvider {
    /**
     * Returns the version provided.
     * This might block on a [java.util.concurrent.Future], therefore it should be called as late as
     * possible
     */
    fun getVersion(): Version
}
package de.c1710.filemojicompat_ui.versions


/**
 * A simple structure for (comparable) versions
 */
class Version(private var version: IntArray) : Comparable<Version>, VersionProvider {

    override fun compareTo(other: Version): Int {
        // We need to pad the arrays to the longest size
        val len = kotlin.math.max(this.version.size, other.version.size)
        // Version codes are reasonably small such that copying them is not too bad
        val thisVersion = this.version.copyOf(len)
        val otherVersion = other.version.copyOf(len)
        val diff: Pair<Int, Int>? = thisVersion.zip(otherVersion)
            .firstOrNull { subVersions: Pair<Int, Int> -> subVersions.first != subVersions.second }
        return if (diff != null) {
            // There is a difference
            if (diff.first < diff.second) {
                -1
            } else {
                1
            }
        } else {
            0
        }
    }

    fun isZero(): Boolean {
        return version.all { subVersion -> subVersion == 0 }
    }

    override fun getVersion(): Version {
        return this
    }

    override fun toString(): String {
        return version.joinToString(".")
    }

    companion object {
        @JvmStatic
        fun fromString(string: String?): Version {
            return Version((string ?: "").split('.')
                .map { subversion: String -> subversion.toIntOrNull() ?: 0 }
                .toIntArray())
        }

        internal fun fromStringOrNull(string: String?): Version? {
            return if (string != null) {
                fromString(string)
            } else {
                null
            }
        }
    }
}
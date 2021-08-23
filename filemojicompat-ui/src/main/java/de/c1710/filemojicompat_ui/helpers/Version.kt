package de.c1710.filemojicompat_ui.helpers

import java.lang.Integer.max
import java.util.*

class Version (val version: IntArray): Comparable<Version> {

    override fun compareTo(other: Version): Int {
        // We need to pad the arrays to the longest size
        val len = max(this.version.size, other.version.size)
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
}
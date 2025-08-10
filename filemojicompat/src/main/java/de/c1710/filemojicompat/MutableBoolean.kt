package de.c1710.filemojicompat

class MutableBoolean(private var state: Boolean) {
    fun set(newState: Boolean) {
        state = newState
    }

    fun get(): Boolean {
        return state
    }
}
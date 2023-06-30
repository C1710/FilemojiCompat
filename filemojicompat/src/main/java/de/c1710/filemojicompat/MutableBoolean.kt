package de.c1710.filemojicompat;

public class MutableBoolean {
    private boolean state;

    public MutableBoolean (boolean initialValue) {
        this.state = initialValue;
    }

    public void set(boolean newState) {
        this.state = newState;
    }

    public boolean get() {
        return this.state;
    }
}

package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Receiver represents a method receiver.
 * Corresponds to uniast.Receiver in Go
 */
public class Receiver {
    @JsonProperty("IsPointer")
    private boolean isPointer; // Java is always false

    @JsonProperty("Type")
    private Identity type;

    public Receiver() {
        this.isPointer = false;
    }

    public Receiver(boolean isPointer, Identity type) {
        this.isPointer = isPointer;
        this.type = type;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public void setPointer(boolean pointer) {
        isPointer = pointer;
    }

    public Identity getType() {
        return type;
    }

    public void setType(Identity type) {
        this.type = type;
    }
}

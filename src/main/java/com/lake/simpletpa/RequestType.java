package com.lake.simpletpa;

/**
 * Defines the type of teleport request.
 */
public enum RequestType {
    /**
     * TPA: The sender teleports to the target's location.
     */
    TPA,

    /**
     * TPA_HERE: The target teleports to the sender's location.
     */
    TPA_HERE
}

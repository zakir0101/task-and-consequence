package com.example.taskandconsequence.model;

public enum Status {
    PENDING(0),
    PENDING_PUNISHMENT(1),

    SUCCEED(2),
    FAIL(3),

    SUCCEED_PUNISHMENT(4);



    private final int value;

    Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Status fromInt(int value) {
        for (Status status : Status.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Value " + value + " is not a valid Status");
    }
}
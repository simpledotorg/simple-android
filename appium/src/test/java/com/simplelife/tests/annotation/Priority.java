package com.simplelife.tests.annotation;

public enum Priority {
    HIGH(1), MEDIUM(2),LOW(3);

    private int rank;
    Priority(int rank)
    {
        this.rank=rank;
    }

    public int getRank(){
        return rank;
    }
}

package main.java.unimelb.core;

import java.util.Random;

public class Wizard {

    private int status;  // Alive = 1; Dead = 0
    private long wizardNum;
    private double hitRate;  // Probability of hitting a player

    public Wizard(double hitRate, long wizardNum) {
        this.status = 1;
        this.wizardNum = wizardNum;
        this.hitRate = hitRate;
    }

    public Wizard(double hitRate) {
        this.hitRate = hitRate;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus() {
        this.status = 0;
    }

    public long getWizardNum() {
        return this.wizardNum;
    }

    public double getHitRate() {
        return this.hitRate;
    }

    public boolean attack() {
        Random rand = new Random();
        double prob = rand.nextDouble();
        if (prob > this.hitRate) {
            return false;
        }
        return true;
    }
}
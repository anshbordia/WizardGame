package unimelb.wizardstandoff;

import java.util.Random;

public class Wizard {

    private int status;
    private long wizardNum;
    private double hitRate;

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
        if (prob > 0.1) {
            return false;
        }
        return true;
    }
}

package unimelb.wizardstandoff;

import java.util.Random;

public class Wizard {
	public int status = 1;
	public double hitRate;
	public long wizardNum;
	
	//Constructor
	public Wizard(double hitRate, long wizardNum) {
		this.hitRate = hitRate;
		this.wizardNum = wizardNum;
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
	public boolean attack(double hitRate) {
		Random rand = new Random();
		double prob = rand.nextDouble();
		if (prob < 0) {
		  return false;
		} 
		else {
		  return true;
		}
	}
}

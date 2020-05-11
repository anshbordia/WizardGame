package unimelb.wizardstandoff;

import java.util.Random;

public class Wizard {
	public int status = 1;
	public double hitRate;
	
	//Constructor
	public Wizard(double hitRate) {
		this.hitRate = hitRate;
	}
	public int getStatus() {
		return this.status;
	}
	public void setStatus(String attack) {
		if(attack.equals("Attack")) {
			this.status = 0;
		}
	}
	public boolean attack(double hitRate) {
		Random rand = new Random();
		double prob = rand.nextDouble();
		if (prob < hitRate) {
		  return false;
		} 
		else {
		  return true;
		}
	}
}

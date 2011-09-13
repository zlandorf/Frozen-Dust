package fr.frozen.network.server;

import java.util.ArrayList;
import java.util.List;

public class IDGiver {
	private int idCounter = 0;
	private List<Integer> freeIds;
	
	public IDGiver() {
		freeIds = new ArrayList<Integer>();
	}

	//TODO maybe check to see if someone can be reusing the id of someone else and maybe receive messages destined to somebody else
	//dangerous !
	//maybe by giving a "spawn" tick and discarding messages with an inferior tick
	public int getId() {
		if (freeIds.size() > 0) {
			return freeIds.remove(0).intValue(); 
		}
		return idCounter++;
	}
	
	public void freeId(int val) {
		freeIds.add(new Integer(val));
	}
}

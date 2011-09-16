package fr.frozen.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class GameState {

	protected boolean active;
	protected boolean visible;

	protected String stateName;
	
	protected List<GameObject> objects;
	
	//we stock the objects in temporary lists and add/remove during the update
	protected List<GameObject> objectsToAdd;
	protected List<GameObject> objectsToRemove;
	
	protected HashMap<String, List<GameObject>> objectCollections;
	
	protected IGameEngine gameEngine;
	
	public GameState(IGameEngine ge, String name) {
		this(ge,name,false,false);
	}
	
	public GameState(IGameEngine ge, String name, boolean active, boolean visible) {
		objects = new Vector<GameObject>();
		objectsToAdd = new Vector<GameObject>();
		objectsToRemove = new Vector<GameObject>();
		objectCollections = new HashMap<String, List<GameObject>>();
		gameEngine = ge;
		stateName = name;
		createGameObjects();
		
		this.active = active;
		this.visible = visible;
	}
	
	public void createGameObjects() {
	}
	
	public synchronized List<GameObject> createGameObjectCollection(String collectionName) {
		List<GameObject> collection = new Vector<GameObject>(); 
		objectCollections.put(collectionName, collection);
		return collection;
	}
	
	public synchronized List<GameObject> getGameObjectCollection(String collectionName) {
		return objectCollections.get(collectionName);
	}
	
	public synchronized GameObject addGameObject(GameObject go, String collectionName) {
		addGameObject(go);
		List<GameObject> collection = objectCollections.get(collectionName);
		if (collection == null) {
			collection = createGameObjectCollection(collectionName);
		}
		collection.add(go);
		return go;
	}
	
	public synchronized GameObject addGameObject(GameObject go) {
		objectsToAdd.add(go);
		return go;
	}
	
	public synchronized void removeGameObject(GameObject go) {
		objectsToRemove.add(go);
		Set<Entry<String, List<GameObject>>> collections = objectCollections.entrySet();
		for (Entry<String, List<GameObject>> entry : collections) {
			List<GameObject> collection = entry.getValue();
			if (collection.contains(go)) {
				collection.remove(go);
			}
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setActive(boolean value) {
		active = value;
		//System.out.println(stateName + " "+(value?"":"de")+"activated");
	}
	
	public void setVisible(boolean value) {
		visible = value;
		//System.out.println(stateName + " now "+(value?"":"in")+"visible");
	}
	
	public String getName() {
		return stateName;
	}
	
	public void setName(String name) {
		stateName = name;
	}
	
	private synchronized void addGameObjects() {
		for (GameObject go : objectsToAdd) {
			objects.add(go);
		}
		objectsToAdd.clear();
	}
	
	private synchronized void removeGameObjects() {
		for (GameObject go : objectsToRemove) {
			objects.remove(go);
		}
		objectsToRemove.clear();
	}
	
	public void update(float deltaTime) {
		addGameObjects();
		removeGameObjects();
		
		for (GameObject go : objects) {
			go.update(deltaTime);
		}
	}
	
	public void render(float deltaTime) {
		for (GameObject go : objects) {
			go.render(deltaTime);
		}
	}
}

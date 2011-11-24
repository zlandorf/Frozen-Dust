package fr.frozen.iron.client.gameStates.gameCreation;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.DropList;
import fr.frozen.iron.client.components.DropListItem;

public class SoloGameCreation extends AbstractGameCreation {

	protected int hostRace = -1;
	protected int otherRace = -1;
	
	public SoloGameCreation(IGameEngine engine) {
		super(engine, "soloGameCreation");
	}

	@Override
	protected void leave() {
		((IronTactics) gameEngine).switchToState("mainMenu");
	}

	@Override
	protected void setPlayers() {
		hostName.setLabel("Player 1");
		setList(hostList);
		otherName.setLabel("Player 2");
		setList(otherList);
		hostList.setEditable(true);
		otherList.setEditable(true);
	}
	
	@Override
	protected void reInit() {
		super.reInit();
		hostRace = otherRace = -1;
	}
	
	@Override
	protected void setReady() {
		if (hostRace != -1 && otherRace != -1) {
			((IronTactics) gameEngine).startNewSoloGame(hostRace, otherRace);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof DropList) {
			DropList source = (DropList) e.getSource();
			DropListItem selected = source.getSelectedItem();
			if (selected == null) return;
			if (source.equals(hostList)) {
				hostList.setLabel(selected.getLabel());
				hostRace = selected.getValue();
			} else {
				otherList.setLabel(selected.getLabel());
				otherRace = selected.getValue();
			}
		}
	}
}

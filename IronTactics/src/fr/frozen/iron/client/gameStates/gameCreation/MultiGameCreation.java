package fr.frozen.iron.client.gameStates.gameCreation;

import org.apache.log4j.Logger;

import fr.frozen.game.IGameEngine;
import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.IronTactics;
import fr.frozen.iron.client.components.ActionEvent;
import fr.frozen.iron.client.components.DropList;
import fr.frozen.iron.client.components.DropListItem;
import fr.frozen.iron.client.messageEvents.NewPlayerEvent;
import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
import fr.frozen.iron.client.messageEvents.RaceChoiceEvent;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class MultiGameCreation extends AbstractGameCreation implements
		NetEventListener {

	protected IronClient netClient;

	public MultiGameCreation(IGameEngine engine) {
		super(engine, "multiGameCreation");
		netClient = ((IronTactics) gameEngine).getNetClient();
	}

	@Override
	protected void leave() {
		netClient.sendMessage(Protocol.SERVER_C_REQUEST_SESSION,
				IronUtil.intToByteArray(Protocol.SESSION_LOBBY.ordinal()));
	}

	@Override
	protected void setReady() {
		netClient.sendEmptyMessage(Protocol.GAME_CREATION_READY);
	}

	@Override
	protected void setPlayers() {
		netClient.sendEmptyMessage(Protocol.SESSION_PLAYER_LIST_REQUEST);
	}

	@Override
	public void onNetEvent(NetEvent ne) {
		if (!active)
			return;

		if (ne instanceof PlayerListReceivedEvent) {
			PlayerListReceivedEvent plre = (PlayerListReceivedEvent) ne;

			if (plre.getList().size() >= 1) {
				host = plre.getList().get(0);
				hostName.setLabel(host.getName());
				if (host.getId() == netClient.getClientId()) {
					setList(hostList);
				}
			}

			if (plre.getList().size() >= 2) {
				other = plre.getList().get(1);
				otherName.setLabel(other.getName());

				if (other.getId() == netClient.getClientId()) {
					setList(otherList);
				}
			}
		}

		if (ne instanceof NewPlayerEvent) {
			NewPlayerEvent npe = (NewPlayerEvent) ne;
			other = npe.getPlayer();
			otherName.setLabel(other.getName());
		}

		if (ne instanceof PlayerLogoutEvent) {
			PlayerLogoutEvent ploe = (PlayerLogoutEvent) ne;
			// if I logged out, i dont wont see the list anymore so no need to
			// update
			// if the other logged out but was hosting, then i will be bumped to
			// the lobby
			if (ploe.getId() == other.getId()
					&& ploe.getId() != netClient.getId()) {
				otherList.setLabel(TXT_RACE_NOT_CHOSEN);
				otherName.setLabel(TXT_WAITING);
			}
		}

		if (ne instanceof RaceChoiceEvent) {
			RaceChoiceEvent rce = (RaceChoiceEvent) ne;
			String race = IronUtil
					.getRaceStr(Protocol.get(rce.getChosenRace()));

			if (host == null || other == null)
				return;// in case we receive a raceEvent before we know who the
						// players are
			// TODO: check if this works properly

			if (rce.getClientId() == netClient.getClientId()) {
				if (rce.getClientId() == host.getId()) {
					if (rce.getChosenRace() != hostList.getSelectedItem()
							.getValue()) {
						Logger.getLogger(getClass()).error(
								"chosen race not the one requested");
					}
				} else if (rce.getClientId() == other.getId()) {
					if (rce.getChosenRace() != otherList.getSelectedItem()
							.getValue()) {
						Logger.getLogger(getClass()).error(
								"chosen race not the one requested");
					}
				}
			} else {
				if (rce.getClientId() == host.getId()) {
					hostList.setLabel(race);
				} else {
					otherList.setLabel(race);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof DropList) {
			DropList source = (DropList) e.getSource();
			DropListItem selected = source.getSelectedItem();
			if (selected == null)
				return;
			netClient.sendMessage(Protocol.GAME_CREATION_RACE_REQUEST,
					IronUtil.intToByteArray(selected.getValue()));
		}
	}
}

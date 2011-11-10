package fr.frozen.iron.common.controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.frozen.iron.client.IronClient;
import fr.frozen.iron.client.messageEvents.GameActionEvent;
import fr.frozen.iron.client.messageEvents.GameOverEvent;
import fr.frozen.iron.client.messageEvents.GameTurnEvent;
import fr.frozen.iron.client.messageEvents.UndoMoveEvent;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.entities.IronUnit;
import fr.frozen.iron.common.skills.Skill;
import fr.frozen.iron.protocol.Protocol;
import fr.frozen.iron.util.IronUtil;
import fr.frozen.network.client.NetEvent;
import fr.frozen.network.client.NetEventListener;

public class ClientGameController extends AbstractGameController implements NetEventListener {

	protected volatile IronClient netClient;
	
	public ClientGameController(IronWorld world, IronClient netClient) {
		super(world);
		this.netClient = netClient;
		netClient.addNetEventListener(this);
	}

	@Override
	protected boolean isAddParticles() {
		return true;
	}

	@Override
	public void undoMove() {
		if (canUndo()) {
			byte[] data = new byte[4];
			System.arraycopy(IronUtil.intToByteArray(lastUnitMoved.getId()), 0,
					data, 0, 4);

			netClient.sendMessage(Protocol.GAME_UNDO_REQUEST, data);
		}
	}
	
	@Override
	public void handleSkill(int unitSrcId, Skill skill, int x, int y) {
		byte[] data = new byte[20];

		System.arraycopy(IronUtil.intToByteArray(unitSrcId), 0, data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_SKILL), 0,
				data, 4, 4);
		System.arraycopy(IronUtil.intToByteArray(skill.getSkillType()), 0, data, 8, 4);
		System.arraycopy(IronUtil.intToByteArray(x), 0, data, 12, 4);
		System.arraycopy(IronUtil.intToByteArray(y), 0, data, 16, 4);

		netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
	}
	
	@Override
	public void handleMove(int unitSrcId, int x, int y) {
		byte[] data = new byte[16];

		System.arraycopy(IronUtil.intToByteArray(unitSrcId), 0,
				data, 0, 4);
		System.arraycopy(IronUtil.intToByteArray(IronUnit.ACTION_MOVE), 0,
				data, 4, 4);
		System.arraycopy(IronUtil.intToByteArray(x), 0, data, 8, 4);
		System.arraycopy(IronUtil.intToByteArray(y), 0, data, 12, 4);

		netClient.sendMessage(Protocol.GAME_ACTION_REQUEST, data);
	}
	
	public void update(float deltaTime) {
		if (!gameStarted)
			return;

		timeLeftForTurn -= deltaTime;
		if (timeLeftForTurn < 0) timeLeftForTurn = 0;
	}
	
	@Override
	public void onNetEvent(NetEvent ne) {
		
		if (ne instanceof GameTurnEvent) {
			GameTurnEvent gte = (GameTurnEvent) ne;

			if (!isGameStarted()) {
				startGame(gte.getPlayerId());
			} else {
				setTurn(gte.getPlayerId());
				turnIndex ^= 1;
			}
		}
		/* in game actions */

		if (ne instanceof GameActionEvent) {
			try {
				handleGameAction((GameActionEvent) ne);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (ne instanceof UndoMoveEvent) {
			IronUnit unit = world.getUnitFromId(((UndoMoveEvent) ne)
					.getUnitId());
			if (unit != null) {
				unit.undoMove();
				setLastUnitMoved(null);
				notifyUndoMove(unit);
			}
		}
		
		if (ne instanceof GameOverEvent) {
			GameOverEvent goe = (GameOverEvent) ne;
			onGameOver(goe.getWinnerId());
		}
	}
	
	protected void handleGameAction(GameActionEvent gae) throws IOException {
		IronUnit unitSrc = world.getUnitFromId(gae.getUnitId());
		DataInputStream is = new DataInputStream(new ByteArrayInputStream(gae
				.getData()));

		int x, y;

		switch (gae.getType()) {
		case IronUnit.ACTION_MOVE:
			x = is.readInt();
			y = is.readInt();
			int moveCost = is.readInt();
			unitSrc.move(x, y, moveCost);
			if (unitSrc.hasPlayed()) {
				unitSrc.setSelected(false);
			}
			if (unitSrc.getOwnerId() == netClient.getClientId()) {
				setLastUnitMoved(unitSrc);
			}
			break;
		case IronUnit.ACTION_SKILL:
			Skill skill = Skill.getSkill(is.readInt());
			x = is.readInt();
			y = is.readInt();

			List<int[]> values = new ArrayList<int[]>();
			while (true) {
				int dstId = is.readInt();
				if (dstId == -1)
					break;
				int value = is.readInt();

				values.add(new int[] { dstId, value });
			}
			skill.executeClientSide(world, unitSrc.getId(), x, y, values);

			if (unitSrc.hasPlayed()) {
				unitSrc.setSelected(false);
			}
			setLastUnitMoved(null);
			notifySkill(unitSrc, skill, x, y, values);
			break;
		default:
			Logger.getLogger(getClass()).error(
					"Action not recognised " + gae.getType());
		}
	}

}

package fr.frozen.iron.client;

import fr.frozen.game.AppletEngine;
import fr.frozen.game.BaseApplet;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;

@SuppressWarnings("serial")
public class IronApplet extends BaseApplet {
	@Override
	protected void createEngine() {
		IronConfig.configClientLogger();
		IronTactics it = new IronTactics(IronConst.HOST);
		it.initIronTactics();
		
		engine = new AppletEngine(it);
	}
}

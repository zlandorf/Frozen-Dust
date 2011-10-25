package fr.frozen.iron.client;

import fr.frozen.game.AppletEngine;
import fr.frozen.game.BaseApplet;
import fr.frozen.iron.util.IronConfig;
import fr.frozen.iron.util.IronConst;

@SuppressWarnings("serial")
public class IronApplet extends BaseApplet {
	
	@Override
	public void init() {
		IronConfig.configClientLogger();
		super.init();
	}
	
	@Override
	protected void createEngine() {
		
		IronTactics it = new IronTactics(IronConst.HOST);
		it.initIronTactics();
		
		engine = new AppletEngine(it);
	}
}

package com.fluxtream.connectors.facebook;

import org.springframework.stereotype.Component;

import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;

/**
 * @author candide
 * 
 */

@Component
@Updater(prettyName = "Facebook", value = 30, objectTypes = {})
public class FacebookUpdater extends AbstractUpdater {

	public FacebookUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(final UpdateInfo updateInfo) throws Exception {
		throw new RuntimeException("Not Yet Implemented");
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) {
	}

}

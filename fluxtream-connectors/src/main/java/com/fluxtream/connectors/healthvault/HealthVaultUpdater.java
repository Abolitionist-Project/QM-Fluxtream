package com.fluxtream.connectors.healthvault;

import org.springframework.stereotype.Component;

import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;


@Component
@Updater(prettyName = "HealthVault", value = 125, objectTypes = {})
public class HealthVaultUpdater extends AbstractUpdater {

	public HealthVaultUpdater() {
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

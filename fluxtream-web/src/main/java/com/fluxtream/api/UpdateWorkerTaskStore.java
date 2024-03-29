package com.fluxtream.api;

import static com.newrelic.api.agent.NewRelic.setTransactionName;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.UpdateWorkerTask;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.ConnectorUpdateService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Candide Kemmler (candide@fluxtream.com)
 */
@Path("/updateTasks")
@Component("RESTUpdateWorkerTaskStore")
@Scope("request")
public class UpdateWorkerTaskStore {

	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.forID("UTC"));

	@GET
	@Path("/{connector}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getUpdateTasks(@PathParam("connector") String connectorName) {
		setTransactionName(null, "GET /updateTasks/" + connectorName);
		try {
			long guestId = AuthHelper.getGuestId();
			final List<UpdateWorkerTask> scheduledUpdates = connectorUpdateService.getScheduledOrInProgressUpdateTasks(
					guestId, Connector.getConnector(connectorName));
			JSONArray array = new JSONArray();
			for (UpdateWorkerTask scheduledUpdate : scheduledUpdates) {
				array.add(toJSON(scheduledUpdate));
			}
			return array.toString();
		} catch (Exception e) {
			return gson.toJson(new StatusModel(false, "Failed to get udpate tasks: " + e.getMessage()));
		}
	}

	@GET
	@Path("/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getUpdateTasksAll() {
		setTransactionName(null, "GET /updateTasks/all");
		try {
			long guestId = AuthHelper.getGuestId();
			final Collection<Connector> connectors = Connector.getAllConnectors();
			JSONArray res = new JSONArray();
			for (Connector c : connectors) {
				final List<UpdateWorkerTask> scheduledUpdates = connectorUpdateService
						.getScheduledOrInProgressUpdateTasks(guestId, Connector.getConnector(c.getName()));
				JSONArray array = new JSONArray();
				for (UpdateWorkerTask scheduledUpdate : scheduledUpdates) {
					array.add(toJSON(scheduledUpdate));
				}
				JSONObject connectorStatus = new JSONObject();
				connectorStatus.accumulate("name", c.getName());
				connectorStatus.accumulate("status", array);
				res.add(connectorStatus);
			}
			return res.toString();
		} catch (Exception e) {
			return gson.toJson(new StatusModel(false, "Failed to get update tasks: " + e.getMessage()));
		}
	}

	private JSONObject toJSON(UpdateWorkerTask task) {
		JSONObject json = new JSONObject();
		json.accumulate("objectTypes", task.getObjectTypes());
		json.accumulate("updateType", task.updateType.toString());
		json.accumulate("timeScheduled", fmt.print(task.timeScheduled));
		json.accumulate("retries", task.retries);
		json.accumulate("status", task.status.toString());
		json.accumulate("jsonParams", task.jsonParams);
		json.accumulate("auditTrail", task.auditTrail);
		return json;
	}

	@GET
	@Path("/{connector}/{objectType}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getObjectTypeUpdateTasks(@PathParam("connector") String connectorName,
			@PathParam("objectType") String objectTypeName) {
		setTransactionName(null, "GET /updateTasks/" + connectorName + "/" + objectTypeName);
		try {
			long guestId = AuthHelper.getGuestId();
			final Connector connector = Connector.getConnector(connectorName);
			final ObjectType objectType = ObjectType.getObjectType(connector, objectTypeName);
			final UpdateWorkerTask scheduledUpdate = connectorUpdateService.getScheduledUpdateTask(guestId,
					connector.getName(), objectType.value());
			return scheduledUpdate != null ? toJSON(scheduledUpdate).toString() : "{}";
		} catch (Exception e) {
			return gson.toJson(new StatusModel(false, "Failed to get update tasks: " + e.getMessage()));
		}
	}

	@DELETE
	@Path("/{connector}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteUpdateTasks(@PathParam("connector") String connectorName) {
		setTransactionName(null, "DELETE /updateTasks/" + connectorName);
		try {
			long guestId = AuthHelper.getGuestId();
			final Connector connector = Connector.getConnector(connectorName);
			connectorUpdateService.flushUpdateWorkerTasks(guestId, connector, false);
			StatusModel statusModel = new StatusModel(true, "successfully deleted pending update tasks for "
					+ connectorName);
			return gson.toJson(statusModel);
		} catch (Exception e) {
			return gson.toJson(new StatusModel(false, "Failed to get update tasks: " + e.getMessage()));
		}
	}

}

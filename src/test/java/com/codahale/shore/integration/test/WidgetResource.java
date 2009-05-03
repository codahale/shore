package com.codahale.shore.integration.test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.wideplay.warp.persist.Transactional;

@Path("/widget/{name}")
@Produces(MediaType.TEXT_PLAIN)
public class WidgetResource {
	private final WidgetDAO widgetDAO;
	
	@Inject
	public WidgetResource(WidgetDAO widgetDAO) {
		this.widgetDAO = widgetDAO;
	}
	
	@GET
	@Transactional
	public String getWidget(@PathParam("name") String name) {
		final Widget widget = widgetDAO.findByName(name);
		
		if (widget != null) {
			return "[Widget name:" + widget.getName() + ", description:" + widget.getDescription() + "]";
		}
		
		throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
	}
	
	@POST
	@Transactional
	public Response makeWidget(@Context UriInfo uriInfo,
			@PathParam("name") String name,
			String description) {
		
		final Widget widget = new Widget();
		widget.setName(name);
		widget.setDescription(description);
		widgetDAO.save(widget);
		
		return Response.created(uriInfo.getBaseUriBuilder().path(WidgetResource.class).build(name)).build();
	}
}

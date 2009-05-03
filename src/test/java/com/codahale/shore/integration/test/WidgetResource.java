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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.Transactional;

@Path("/widget/{name}")
@Produces(MediaType.TEXT_PLAIN)
public class WidgetResource {
	private final Provider<Session> session;
	
	@Inject
	public WidgetResource(Provider<Session> session) {
		this.session = session;
	}
	
	@GET
	@Transactional
	public String getWidget(@PathParam("name") String name) {
		final Session s = session.get();
		
		final Criteria criteria = s.createCriteria(Widget.class);
		criteria.add(Restrictions.eq("name", name));
		final Widget widget = (Widget) criteria.uniqueResult();
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
		final Session s = session.get();
		
		final Widget widget = new Widget();
		widget.setName(name);
		widget.setDescription(description);
		s.persist(widget);
		
		return Response.created(uriInfo.getBaseUriBuilder().path(WidgetResource.class).build(name)).build();
	}
}

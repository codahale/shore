package com.codahale.shore.integration.test;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.codahale.shore.dao.AbstractDAO;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.Transactional;

public class WidgetDAOImpl extends AbstractDAO<Widget> implements WidgetDAO {
	
	@Inject
	public WidgetDAOImpl(Provider<Session> provider) {
		super(provider, Widget.class);
	}

	@Transactional
	@Override
	public Widget findByName(String name) {
		return uniqueResult(
			criteria()
				.add(Restrictions.eq("name", name))
				.setMaxResults(1)
		);
	}
	
	@Transactional
	@Override
	public Widget save(Widget widget) {
		return persist(widget);
	}
	
}

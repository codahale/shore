package com.codahale.shore.integration.test;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.codahale.shore.dao.AbstractDAO;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.Transactional;

public class WidgetDAOImpl extends AbstractDAO implements WidgetDAO {
	
	@Inject
	public WidgetDAOImpl(Provider<Session> provider) {
		super(provider);
	}

	@Transactional
	@Override
	public Widget findByName(String name) {
		final Criteria criteria = currentSession().createCriteria(Widget.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.setMaxResults(1);
		return (Widget) criteria.uniqueResult();
	}
	
	@Transactional
	@Override
	public Widget save(Widget widget) {
		currentSession().save(widget);
		return widget;
	}
	
}

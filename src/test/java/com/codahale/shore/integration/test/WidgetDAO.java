package com.codahale.shore.integration.test;

import com.google.inject.ImplementedBy;

@ImplementedBy(WidgetDAOImpl.class)
public interface WidgetDAO {
	public abstract Widget findByName(String name);
	public abstract Widget save(Widget widget);
}

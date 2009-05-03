package com.codahale.shore.integration.test;

import org.mortbay.servlet.GzipFilter;

import com.codahale.shore.AbstractConfiguration;

class IntegrationTestConfig extends AbstractConfiguration {
	@Override
	protected void configure() {
		addResourcePackage("com.codahale.shore.integration.test");
		addEntityPackage("com.codahale.shore.integration.test");
		addServletFilter(GzipFilter.class, "/*");
	}

	@Override
	public String getExecutableName() {
		return "test";
	}
}
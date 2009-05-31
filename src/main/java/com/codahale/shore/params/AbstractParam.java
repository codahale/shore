package com.codahale.shore.params;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * An abstract base class for parameter classes.
 * 
 * @author coda
 *
 * @param <V> the type of value returned by descendent classes
 */
public abstract class AbstractParam<V> {
	private final V value;
	
	/**
	 * Parses {@code param}, throwing an exception if {@code param} is
	 * unparsable.
	 * 
	 * @param param an external input from a path, query, form, or other parameter
	 * @throws WebApplicationException if {@code param} is unparsable
	 */
	public AbstractParam(String param) throws WebApplicationException {
		try {
			this.value = parse(param);
		} catch (Exception e) {
			throw new WebApplicationException(onError(param, e));
		}
	}
	
	/**
	 * Returns the parsed value.
	 * 
	 * @return the parsed value
	 */
	public V getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	/**
	 * Parses {@code param} and returns it as an instance of type {@code V}.
	 * 
	 * @param param an external input from a path, query, form, or other parameter
	 * @return {@code param} as an instance of type {@code V}
	 * @throws Exception if {@code param} is unparsable
	 */
	protected abstract V parse(String param) throws Exception;
	
	/**
	 * Returns a 400 Bad Request response describing the invalid parameter.
	 *
	 * @param param the invalid parameter
	 * @param e the type of exception thrown by {@link #parse(String)}
	 * @return a {@link Response} describing the problem parsing {@code param}
	 */
	protected Response onError(String param, Throwable e) {
		return Response
				.status(Status.BAD_REQUEST)
				.entity(getErrorMessage(param, e))
				.build();
	}

	/**
	 * Returns an error message describing the invalid parameter.
	 * 
	 * @param param the invalid parameter
	 * @param e the type of exception thrown by {@link #parse(String)}
	 * @return a string describing the problem parsing {@code param}
	 */
	protected String getErrorMessage(String param, Throwable e) {
		return new StringBuilder()
					.append("Invalid parameter: ")
					.append(param)
					.append(" (")
					.append(e.getMessage())
					.append(").")
					.toString();
	}
}

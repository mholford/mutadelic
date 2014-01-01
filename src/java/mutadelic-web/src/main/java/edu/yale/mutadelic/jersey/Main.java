package edu.yale.mutadelic.jersey;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import edu.yale.mutadelic.morphia.MorphiaService;

public class Main {
	
	static class MorphiaBinder implements Binder {

		@Override
		public void bind(DynamicConfiguration config) {
			config.bind(BuilderHelper.link(MorphiaServiceImpl.class)
					.to(MorphiaService.class).build());
		}

	}
	
	static class CORSFilter implements ContainerResponseFilter
	{
	  private static final String ORIGINHEADER = "Origin";
	  private static final String ACAOHEADER = "Access-Control-Allow-Origin";
	  private static final String ACRHHEADER = "Access-Control-Request-Headers";
	  private static final String ACAHHEADER = "Access-Control-Allow-Headers";

	  public CORSFilter()
	  {
	  }

	  @Override
	  public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
	  {
	    final String requestOrigin = request.getHeaderString(ORIGINHEADER);
	    response.getHeaders().add(ACAOHEADER, requestOrigin);

	    final String requestHeaders = request.getHeaderString(ACRHHEADER);
	    response.getHeaders().add(ACAHHEADER, requestHeaders);
	  }
	}

	protected static ResourceConfig configure() {

		ResourceConfig rc = new ResourceConfig();
		rc.packages("edu.yale.mutadelic.jersey");
		rc.registerInstances(new MorphiaBinder(), new JacksonFeature(), new MultiPartFeature());
		rc.register(CORSFilter.class);
		return rc;
	}

	public static void main(String[] args) {
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
				URI.create("http://localhost:8080/mutadelic/"), configure());
		System.out.println("Press any key to stop");
		try {
			System.in.read();
			server.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

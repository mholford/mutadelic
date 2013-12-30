package edu.yale.mutadelic.jersey;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import edu.yale.mutadelic.morphia.entities.Variant;

@Path("variants")
public class VariantResource {

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Variant> addVariantFile(@FormDataParam("file") InputStream is, @FormDataParam("file") FormDataContentDisposition info) {
		List<Variant> output = new ArrayList<>();
		
		return output;
	}
}

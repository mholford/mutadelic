package edu.yale.mutadelic.jersey;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.VariantDAO;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.Variant;

@Path("inputs")
public class InputResource {

	@Inject
	MorphiaService morphiaService;
	private InputDAO inputDao;
	private OutputDAO outputDao;
	private VariantDAO variantDao;

	public InputResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addInput(Input newI) {
		inputDao = morphiaService.getInputDAO();
		inputDao.save(newI);

		return Response.status(201).build();
	}

	@POST
	@Path("{inputId}/variants")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addInputVariant(
			@PathParam(value = "inputId") String inputId, Variant newV) {
		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input i = inputDao.findById(iid);

		variantDao = morphiaService.getVariantDAO();
		variantDao.save(newV);
		i.getVariants().add(newV);

		inputDao.save(i);

		return Response.status(201).build();
	}

	@GET
	@Path("{inputId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Input getInput(@PathParam(value = "inputId") String inputId) {
		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input i = inputDao.findById(iid);
		return i;
	}

	@GET
	@Path("{inputId}/variants/{variantId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Variant getVariant(@PathParam(value = "inputId") String inputId,
			@PathParam(value = "variantId") String variantId) {
		Integer vid = Integer.parseInt(variantId);
		variantDao = morphiaService.getVariantDAO();
		Variant variant = variantDao.findById(vid);
		return variant;
	}

	@GET
	@Path("{inputId}/outputs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Output> getInputOutputs(
			@PathParam(value = "inputId") String inputId) {
		Integer iid = Integer.parseInt(inputId);
		try {
			outputDao = morphiaService.getOutputDAO();
			List<Output> outputs = outputDao.findByInputId(iid);
			return outputs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("{inputId}/variants")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Variant> getInputVariants(
			@PathParam(value = "inputId") String inputId) {
		Integer iid = Integer.parseInt(inputId);
		try {
			inputDao = morphiaService.getInputDAO();
			Input i = inputDao.findById(iid);
			List<Variant> vars = i.getVariants();
			return vars;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{inputId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editInput(@PathParam(value = "inputId") String inputId,
			Input newI) {
		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input i = inputDao.findById(iid);

		if (newI.getName() != null) {
			i.setName(newI.getName());
		}

		inputDao.save(i);

		return Response.status(200).build();
	}

	@POST
	@Path("{inputId}/variants/{variantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editVariant(@PathParam(value = "inputId") String inputId,
			@PathParam(value = "variantId") String variantId, Variant newV) {
		Integer vid = Integer.parseInt(variantId);
		variantDao = morphiaService.getVariantDAO();
		Variant v = variantDao.findById(vid);

		if (newV.getChromosome() != null) {
			v.setChromosome(newV.getChromosome());
		}
		if (newV.getEnd() != null) {
			v.setEnd(newV.getEnd());
		}
		if (newV.getObserved() != null) {
			v.setObserved(newV.getObserved());
		}
		if (newV.getReference() != null) {
			v.setReference(newV.getReference());
		}
		if (newV.getStart() != null) {
			v.setStart(newV.getStart());
		}
		if (newV.getStrand() != null) {
			v.setStrand(newV.getStrand());
		}

		variantDao.save(v);

		return Response.status(200).build();
	}

	@DELETE
	@Path("{inputId}/variants/{variantId}")
	public Response deleteVariant(@PathParam(value = "inputId") String inputId,
			@PathParam(value = "variantId") String variantId) {
		Integer vid = Integer.parseInt(variantId);
		variantDao = morphiaService.getVariantDAO();
		Variant v = variantDao.findById(vid);

		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input i = inputDao.findById(iid);
		List<Variant> ivs = i.getVariants();
		ivs.remove(v);
		inputDao.save(i);
		variantDao.delete(v);

		return Response.status(200).build();
	}

	@DELETE
	@Path("{inputId}")
	public Response deleteInput(@PathParam(value = "inputId") String inputId) {
		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input i = inputDao.findById(iid);

		inputDao.delete(i);

		return Response.status(200).build();
	}
}

package edu.yale.mutadelic.jersey;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.pipeline.PipelineExecutor;

@Path("outputs")
public class OutputResource {

	@Inject
	MorphiaService morphiaService;
	private OutputDAO outputDao;
	private InputDAO inputDao;
	private UserDAO userDao;
	private WorkflowDAO workflowDao;

	public OutputResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addOutput(@QueryParam("user_id") String userId,
			@QueryParam("workflow_id") String workflowId,
			@QueryParam("input_id") String inputId) {
		User u = null;
		Workflow w = null;
		
		if (userId == null) {
			User defU = Defaults.getDefaultUser();
			userDao = morphiaService.getUserDAO();
			User testU = userDao.findByName(defU.getName());
			if (testU != null) {
				u = testU;
			}else {
				u = defU;
				userDao.save(defU);
			}
		}
		
		if (workflowId == null) {
			Workflow defW = Defaults.getDefaultWorkflow();
			workflowDao = morphiaService.getWorkflowDAO();
			Workflow testW = workflowDao.findByName(defW.getName());
			if (testW != null) {
				w = testW;
			} else {
				w = defW;
				workflowDao.save(defW);
			}
		}
		
		Integer iid = Integer.parseInt(inputId);
		inputDao = morphiaService.getInputDAO();
		Input input = inputDao.findById(iid);
		
		PipelineExecutor pex = morphiaService.getPipelineExecutor();
		AbfabProcessor ap = new AbfabProcessor(pex, w);
				
		List<Variant> vars = input.getVariants();
		List<AnnotatedVariant> avs = new ArrayList<>();
		for (Variant v : vars) {
			AnnotatedVariant av= ap.annotateVariant(v);
			avs.add(av);
		}
		
		Output o = new Output();
		o.setInput(iid);
		o.setOwner(u.getId());
		o.setWorkflow(w.getId());
		o.setResults(avs);
		
		outputDao.save(o);

		return Response.status(201).build();
	}

	@GET
	@Path("{outputId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Output getOutput(@PathParam("outputId") String outputId) {
		Integer oid = Integer.parseInt(outputId);
		outputDao = morphiaService.getOutputDAO();
		Output o = outputDao.findById(oid);
		return o;
	}

	@GET
	@Path("{outputId}/variants/{variantId}")
	@Produces(MediaType.APPLICATION_JSON)
	public AnnotatedVariant getVariant(@PathParam("outputId") String outputId,
			@PathParam("variantId") String variantId) {
		Integer oid = Integer.parseInt(outputId);
		Integer vid = Integer.parseInt(variantId) - 1;
		outputDao = morphiaService.getOutputDAO();
		Output o = outputDao.findById(oid);

		AnnotatedVariant variant = o.getResults().get(vid);
		return variant;
	}

	@GET
	@Path("{outputId}/variants")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AnnotatedVariant> getOutputVariants(
			@PathParam("outputId") String outputId) {
		Integer oid = Integer.parseInt(outputId);
		try {
			outputDao = morphiaService.getOutputDAO();
			Output o = outputDao.findById(oid);
			List<AnnotatedVariant> vars = o.getResults();
			return vars;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@DELETE
	@Path("{outputId}")
	public Response deleteOutput(@PathParam("outputId") String outputId) {
		Integer oid = Integer.parseInt(outputId);
		outputDao = morphiaService.getOutputDAO();
		Output o = outputDao.findById(oid);

		outputDao.delete(o);

		return Response.status(200).build();
	}
}

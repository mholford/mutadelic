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
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.Workflow;

@Path("workflows")
public class WorkflowResource {

	@Inject
	MorphiaService morphiaService;
	private WorkflowDAO workflowDao;
	private OutputDAO outputDao;

	public WorkflowResource() {

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Workflow> getAllWorkflows() {
		try {
			workflowDao = morphiaService.getWorkflowDAO();
			List<Workflow> asList = workflowDao.find().asList();
			return asList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Integer addUser(Workflow newW) {
		workflowDao = morphiaService.getWorkflowDAO();
		workflowDao.save(newW);

		return newW.getId();
	}

	@GET
	@Path("{workflowId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Workflow getWorkflow(
			@PathParam(value = "workflowId") String workflowId) {
		Integer wid = Integer.parseInt(workflowId);
		workflowDao = morphiaService.getWorkflowDAO();
		Workflow w = workflowDao.findById(wid);
		return w;
	}

	@GET
	@Path("{workflowId}/outputs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Output> getWorkflowOutputs(
			@PathParam(value = "workflowId") String workflowId) {
		Integer wid = Integer.parseInt(workflowId);
		try {
			outputDao = morphiaService.getOutputDAO();
			List<Output> outputs = outputDao.findByWorkflowId(wid);
			return outputs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{workflowId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editWorkflow(
			@PathParam(value = "workflowId") String workflowId, Workflow newW) {
		Integer wid = Integer.parseInt(workflowId);
		workflowDao = morphiaService.getWorkflowDAO();
		Workflow w = workflowDao.findById(wid);

		if (newW.getName() != null) {
			w.setName(newW.getName());
		}

		workflowDao.save(w);

		return Response.status(200).build();
	}

	@DELETE
	@Path("{workflowId}")
	public Response deleteWorkflow(
			@PathParam(value = "workflowId") String workflowId) {
		Integer wid = Integer.parseInt(workflowId);
		workflowDao = morphiaService.getWorkflowDAO();
		Workflow w = workflowDao.findById(wid);

		workflowDao.delete(w);

		return Response.status(200).build();
	}
}

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
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Workflow;

@Path("users")
public class UserResource {

	@Inject
	private MorphiaService morphiaService;
	private UserDAO userDao;
	private WorkflowDAO workflowDao;
	private InputDAO inputDao;
	private OutputDAO outputDao;

	public UserResource() {

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getAllUsers() {
		try {
			userDao = morphiaService.getUserDAO();
			List<User> asList = userDao.find().asList();
			return asList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Integer addUser(User newU) {
		userDao = morphiaService.getUserDAO();
		userDao.save(newU);
		
		return newU.getId();
	}
	
	@GET
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam(value = "userId") String userId) {
		Integer uid = Integer.parseInt(userId);
		userDao = morphiaService.getUserDAO();
		User u = userDao.findById(uid);
		return u;
	}
	
	@GET
	@Path("{userId}/workflows")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Workflow> getUserWorkflows(@PathParam(value="userId") String userId) {
		Integer uid = Integer.parseInt(userId);
		try {
			workflowDao = morphiaService.getWorkflowDAO();
			List<Workflow> workflow = workflowDao.findByUserId(uid);
			return workflow;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@GET
	@Path("{userId}/inputs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Input> getUserInputs(@PathParam(value="userId") String userId) {
		Integer uid = Integer.parseInt(userId);
		try {
			inputDao = morphiaService.getInputDAO();
			List<Input> inputs = inputDao.findByUserId(uid);
			return inputs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@GET
	@Path("{userId}/outputs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Output> getUserOutputs(@PathParam(value="userId") String userId) {
		Integer uid = Integer.parseInt(userId);
		try {
			outputDao = morphiaService.getOutputDAO();
			List<Output> outputs = outputDao.findByUserId(uid);
			return outputs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@POST
	@Path("{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editUser(@PathParam(value="userId") String userId, User newU) {
		Integer uid = Integer.parseInt(userId);
		userDao = morphiaService.getUserDAO();
		User u = userDao.findById(uid);
		
		if (newU.getEmail() != null) {
			u.setEmail(newU.getEmail());
		}
		if (newU.getName() != null) {
			u.setName(newU.getName());
		}
		userDao.save(u);
		
		return Response.status(200).build();
	}
	
	@DELETE
	@Path("{userId}")
	public Response deleteUser(@PathParam(value="userId") String userId) {
		Integer uid = Integer.parseInt(userId);
		userDao = morphiaService.getUserDAO();
		User u = userDao.findById(uid);
		
		userDao.delete(u);
		
		return Response.status(200).build();
	}
}
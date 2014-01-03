package edu.yale.mutadelic.jersey;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
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
import javax.ws.rs.core.Response.ResponseBuilder;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.ValueEntry;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.pipeline.PipelineExecutor;
import static edu.yale.abfab.NS.*;

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
	public Integer addOutput(@QueryParam("user_id") String userId,
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
			} else {
				u = defU;
				userDao.save(defU);
			}
		}

		if (workflowId == null) {
			Workflow defW;
			try {
				defW = Defaults.getDefaultWorkflow();

				workflowDao = morphiaService.getWorkflowDAO();
				Workflow testW = workflowDao.findByName(defW.getName());
				if (testW != null) {
					w = testW;
				} else {
					w = defW;
					workflowDao.save(defW);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
			AnnotatedVariant av = ap.annotateVariant(v);
			avs.add(av);
		}

		outputDao = morphiaService.getOutputDAO();
		Output o = new Output();
		o.setInput(iid);
		o.setOwner(u.getId());
		o.setWorkflow(w.getId());
		o.setResults(avs);

		outputDao.save(o);

		return o.getId();
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
	
	private File getExcelFile(Output o) throws Exception {
		String outfileName = String.format("mutadelic-output-%d.xls", o.getId());
		File output = new File(outfileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));

		// Write header
		bw.write(String
				.format("Flagged\tChromosome\tStrand\tStart\tEnd\tReference\tObserved\t"
						+ "Property Name\tProperty Value\tSignificant\n"));
		bw.flush();

		for (AnnotatedVariant av : o.getResults()) {
			for (ValueEntry ve : av.getValueEntries()) {
				bw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", av
						.isFlagged() ? "Y" : "N", av.getVariant()
						.getChromosome(), av.getVariant().getStrand(), av
						.getVariant().getStart(), av.getVariant().getEnd(), av
						.getVariant().getReference(), av.getVariant()
						.getObserved(), ve.getKey(), ve.getValue(), ve
						.getLevel().equals("UP") ? "Y" : "N"));
				bw.flush();
			}
		}
		
		bw.close();
		
		return output;
	}

	@GET
	@Path("{outputId}/excel")
	@Produces("application/vnd.ms-excel")
	public Response getExcel(@PathParam("outputId") String outputId) {
		Integer oid = Integer.parseInt(outputId);
		outputDao = morphiaService.getOutputDAO();
		Output o = outputDao.findById(oid);

		try {
			File f = getExcelFile(o);

			ResponseBuilder resp = Response.ok(f);
			String outfileName = String.format("mutadelic-output-%d.xls", oid);
			resp.header("Content-Disposition",
					String.format("attachment; filename=%s", outfileName));
			return resp.build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
	
	private File getRDFFile(Output o) {
		String outfileName = String.format("mutadelic-output-%d.rdf", o.getId());
		File output = new File(outfileName);
		workflowDao = morphiaService.getWorkflowDAO();
		
		String workflowOnt = workflowDao.findById(o.getWorkflow()).getExecDoc();
		
		List<String> outputOnts = new ArrayList<>();
		for (AnnotatedVariant av:o.getResults()) {
			outputOnts.add(av.getRdf());
		}
		
		Abductor abductor = new HermitAbductor("test");
		abductor.setNamespace(NS);
		DLController dl = abductor.getDLController();
		dl.load(new StringReader(workflowOnt), false);
		
		for (String oo: outputOnts) {
			Abductor a2 = new HermitAbductor("a2");
			a2.setNamespace(NS);
			DLController dl2 = a2.getDLController();
			dl2.load(new StringReader(oo), false);
			for (DLAxiom<?> ax : dl2.getAxioms()) {
				dl.addAxiom(ax);
			}
		}
		try {
			dl.saveOntology(new FileOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}

	@GET
	@Path("{outputId}/rdf")
	@Produces("text/plain")
	public Response getRDF(@PathParam("outputId") String outputId) {
		Integer oid = Integer.parseInt(outputId);
		outputDao = morphiaService.getOutputDAO();
		Output o = outputDao.findById(oid);
		
		try {
			File f = getRDFFile(o);
			
			ResponseBuilder resp = Response.ok(f);
			String outfileName = String.format("mutadelic-output-%d.rdf", oid);
			resp.header("Content-Disposition", String.format("attachment; filename=%s", outfileName));
			return resp.build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
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

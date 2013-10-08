package edu.yale.mutadelic.ncbivr;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class NCBIVariationReporter {

	private final String URI = "http://www.ncbi.nlm.nih.gov/projects/SNP/VariantAnalyzer/var_rep.cgi";
	public final static String ASSEMBLY_37 = "GCF_000001405.13";

	public String analyze(String organism, String assembly, String query)
			throws Exception {
		String result = null;

		CloseableHttpClient hc = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(30000).build();
		HttpPost hp = new HttpPost(URI);
		hp.setConfig(requestConfig);
		HttpEntity he = MultipartEntityBuilder
				.create()
				.addPart("organism",
						new StringBody(organism, ContentType.TEXT_PLAIN))
				.addPart("source-assembly",
						new StringBody(assembly, ContentType.TEXT_PLAIN))
				.addPart("annot1",
						new StringBody(query, ContentType.TEXT_PLAIN)).build();
		hp.setEntity(he);
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		
		try {
			result = hc.execute(hp, responseHandler);
			
		} finally {
			hc.close();
		}
		

		return result;
	}
}

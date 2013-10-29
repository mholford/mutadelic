package edu.yale.mutadelic.ncbivr;

import java.util.HashMap;
import java.util.Map;

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
	public final static String DEFAULT_ASSEMBLY = ASSEMBLY_37;
	public final static String HUMAN = "9606";
	public final static Map<String, String> CHR_NUC = new HashMap<>();
	static {
		for (int i = 1; i <= 22; i++) {
			String is = String.valueOf(i);
			int padding = 6 - is.length();
			String zeroPad = "";
			for (int j = 0; j < padding; j++) {
				zeroPad += "0";
			}
			String nuc = String.format("NC_%s%s.10", zeroPad, is);
			CHR_NUC.put(String.format("Chr%d", i), nuc);
		}
		CHR_NUC.put("ChrX", "NC_000023.10");
		CHR_NUC.put("ChrY", "NC_000024.9");
	}

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
			String jobIdLine = result.substring(0, result.indexOf("\n") + 1);
			result = jobIdLine + result.substring(result.indexOf("##"));
		} finally {
			hc.close();
		}

		return result;
	}
}

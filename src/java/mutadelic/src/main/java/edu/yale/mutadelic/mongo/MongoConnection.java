package edu.yale.mutadelic.mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoConnection {

	public static final String MONGO_DB = "mutadelic";
	public static final String MONGO_ID = "_id";
	public static final String ONE_K_GENOME_TABLE = "onekgenome";
	public static final String ONE_K_CHROMOSOME = "chr";
	public static final String ONE_K_POSITION = "pos";
	public static final String ONE_K_REFERENCE = "ref";
	public static final String ONE_K_OBSERVED = "obs";
	public static final String ONE_K_MAF = "maf";
	public static final String ONE_K_AFR_MAF = "afr";
	public static final String ONE_K_AMR_MAF = "amr";
	public static final String ONE_K_ASN_MAF = "asn";
	public static final String ONE_K_EUR_MAF = "eur";
	public static final String PHYLOP_TABLE = "phylop";
	public static final String PHYLOP_VALUES = "vals";
	public static final String SIFT_TABLE = "sift";
	public static final String SIFT_VALUES = "vals";
	public static final String RCMDB_TABLE = "rcmdb";
	public static final String RCMDB_DISEASE= "disease";
	public static final String RCMDB_GENE= "gene";
	public static final String RCMDB_REGION= "region";
	public static final String RCMDB_HGVSP= "hgvsp";
	public static final String RCMDB_MUT_TYPE= "type";
	public static final String RCMDB_MUT_NAME= "name";
	public static final String RCMDB_PMID= "pmid";
	public static final String RCMDB_REFS = "refs";	
	public static final String CCDS_IDX_TABLE = "ccds";
	public static final String CCDS_IDX_NAME = "name";
	public static final String CCDS_POS_TABLE = "ccdspos";
	public static final String CCDS_POS_START = "start";
	public static final String CCDS_POS_END = "end";
	public static final String CCDS_REF_TABLE = "ccdsref";
	public static final String CCDS_REF_ENSEMBL = "ensembl";
	public static final String CCDS_REF_REFSEQ = "refseq";
	public static final String CCDS_REF_HAVANA = "havana";

	private static MongoConnection INSTANCE;
	private MongoClient mongoClient;
	private DB mutadelicDB;

	private MongoConnection() {
		try {
			mongoClient = new MongoClient("localhost", 27017);
			mutadelicDB = mongoClient.getDB(MONGO_DB);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static MongoConnection instance() {
		if (INSTANCE == null) {
			INSTANCE = new MongoConnection();
		}
		return INSTANCE;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public DB getMutadelicDB() {
		return mutadelicDB;
	}

	public DBCollection getOneKTable() {
		return mutadelicDB.getCollection(ONE_K_GENOME_TABLE);
	}
	
	public DBCollection getPhylopTable() {
		return mutadelicDB.getCollection(PHYLOP_TABLE);
	}
	
	public DBCollection getSiftTable() {
		return mutadelicDB.getCollection(SIFT_TABLE);
	}
	
	public DBCollection getCCDSPositionTable() {
		return mutadelicDB.getCollection(CCDS_POS_TABLE);
	}
	
	public DBCollection getCCDSRefTable() {
		return mutadelicDB.getCollection(CCDS_REF_TABLE);
	}
	
	public DBCollection getCCDSIndexTable() {
		return mutadelicDB.getCollection(CCDS_IDX_TABLE);
	}
	
	public DBCollection getRCMDBTable() {
		return mutadelicDB.getCollection(RCMDB_TABLE);
	}
}

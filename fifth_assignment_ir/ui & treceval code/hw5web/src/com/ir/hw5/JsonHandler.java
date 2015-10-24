package com.ir.hw5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

@Path("/qrel")
public class JsonHandler {
	static Map<String, QrelBean> qrel = new TreeMap<String, QrelBean>();
	static File file;
	static BufferedWriter out ;
	
	public JsonHandler() throws IOException {
		super();

		file = new File("C:/Users/Nitin/Assign5/" + "qRelFile" + ".txt");
		
		if(!(file.exists())){
			System.out.println("File Doesnot Exists..");
			file.createNewFile();
		}
		
		out = new BufferedWriter(new FileWriter(file));
		
	}

	/*@GET
	@Path("/{Id}")
	public String getDetails(@PathParam("Id") String id) {

		System.out.println("IN GET" + id);
		return "Hello World " + id;

	}*/

	

	@POST
	@Path("/readAdd")
	@Consumes("application/json")
	public String getQrelData(String json) throws JSONException {
		System.out.println("In getQrelData...");

		JSONObject j = new JSONObject(json);
		System.out.println(j);

		QrelBean q = new QrelBean();

		String queryId=(String) j.get("queryId");
		String accessor = (String) j.get("accessor");
		String docId = (String) j.get("docId");
		String rank = (String) j.get("rank");
		String fileName = (String) j.get("fileName");

		/*System.out.println("Accessor:: " + j.get("accessor"));
		System.out.println("DocId :: " + j.get("docId"));
		System.out.println("Rank:: " + j.get("rank"));*/
		q.setQueryId(queryId);
		q.setAccessor(accessor);
		q.setDocId(docId);
		q.setRank(rank);
		
		qrel.put(docId, q);
		new MapHandler(docId,q);

		//System.out.println("Before Writing");
		//writeToFile();
		//System.out.println("After Writing");

		System.out.println("Map Size:::: "+ qrel.size());
		int mapSize = qrel.size();
		
		return (String.valueOf(mapSize));

	}

	@POST
	@Path("/writeFile")
	public String writeToFile() {
		try {
			MapHandler mp = new MapHandler();
			Map<String, QrelBean> qm=mp.returnMap();
			System.out.println("In File Creation..");
			System.out.println("map Size:::: " + qm.size());

			int count = 1;
			for (Map.Entry<String, QrelBean> qr : qm.entrySet()) {
				if (count <= 200) {
					String str = qr.getValue().getQueryId()+ " " + qr.getValue().getAccessor() + " "
							+ qr.getValue().getDocId() + " "
							+ qr.getValue().getRank();
					out.write(str);
					out.newLine();
					count++;
				} else
					break;

			}

			out.close();
			return "success";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}

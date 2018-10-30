package com.sjsu.cloud.proj.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class ClientUtils {

	public Response sendPostRequest(String URL,String jsonPayload) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		return target.request().post(Entity.entity(jsonPayload, MediaType.APPLICATION_JSON));
	}
	
	public Response sendGetRequest(String URL) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		return target.request().get();
	}
	
	public Response sendPostRequestForm(String URL, String userName) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		Form form = new Form();
		form.param("username", userName);
		return target.request().post(Entity.form(form), Response.class);
	}
	
	public Response sendFileUploadRequest(String URL, String uploadFile, String keyName, String fileSize, String userName) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		Form form = new Form();
		form.param("uploadfile", uploadFile);
		form.param("keyname", keyName);
		form.param("filesize", fileSize);
		form.param("username", userName);
		return target.request().post(Entity.form(form), Response.class);
	}
	
	public void downloadFile(String URL, String keyName, String DownloadPath) throws FileNotFoundException, IOException {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		Form form = new Form();
		form.param("keyname", keyName);
		Response response = target.request().post(Entity.form(form), Response.class);
		
		String binarySWF = response.readEntity(String.class);  
		FileOutputStream fos = new FileOutputStream(new File("myfile.swf"));
		byte[] SWFByteArray = binarySWF.getBytes();       
		fos.write(SWFByteArray);
		fos.flush();
		fos.close();
		
	}
	
	public Response deleteFile(String URL, String keyname) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);
		Form form = new Form();
		form.param("keyname", keyname);
		return target.request().post(Entity.form(form), Response.class);
	}
}

package com.sjsu.cloud.proj.service.impl;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.sjsu.cloud.proj.exceptions.ApplicationException;
import com.sjsu.cloud.proj.model.UpdateFile;
import com.sjsu.cloud.proj.model.User;
import com.sjsu.cloud.proj.repository.UserRepository;
import com.sjsu.cloud.proj.repository.impl.NativeDBRepositoryImpl;
import com.sjsu.cloud.proj.service.AWSS3Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.List;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;

@Service
public class S3ServiceImpl implements AWSS3Services {

	@Autowired
	protected TransferManager tm;
	
	@Autowired
	private AmazonS3 client;

	@Value("${jsa.s3.bucket}")
	protected String bucketName;
	
	@Autowired
	NativeDBRepositoryImpl dbRepository;
	
	@Autowired
	public UserRepository userRepository;
	
	@Override
	public void uploadFile(String key, String fileName, String fileSize, String userName) throws ApplicationException {
		
		User userInfo = dbRepository.getUserInfo(userName);
		if (userInfo == null) {
			throw new ApplicationException(403, "User not found");
		}
		
		File file = new File(fileName);
		System.out.println(file.length());
		final PutObjectRequest request = new PutObjectRequest(bucketName, key, new File(fileName));
		request.setGeneralProgressListener(new ProgressListener() {
			@Override
			public void progressChanged(ProgressEvent progressEvent) {
				long bytes = progressEvent.getBytesTransferred();
			}
		});

		Upload upload = tm.upload(request);
		
		String[] name = fileName.split("\\/");
		String fname = name[name.length-1];
		UpdateFile fileInfo = new UpdateFile();
		fileInfo.setName(fname);
		fileInfo.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		fileInfo.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
		fileInfo.setDescription("upload:" + fname);
		if (fileSize == null)
			fileInfo.setFile_size(null); 
		else 
			fileInfo.setFile_size(fileSize); 
		
		fileInfo.setPath(key);
		fileInfo.setUserid(userInfo.getId());
		
		try {
			upload.waitForCompletion(); 
		} catch (AmazonServiceException e) {
				e.getMessage();
		} catch (AmazonClientException e) {
			e.getMessage();
		} catch (InterruptedException e) {
			e.getMessage();
		} 
		userRepository.save(fileInfo);
	}

	@Override
	public byte[] downloadFile(String keyName) {

		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName);
		S3Object s3Object = client.getObject(getObjectRequest);
		S3ObjectInputStream objectInputStream = s3Object.getObjectContent();

		byte[] bytes = null;
		try {
			bytes = IOUtils.toByteArray(objectInputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String fileName = null;
		try {
			fileName = URLEncoder.encode(keyName, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		HttpHeaders headers = new HttpHeaders();
//		headers.set("fileName", "keyName");
//		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(bytes, headers, HttpStatus.OK);
//		    return responseEntity;
		return bytes;
	}
	
	@Override
	public void deleteFile(String keyName) {
        try {
        		client.deleteObject(new DeleteObjectRequest(bucketName, keyName));
        		dbRepository.deleteFile(keyName);
        } catch(AmazonServiceException ex) {
        		ex.getMessage();
        }
	}
	
	@Override
	public List<UpdateFile> getUserFiles(String userName) throws ApplicationException {
		
		User userInfo = dbRepository.getUserInfo(userName);
		if (userInfo == null) {
			throw new ApplicationException(403, "User not found");
		}
		
		List<UpdateFile> files = dbRepository.getUserFiles(userInfo.getId());
		return files;
	}

	@Override
	public List<UpdateFile> getAllFiles() {
		
		List<UpdateFile> files = dbRepository.getAllUserFiles();
		return files;
	}
}

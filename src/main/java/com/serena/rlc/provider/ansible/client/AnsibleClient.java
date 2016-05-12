/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.ansible.client;

import com.serena.rlc.provider.ansible.domain.Job;
import com.serena.rlc.provider.ansible.domain.JobTemplate;
import com.serena.rlc.provider.ansible.exception.AnsibleClientException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * Ansible Tower Client
 * @author klee@serena.com
 */
@Component
public class AnsibleClient {
    private static final Logger logger = LoggerFactory.getLogger(AnsibleClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";

    private String ansibleUrl;
    private String ansibleUsername;
    private String ansiblePassword;
    private String apiVersion;
    private String ansibleToken;

    private DefaultHttpClient httpClient;
    private HttpHost httpHost = null;


    public AnsibleClient() {

    }
    
    public AnsibleClient(String url, String apiVersion, String username, String password) throws AnsibleClientException{
        this.ansibleUrl = url;
        this.apiVersion = apiVersion;
        this.ansibleUsername = username;
        this.ansiblePassword = password;
        this.createConnection(this.ansibleUrl, this.apiVersion, this.ansibleUsername, this.ansiblePassword);
    }

    public String getAnsibleUrl() {
        return ansibleUrl;
    }

    public void setAnsibleUrl(String url) {
        this.ansibleUrl = url;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getAnsibleUsername() {
        return ansibleUsername;
    }

    public void setAnsibleUsername(String username) {
        this.ansibleUsername = username;
    }

    public String getAnsiblePassword() {
        return ansiblePassword;
    }

    public void setAnsiblePassword(String password) {
        this.ansiblePassword = password;
    }

    private String getAnsibleToken() {
        return ansibleToken;
    }

    private void setAnsibleToken(String ansibleToken) {
        this.ansibleToken = ansibleToken;
    }

    /**
     * Create a new connection to Ansible, storing the authentication toke for future calls.
     *
     * @param url  the url to Ansible, e.g. https://servername
     * @param username  the username of the Ansible user
     * @param password  the password of the Ansible user
     * @throws AnsibleClientException
     */
    public void createConnection(String url, String apiVersion, String username, String password) throws AnsibleClientException {
        // create an acceptingTrustStrategy to accept all connections - insecure but flexible
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };
        SSLSocketFactory sf = null;
        try {
            sf = new SSLSocketFactory(acceptingTrustStrategy,
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AnsibleClientException("Unable to create SSL Socket Factory", ex);

        }
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", 443, sf));
        ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);

        this.ansibleUrl = url;
        this.ansibleUsername = username;
        this.ansiblePassword = password;
        this.apiVersion = apiVersion;
        this.httpClient = new DefaultHttpClient(ccm);
        String[] urlParts = this.ansibleUrl.split(":");
        if (urlParts.length > 2) {
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/",""), Integer.parseInt(urlParts[2]), urlParts[0]);
        } else {
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/", ""), 443, urlParts[0]);
        }
        this.httpClient.getParams().setParameter(ClientPNames.DEFAULT_HOST, httpHost);

        try {
            this.ansibleToken = getAuthToken();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleClientException("Unable to retrieve authentication token", e);
        }
    }

    /**
     * Get a list of all the Job Templates
     * @return a list of JobTemplate objects
     * @throws AnsibleClientException
     */
    public List<JobTemplate> getJobTemplates() throws AnsibleClientException {
        logger.debug("Retrieving Ansible Job Templates");
        
        String jobsResponse = processGet(getAnsibleUrl(), "/job_templates/");
        logger.debug(jobsResponse);

        List<JobTemplate> jobTemplates = JobTemplate.parse(jobsResponse);
        return jobTemplates;
    }

    /**
     * Launch a new Ansible Job from a Job Template
     * @param jobTemplateId  the id of the job template
     * @param templateData  any additional data to send with the request
     * @return the Job that was started as a result of the request
     * @throws AnsibleClientException
     */
    public Job launchJob(Long jobTemplateId, String templateData) throws AnsibleClientException {
        logger.debug("Launching Ansible Job from Job Template id: {} with data: {}", jobTemplateId, templateData);

        // TODO: pass templateData to Post
        String result = processPost(getAnsibleUrl(), "/job_templates/"+jobTemplateId.toString()+"/launch/", templateData);
        Long jobId = Job.parseJobResult(result);
        logger.debug("Successfully launched Ansible Job id: {}", jobId);

        Job job = getJob(jobId);
        return job;
    }

    /**
     * Get the details of a specific Job
     * @param jobId  the id of the Job to retrieve
     * @return a Job
     * @throws AnsibleClientException
     */
    public Job getJob(Long jobId) throws AnsibleClientException {
        logger.debug("Retrieving Ansible Job id: {}", jobId);

        String result = processGet(getAnsibleUrl(), "/jobs/"+jobId.toString()+"/");
        Job job = Job.parseSingle(result);
        logger.debug("Found Ansible Job: \"{}\"", job.getName());

        return job;
    }

    /**
     * Get the status of a specific Job
     * @param jobId  the id of the Job to retrieve
     * @return  a String containing the status of the Job
     * @throws AnsibleClientException
     */
    public String getJobStatus(Long jobId) throws AnsibleClientException {
        logger.debug("Retrieving status of Ansible Job id: {}", jobId);

        String result = processGet(getAnsibleUrl(), "/jobs/"+jobId.toString()+"/");
        Job job = Job.parseSingle(result);

        logger.debug("Found Ansible Job: \"{}\" with status \"{}\"", job.getName(), job.getStatus());

        return job.getStatus();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Login to ansible and retrieve an authentication token for subsequent calls.
     * @return an Ansible authentication token
     * @throws AnsibleClientException, IOException, GeneralSecurityException
     */
    public String getAuthToken() throws AnsibleClientException, IOException, GeneralSecurityException {
        String uri = createUrl(getAnsibleUrl(), "/authtoken/");
        String postData = null;

        if (getAnsibleUsername() != null && !StringUtils.isEmpty(getAnsibleUsername())) {
            postData = "{\"username\": \"" + getAnsibleUsername() + "\", \"password\": \"" + getAnsiblePassword() + "\"}";
        }

        logger.debug("Start executing Ansible POST request to url=\"{}\" with payload={}", uri, postData);
        HttpPost postRequest = new HttpPost(uri);

        postRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        String result = "";
        String token = "";

        try {
            postRequest.setEntity(new StringEntity(postData));

            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
            logger.debug("End executing Ansible POST request to url=\"{}\" and received this result={}", uri, result);

            JSONParser parser = new JSONParser();
            try {
                Object parsedObject = parser.parse(result);
                JSONObject jsonObject = (JSONObject) parsedObject;
                if (jsonObject != null) {
                    token = (String) jsonObject.get("token");
                }
                return token;
            } catch (org.json.simple.parser.ParseException e) {
                logger.error("Error while parsing input JSON - " + result, e);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleClientException("Server not available", e);
        }

        return null;
    }

    /**
     * Execute a get request to Ansible.
     *
     * @param ansibleUrl  the URL to Ansible
     * @param getPath  the path for the specific request
     * @return String containing the response body
     * @throws AnsibleClientException
     */
    public String processGet(String ansibleUrl, String getPath) throws AnsibleClientException {
        String uri = createUrl(ansibleUrl, getPath);
        String token = getAnsibleToken();
        if (token == null || StringUtils.isEmpty(token)) {
            try {
                token = getAuthToken();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new AnsibleClientException("Unable to retrieve authentication token", e);
            }
        }

        logger.debug("Start executing Ansible GET request to url=\"{}\"", uri);

        HttpGet getRequest = new HttpGet(uri);
        
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
        //Authorization: Token 8f17825cf08a7efea124f2638f3896f6637f8745
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, "token " + token);
        String result = "";

        try {
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleClientException("Server not available", e);
        }

        logger.debug("End executing Ansible GET request to url=\"{}\" and received this result={}", uri, result);
        
        return result;
    }

    /**
     * Execute a post request to Ansible.
     *
     * @param ansibleUrl  the URL to Ansible
     * @param postPath  the path for the specific request
     * @param postBody the data to send with the request
     * @return String containing the response body
     * @throws AnsibleClientException
     */
    public String processPost(String ansibleUrl, String postPath, String postBody) throws AnsibleClientException {
        String uri = createUrl(ansibleUrl, postPath);
        String token = getAnsibleToken();
        if (token == null || StringUtils.isEmpty(token)) {
            try {
                token = getAuthToken();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new AnsibleClientException("Unable to retrieve authentication token", e);
            }
        }

        logger.debug("Start executing Ansible POST request to url=\"{}\" with data: {}", uri, postBody);

        HttpPost postRequest = new HttpPost(uri);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        postRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
        postRequest.addHeader(HttpHeaders.AUTHORIZATION, "token " + token);
        try {
            postRequest.setEntity(new StringEntity(postBody,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleClientException("Error creating body for POST request", e);
        }
        String result = "";

        try {
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED &&
                    response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleClientException("Server not available", e);
        }

        logger.debug("End executing Ansible POST request to url=\"{}\" and received this result={}", uri, result);

        return result;
    }

    /**
     * Create an Ansible URL from base and path
     * @param ansibleUrl  the base Ansible URL, e.g. https://servername
     * @param path  the path to the request
     * @return a Stirng containing a complete Ansible path
     */
    public String createUrl(String ansibleUrl, String path) {
        String result;
        // if path doesn't start with "/" add it
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // if ansibleUrl ends with "/" remove it
        if (ansibleUrl.endsWith("/")) {
            ansibleUrl.substring(0, ansibleUrl.length()-1);
        }

        // does path already contains api version?
        if (path.contains("api")) {
            result = ansibleUrl + path;
        } else {
            result = ansibleUrl + "/api/" + getApiVersion() + path;
        }
        return result;
    }

    /**
     * Create a formatted Ansible error and message from a HTTP response
     * @param response  the response to parse
     * @return AnsibleClientException
     */
    private AnsibleClientException createHttpError(HttpResponse response) {
        String message;
        try {
            StatusLine statusLine = response.getStatusLine();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuffer responsePayload = new StringBuffer();
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                responsePayload.append(line);
            }

            message = String.format("request not successful: %d %s. Reason: %s", statusLine.getStatusCode(), HttpStatus.getStatusText(statusLine.getStatusCode()), responsePayload);

            logger.info(message);

            if (new Integer(HttpStatus.SC_UNAUTHORIZED).equals(statusLine.getStatusCode())) {
                return new AnsibleClientException("Ansible: Invalid credentials provided.");
            } else if (new Integer(HttpStatus.SC_NOT_FOUND).equals(statusLine.getStatusCode())) {
                return new AnsibleClientException("Ansible: Request URL not found.");
            } else if (new Integer(HttpStatus.SC_BAD_REQUEST).equals(statusLine.getStatusCode())) {
                return new AnsibleClientException("Ansible: Bad request. " + responsePayload);
            }
        } catch (IOException e) {
            return new AnsibleClientException("Ansible: Can't read response");
        }

        return new AnsibleClientException(message);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    static public void main(String[] args) throws AnsibleClientException {

        /*
        // prompt for and iterate over extra_vars, e.g. "extra_vars": "{\"username\": \"admin\", \"environment\": \"test\", \"password\": \"password\"}",
        String jsonString = "{\"extra_vars\": {\"username\": \"admin\", \"environment\": \"test\", \"password\": \"password\"}}";
        System.out.println(jsonString);
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) parsedObject;
            JSONObject extraVars = (JSONObject) jsonObject.get("extra_vars");

            for(Iterator iterator = extraVars.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                System.out.println(key + ":" + extraVars.get(key));
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + jsonString, e);
        }
        System.exit(0);*/

        JobTemplate firstJt = null;

        AnsibleClient ac = new AnsibleClient("https://ec2-54-194-87-69.eu-west-1.compute.amazonaws.com", "v1", "admin", "Pa55w0rd");
        String response = null;
        try {
            response = ac.getAuthToken();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Authentication token: " + response);

        List<JobTemplate> jts = ac.getJobTemplates();
        for (JobTemplate jt: jts) {
            if (firstJt == null) firstJt = jt;
            System.out.println("Found Job Template " + jt.getId() + " - " + jt.getName());
            System.out.println("URL: " + jt.getUrl());
            System.out.println("Description: " + jt.getDescription());
        }

        Job job = ac.launchJob(firstJt.getId(), "");
        System.out.println("Started job with id: " + job.getId().toString());
        System.out.println("Name: " + job.getName());
        System.out.println("Status: " + job.getStatus());

        int pollCount = 0;
        String jobStatus = null;
        while (pollCount < 100) {
            try {
                Thread.sleep(6000);
                jobStatus = ac.getJobStatus(job.getId());
                System.out.println("Status: " + jobStatus);
            } catch (AnsibleClientException ex) {
                logger.debug ("Error checking job status ({}) - {}", job.getId(), ex.getMessage());
            } catch (InterruptedException ex) {
            }
            if (jobStatus != null && (jobStatus.equals("successful") || jobStatus.equals("failed"))) {
                break;
            }

            pollCount++;
        }

        if (jobStatus != null && jobStatus.equals("successful")) {
            System.out.println("Job " + job.getName() + " has succeeded");
        } else {
            System.out.println("Job " + job.getName() + " has failed or its status cannot be retrieved.");
        }
        
    }
}

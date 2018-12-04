/* ===========================================================================
 *  Copyright (c) 2018 Micro Focus. All rights reserved.
 *
 *  Use of the Sample Code provided by Micro Focus is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Micro Focus software product(s) that you have licensed separately from Micro Focus.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Micro Focus from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any eventId or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Micro Focus software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Micro Focus.  You agree to maintain the
 *  Confidential Information in strict confidence for Micro Focus.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Micro Focus of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Micro Focus as used herein shall refer to Micro Focus,
 *  and its affiliates.  An entity shall be considered to be an
 *  affiliate of Micro Focus if it is an entity that controls, is controlled by,
 *  or is under common control with Micro Focus.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Micro Focus.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */

package com.serena.rlc.provider.ansible.client;

import com.serena.rlc.provider.ansible.domain.Job;
import com.serena.rlc.provider.ansible.domain.JobTemplate;
import com.serena.rlc.provider.ansible.domain.Project;
import com.serena.rlc.provider.ansible.exception.AnsibleTowerClientException;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * Ansible Tower Client
 * @author kevin.lee@microfocus.com
 */
@Component
public class AnsibleTowerClient {
    private static final Logger logger = LoggerFactory.getLogger(AnsibleTowerClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";

    private String ansibleUrl;
    private String ansibleUsername;
    private String ansiblePassword;
    private String apiVersion;
    private String ansibleToken;

    private DefaultHttpClient httpClient;
    private HttpHost httpHost = null;
    private int ansiblePort = 443;


    public AnsibleTowerClient() {

    }
    
    public AnsibleTowerClient(String url, String apiVersion, String username, String password) throws AnsibleTowerClientException {
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
     * @throws AnsibleTowerClientException
     */
    public void createConnection(String url, String apiVersion, String username, String password) throws AnsibleTowerClientException {
        // create an acceptingTrustStrategy to accept all connections - less secure but flexible
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
            throw new AnsibleTowerClientException("Unable to create SSL Socket Factory", ex);

        }

        this.ansibleUrl = url;
        this.ansibleUsername = username;
        this.ansiblePassword = password;
        this.apiVersion = apiVersion;
        String[] urlParts = this.ansibleUrl.split(":");
        if (urlParts.length > 2) {
            ansiblePort = Integer.parseInt(urlParts[2]);
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/",""), ansiblePort , urlParts[0]);
        } else {
            if (urlParts[0].equals("https"))
                ansiblePort = 443;
            else
                ansiblePort = 80;
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/", ""), ansiblePort, urlParts[0]);
        }
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(urlParts[0], ansiblePort, sf));
        ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);
        this.httpClient = new DefaultHttpClient(ccm);
        this.httpClient.getParams().setParameter(ClientPNames.DEFAULT_HOST, httpHost);

        try {
            this.ansibleToken = getAuthToken();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleTowerClientException("Unable to retrieve authentication token", e);
        }
    }

    /**
     * Get a list of all the Projects
     * @return a list of Project objects
     * @throws AnsibleTowerClientException
     */
    public List<Project> getProjects() throws AnsibleTowerClientException {
        logger.debug("Retrieving Ansible Projects");

        String projResponse = processGet(getAnsibleUrl(), "/projects/");
        logger.debug(projResponse);

        List<Project> projTemplates = Project.parse(projResponse);
        return projTemplates;
    }

    /**
     * Get the details of a specific Job Template
     * @param jobTemplateId  the id of the Job Template to retrieve
     * @return a Job Template
     * @throws AnsibleTowerClientException
     */
    public JobTemplate getJobTemplate(Long jobTemplateId) throws AnsibleTowerClientException {
        logger.debug("Retrieving Ansible Job Template id: {}", jobTemplateId);

        String result = processGet(getAnsibleUrl(), "/job_templates/"+jobTemplateId.toString()+"/");
        System.out.println(result);
        JobTemplate jobTemplate = JobTemplate.parseSingle(result);
        logger.debug("Found Ansible Job Template: \"{}\"", jobTemplate.getName());

        return jobTemplate;
    }

    /**
     * Get a list of all the Job Templates
     * @param projectId  the project to get the templates for
     * @return a list of JobTemplate objects
     * @throws AnsibleTowerClientException
     */
    public List<JobTemplate> getJobTemplates(Long projectId) throws AnsibleTowerClientException {
        logger.debug("Retrieving Ansible Job Templates for project id: {}" , projectId);
        
        String jobsResponse = processGet(getAnsibleUrl(), "/job_templates/?project__id="+projectId.toString());
        logger.debug(jobsResponse);

        List<JobTemplate> jobTemplates = JobTemplate.parse(jobsResponse);
        return jobTemplates;
    }

    /**
     * Launch a new Ansible Job from a Job Template
     * @param jobTemplateId  the id of the job template
     * @param templateData  any additional data to send with the request
     * @return the Job that was started as a result of the request
     * @throws AnsibleTowerClientException
     */
    public Job launchJob(Long jobTemplateId, String templateData) throws AnsibleTowerClientException {
        logger.debug("Launching Ansible Job from Job Template id: {} with data: {}", jobTemplateId, templateData);

        // TODO: pass templateData to Post
        String result = processPost(getAnsibleUrl(), "/job_templates/"+jobTemplateId.toString()+"/launch/", templateData);
        System.out.println(result);
        Long jobId = Job.parseJobResult(result);
        logger.debug("Successfully launched Ansible Job id: {}", jobId);

        Job job = getJob(jobId);
        return job;
    }

    /**
     * Get the details of a specific Job
     * @param jobId  the id of the Job to retrieve
     * @return a Job
     * @throws AnsibleTowerClientException
     */
    public Job getJob(Long jobId) throws AnsibleTowerClientException {
        logger.debug("Retrieving Ansible Job id: {}", jobId);

        String result = processGet(getAnsibleUrl(), "/jobs/"+jobId.toString()+"/");
        System.out.println(result);
        Job job = Job.parseSingle(result);
        logger.debug("Found Ansible Job: \"{}\"", job.getName());

        return job;
    }

    /**
     * Get the status of a specific Job
     * @param jobId  the id of the Job to retrieve
     * @return  a String containing the status of the Job
     * @throws AnsibleTowerClientException
     */
    public String getJobStatus(Long jobId) throws AnsibleTowerClientException {
        logger.debug("Retrieving status of Ansible Job id: {}", jobId);

        String result = processGet(getAnsibleUrl(), "/jobs/"+jobId.toString()+"/");
        System.out.println(result);
        Job job = Job.parseSingle(result);

        logger.debug("Found Ansible Job: \"{}\" with status \"{}\"", job.getName(), job.getStatus());

        return job.getStatus();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Login to ansible and retrieve an authentication token for subsequent calls.
     * @return an Ansible authentication token
     * @throws AnsibleTowerClientException, IOException, GeneralSecurityException
     */
    public String getAuthToken() throws AnsibleTowerClientException, IOException, GeneralSecurityException {
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
            throw new AnsibleTowerClientException("Server not available", e);
        }

        return null;
    }

    /**
     * Execute a get request to Ansible.
     *
     * @param ansibleUrl  the URL to Ansible
     * @param getPath  the path for the specific request
     * @return String containing the response body
     * @throws AnsibleTowerClientException
     */
    public String processGet(String ansibleUrl, String getPath) throws AnsibleTowerClientException {
        String uri = createUrl(ansibleUrl, getPath);
        String token = getAnsibleToken();
        if (token == null || StringUtils.isEmpty(token)) {
            try {
                token = getAuthToken();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new AnsibleTowerClientException("Unable to retrieve authentication token", e);
            }
        }

        logger.debug("Start executing Ansible GET request to url=\"{}\"", uri);

        HttpGet getRequest = new HttpGet(uri);
        
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, "Token " + token);
        String result = "";

        System.out.println(getRequest.toString());

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
            throw new AnsibleTowerClientException("Server not available", e);
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
     * @throws AnsibleTowerClientException
     */
    public String processPost(String ansibleUrl, String postPath, String postBody) throws AnsibleTowerClientException {
        String uri = createUrl(ansibleUrl, postPath);
        String token = getAnsibleToken();
        if (token == null || StringUtils.isEmpty(token)) {
            try {
                token = getAuthToken();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new AnsibleTowerClientException("Unable to retrieve authentication token", e);
            }
        }

        logger.debug("Start executing Ansible POST request to url=\"{}\" with data: {}", uri, postBody);

        HttpPost postRequest = new HttpPost(uri);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        postRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
        postRequest.addHeader(HttpHeaders.AUTHORIZATION, "Token " + token);
        try {
            postRequest.setEntity(new StringEntity(postBody,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            throw new AnsibleTowerClientException("Error creating body for POST request", e);
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
            throw new AnsibleTowerClientException("Server not available", e);
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
     * @return AnsibleTowerClientException
     */
    private AnsibleTowerClientException createHttpError(HttpResponse response) {
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
                return new AnsibleTowerClientException("Ansible: Invalid credentials provided.");
            } else if (new Integer(HttpStatus.SC_NOT_FOUND).equals(statusLine.getStatusCode())) {
                return new AnsibleTowerClientException("Ansible: Request URL not found.");
            } else if (new Integer(HttpStatus.SC_BAD_REQUEST).equals(statusLine.getStatusCode())) {
                return new AnsibleTowerClientException("Ansible: Bad request. " + responsePayload);
            }
        } catch (IOException e) {
            return new AnsibleTowerClientException("Ansible: Can't read response");
        }

        return new AnsibleTowerClientException(message);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    static public void main(String[] args) throws AnsibleTowerClientException {

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

        Project firstPrj = null;
        JobTemplate firstJt = null;

        AnsibleTowerClient ac = new AnsibleTowerClient("https://ec2-18-130-248-121.eu-west-2.compute.amazonaws.com", "v2", "admin", "");

        String response = null;
        try {
            response = ac.getAuthToken();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Authentication token: " + response);

        List<Project> prjs = ac.getProjects();
        for (Project prj: prjs) {
            if (firstPrj == null) firstPrj = prj;
            System.out.println("Found Project " + prj.getId() + " - " + prj.getName());
            System.out.println("URL: " + prj.getUrl());
            System.out.println("Description: " + prj.getDescription());

        }
        List<JobTemplate> jts = ac.getJobTemplates(firstPrj.getId());
        for (JobTemplate jt: jts) {
            if (firstJt == null) firstJt = jt;
            System.out.println("Found Job Template " + jt.getId() + " - " + jt.getName());
            System.out.println("URL: " + jt.getUrl());
            System.out.println("Description: " + jt.getDescription());
            System.out.println("Extra Vars: " + jt.getExtraVars());

            Yaml yaml = new Yaml();
            Map<Object, Object> document = yaml.load(jt.getExtraVars());
            for (Map.Entry<Object, Object> entry : document.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }

        }
System.exit(0);
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
            } catch (AnsibleTowerClientException ex) {
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

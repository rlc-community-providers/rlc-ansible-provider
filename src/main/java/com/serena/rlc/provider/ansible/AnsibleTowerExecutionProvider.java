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

package com.serena.rlc.provider.ansible;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serena.rlc.provider.BaseExecutionProvider;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.ansible.domain.Project;
import com.serena.rlc.provider.ansible.exception.AnsibleTowerClientException;
import com.serena.rlc.provider.data.model.INotificationInfo;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.ansible.client.AnsibleTowerClient;
import com.serena.rlc.provider.ansible.domain.Job;
import com.serena.rlc.provider.ansible.domain.JobTemplate;

import com.serena.rlc.provider.exceptions.ProviderValidationException;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Ansible Tower Execution Provider
 * @author kevin.lee@microfocus.com
 */
public class AnsibleTowerExecutionProvider extends BaseExecutionProvider {

    final static Logger logger = LoggerFactory.getLogger(AnsibleTowerExecutionProvider.class);

    final static String LAUNCH_JOB_ACTION = "launchJob";
    final static String LAUNCH_JOB_PARAM = "launchJobParam";
    final static String PROJECT_PARAM = "projectParam";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(
            name = "ansible_url", displayName = "Ansible Tower URL",
            description = "Ansible Tower URL.",
            defaultValue = "https://<servername>",
            dataType = DataType.TEXT
    )
    private String ansibleTowerUrl;

    @ConfigProperty(
            name = "ansible_api_version", displayName = "Ansible Tower API Version",
            description = "Ansible Tower API Version.",
            defaultValue = "v2",
            dataType = DataType.TEXT
    )
    private String ansibleTowerApiVersion;

    @ConfigProperty(
            name = "ansible_serviceuser", displayName = "User Name",
            description = "Ansible Tower Username.",
            defaultValue = "",
            dataType = DataType.TEXT
    )
    private String serviceUser;

    @ConfigProperty(
            name = "ansible_servicepassword", displayName = "Password",
            description = "Ansible Tower Password",
            defaultValue = "",
            dataType = DataType.PASSWORD
    )
    private String servicePassword;

    @ConfigProperty(
            name = "ansible_project_filter", displayName = "Project Filter",
            description = "Ansible Tower Project filter. Used to filter the list of available projects with a wildcard expression",
            defaultValue = "",
            dataType = DataType.TEXT
    )
    private String projectFilter;

    @ConfigProperty(
            name = "ansible_job_template_filter", displayName = "Job Template Filter",
            description = "Ansible Tower Job Template filter. Used to filter the list of available job templates with a wildcard expression",
            defaultValue = "",
            dataType = DataType.TEXT
    )
    private String jobTemplateFilter;

    @ConfigProperty(
            name = "ansible_job_polling_interval",
            displayName = "Job Polling Interval",
            description = "Tells the job polling system at what intervals to check the deployment status, in seconds. The default polling interval is 60 seconds and allowed values are between 10 and 3600 seconds.",
            defaultValue = "60",
            minValue = "10",
            maxValue = "3600",
            dataType = DataType.NUMERIC
    )
    private String jobPollingInterval;

    @ConfigProperty(
            name = "ansible_job_polling_timeout",
            displayName = "Job Polling Timeout",
            description = "Tells the job polling system the amount of time at which it should stop checking for deployment execution status. The default and minimum value is 3600 seconds. Blank value treated as no timeout.",
            defaultValue = "3600",
            minValue = "600",
            dataType = DataType.NUMERIC
    )
    private String jobPollingTimeout;

    public String getAnsibleTowerUrl() {
        return ansibleTowerUrl;
    }

    @Autowired(required = false)
    public void setAnsibleTowerUrl(String ansibleTowerUrl) {
        if (StringUtils.isNotEmpty(ansibleTowerUrl)) {
            this.ansibleTowerUrl = ansibleTowerUrl.replaceAll("^\\s+", "");
        } else {
            this.ansibleTowerUrl = "https://localhost";
        }
    }

    public String getAnsibleTowerApiVersion() {
        return ansibleTowerApiVersion;
    }

    @Autowired(required = false)
    public void setAnsibleTowerApiVersion(String ansibleTowerApiVersion) {
        if (StringUtils.isNotEmpty(ansibleTowerApiVersion)) {
            this.ansibleTowerApiVersion = ansibleTowerApiVersion.replaceAll("^\\s+", "");
        } else {
            this.ansibleTowerApiVersion = "v1";
        }
    }

    public String getServiceUser() {
        return serviceUser;
    }

    @Autowired(required = false)
    public void setServiceUser(String serviceUser) {
        if (!StringUtils.isEmpty(serviceUser)) {
            this.serviceUser = serviceUser.replaceAll("^\\s+", "");
        }
    }

    public String getServicePassword() {
        return servicePassword;
    }

    @Autowired(required = false)
    public void setServicePassword(String servicePassword) {
        if (!StringUtils.isEmpty(servicePassword)) {
            this.servicePassword = servicePassword.replaceAll("^\\s+", "");
        }
    }

    public String getProjectFilter() {
        return projectFilter;
    }

    @Autowired(required = false)
    public void setProjectFilter(String projectFilter) {
        this.projectFilter = projectFilter;
    }

    public String getJobTemplateFilter() {
        return jobTemplateFilter;
    }

    @Autowired(required = false)
    public void setJobTemplateFilter(String jobTemplateFilter) {
        this.jobTemplateFilter = jobTemplateFilter;
    }

    public void setJobPollingInterval(String jobPollingInterval) {
        if (StringUtils.isNotEmpty(jobPollingInterval)) {
            this.jobPollingInterval = jobPollingInterval.replaceAll("^\\s+", "");
            if (!StringUtils.isNumeric(jobPollingInterval)) {
                this.jobPollingInterval = "60";
            } else {
                Integer interval = new Integer(jobPollingInterval);
                if (interval < 10) {
                    interval = 10;
                } else if (interval > 3600) {
                    interval = 3600;
                }

                this.jobPollingInterval = interval.toString();
            }
        } else {
            this.jobPollingInterval = "60";
        }

    }

    public Integer getJobPollingIntervalValue() {
        return new Integer(this.jobPollingInterval);
    }

    public String getJobPollingInterval() {
        return this.jobPollingInterval;
    }

    public void setJobPollingTimeout(String jobPollingTimeout) {
        if (StringUtils.isNotEmpty(jobPollingTimeout)) {
            this.jobPollingTimeout = jobPollingTimeout.replaceAll("^\\s+", "");
            if (!StringUtils.isNumeric(this.jobPollingTimeout)) {
                this.jobPollingTimeout = "3600";
            }
        }

    }

    public Integer getJobPollingTimeoutValue() {
        if (StringUtils.isEmpty(this.jobPollingTimeout)) {
            return null;
        } else {
            Integer timeout = new Integer(this.jobPollingTimeout);
            return timeout < 3600 ? 3600 : timeout;
        }
    }

    public String getJobPollingTimeout() {
        return this.jobPollingTimeout;
    }


    //================================================================================


    @Autowired
    AnsibleTowerClient ansibleTowerClient;

    @Override
    @Service(name = EXECUTE, displayName = "Execute", description = "Execute Ansible Tower action.")
    @Params(params = {
        @Param(fieldName = ACTION, description = "Ansible Tower action to execute", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, description = "Ansible Tower action properties", required = true)})
    public ExecutionInfo execute(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return launchJob(taskTitle, taskDescription, properties);
    }

    @Action(name = LAUNCH_JOB_ACTION, displayName = "Launch Ansible Job", description = "Launch an Ansible Job from a preconfigured Job Template")
    @Params(params = {
            @Param(fieldName = PROJECT_PARAM, displayName = "Project", description = "Ansible Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = LAUNCH_JOB_PARAM, displayName = "Job Template", description = "Ansible Job Template", required = true, dataType = DataType.SELECT),
            @Param(fieldName = EXTENDED_FIELDS, displayName = "Extended Fields", description = "Ansible Job extended fields", required = false, environmentProperty = false, dataType = DataType.SELECT)
    })
    public ExecutionInfo launchJob(String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        Field projectField = Field.getFieldByName(properties, PROJECT_PARAM);
        if (projectField == null || StringUtils.isEmpty(projectField.getValue())) {
            throw new ProviderException("Missing required field: " + PROJECT_PARAM);
        }
        Field launchJobField = Field.getFieldByName(properties, LAUNCH_JOB_PARAM);
        if (launchJobField == null || StringUtils.isEmpty(launchJobField.getValue())) {
            throw new ProviderException("Missing required field: " + LAUNCH_JOB_PARAM);
        }

        logger.debug("Found Job Template: {} - \"{}\" for project: {} - \"{}\"", launchJobField.getValue(),
                launchJobField.getDisplayValue(), projectField.getValue(), projectField.getDisplayValue());
        Long projectId = 0L;
        if (projectField.getValue() != null) {
            projectId = Long.valueOf(projectField.getValue());
        }
        Long launchJob = 0L;
        if (launchJobField.getValue() != null) {
            launchJob = Long.valueOf(launchJobField.getValue());
        }

        // Create Job parameters string
        String paramString = "{";

        Field extendedFieldsField = Field.getFieldByName(properties, EXTENDED_FIELDS);
        if (extendedFieldsField != null) {
            List<Field> extendedFields = extendedFieldsField.getExtendedFields();
            int count = 0;
            for (Field extendedField : extendedFields) {
                if (count > 0) {
                    paramString += ", ";
                }
                paramString += "\"" + extendedField.getFieldName() + "\": \"" + extendedField.getValue() + "\"";
                count++;
            }
        }
        paramString += "}";
        if (paramString != "{}") {
            paramString = "{\"extra_vars\": " + paramString + "}";
        }

        ExecutionInfo retVal = new ExecutionInfo();
        this.setAnsibleTowerClientConnectionDetails();

        Job job = null;
        try {
            job = this.getAnsibleTowerClient().launchJob(launchJob, paramString);

            if (job.getId() != null && !StringUtils.isEmpty(job.getId().toString())) {
                retVal.setExecutionUrl(this.getAnsibleTowerUrl()+"/#/jobs/"+job.getId().toString());
                retVal.setMessage(String.format("Job Number: %d", job.getId()));
                retVal.setExecutionId("ansible-"+job.getId().toString());
                retVal.setStatus(ExecutionStatus.IN_PROGRESS);
                retVal.setPollingInterval(getJobPollingIntervalValue());
            } else {
                retVal.setExecutionUrl(this.getAnsibleTowerUrl()+"/#/jobs/"+job.getId().toString());
                retVal.setMessage("Failed to start Job.");
                retVal.setStatus(ExecutionStatus.FAILED);
            }
        } catch (AnsibleTowerClientException ex) {
            logger.error("Unable to Launch Ansible Job: {}", ex.getMessage());
            retVal.setStatus(ExecutionStatus.FAILED);
            retVal.setExecutionUrl(this.getAnsibleTowerClient().createUrl(this.getAnsibleTowerUrl(), job.getUrl()));
            retVal.setMessage("Failed to Launch Ansible Job from Template");
        }

        return retVal;
    }

    @Service(name = VALIDATE, displayName = "Validate", description = "Validate")
    @Params(params = {
        @Param(fieldName = ACTION, displayName = "Action", description = "Ansible Tower action to validate", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, displayName = "Properties", description = "Ansible Tower action properties", required = true)
    })
    @Override
    public ExecutionInfo validate(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        //TODO: check for valid action
        return new ExecutionInfo("task is valid", true);
    }

    public ExecutionInfo retryExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        //TODO: launchJob(taskTitle, taskDescription, properties, false)
        return launchJob(taskTitle, taskDescription, properties);
    }

    public ExecutionInfo cancelExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        executionInfo.setStatus(ExecutionStatus.CANCELED);
        executionInfo.setMessage("Job cancelled");
        executionInfo.setPollingInterval(-1);
        return executionInfo;
    }

    public ExecutionInfo checkExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) {
        if (!action.equalsIgnoreCase(LAUNCH_JOB_ACTION)) {
            executionInfo.setSuccess(false);
            executionInfo.setMessage(String.format("Check execution for action '%s' is currently not implemented!", action));
            executionInfo.setStatus(ExecutionStatus.FAILED);
            return executionInfo;
        } else {
            String executionId = executionInfo.getExecutionId();
            logger.debug("Found Execution Id: {}", executionId);
            logger.debug(action);
            logger.debug(taskTitle);
            logger.debug(taskDescription);
            Long jobId = new Long(executionId.replaceAll("ansible-", ""));

            try {
                this.setAnsibleTowerClientConnectionDetails();
                String jobStatus = this.getAnsibleTowerClient().getJobStatus(jobId);
                logger.debug("Job execution status is: " + jobStatus);
                if (jobStatus.equals("running")) {
                    executionInfo.setMessage(String.format("Job Number: %d is in progress", jobId));
                    executionInfo.setStatus(ExecutionStatus.IN_PROGRESS);
                    return executionInfo;
                } else if (jobStatus.equals("successful")) {
                    executionInfo.setSuccess(true);
                    executionInfo.setMessage(String.format("Job Number: %d was successful", jobId));
                    executionInfo.setStatus(ExecutionStatus.COMPLETED);
                    return executionInfo;
                } else if (jobStatus.equals("failed")) {
                    executionInfo.setSuccess(false);
                    executionInfo.setMessage(String.format("Job Number: %d failed", jobId));
                    executionInfo.setStatus(ExecutionStatus.FAILED);
                    return executionInfo;
                } else {
                    return executionInfo;
                }
            } catch (AnsibleTowerClientException ex) {
                    executionInfo.setSuccess(false);
                    executionInfo.setMessage(ex.getLocalizedMessage());
                    executionInfo.setStatus(ExecutionStatus.FAILED);
                    return executionInfo;
            }
        }
    }

    @Override
    public INotificationInfo parseNotification(INotificationInfo notificationInfo) {
        throw new IllegalStateException("Method not implemented");
    }

    public void validateConfiguration() throws ProviderValidationException {
        logger.debug("Validate Ansible Tower Execution Configuration");
        List<String> errors = new ArrayList();
        try {
            this.ansibleTowerClient.createConnection(this.getAnsibleTowerUrl(), this.getAnsibleTowerApiVersion(), this.getServiceUser(), this.getServicePassword());
        } catch (Exception ex) {
            errors.add("Can't connect to Ansible Tower server. Reason: " + ex.getMessage());
        }

        if (!errors.isEmpty()) {
            throw new ProviderValidationException((String)errors.stream().collect(Collectors.joining("\n")));
        }
    }

    //================================================================================

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(PROJECT_PARAM)) {
            return getProjectFieldValues(PROJECT_PARAM);
        } else if (fieldName.equalsIgnoreCase(LAUNCH_JOB_PARAM)) {
            return getJobFieldValues(LAUNCH_JOB_PARAM, properties);
        } else if (fieldName.equalsIgnoreCase(EXTENDED_FIELDS)) {
            return getExtendedFieldValues(fieldName, properties);
        } else {
            return null;
        }
    }

    @Getter(name = PROJECT_PARAM, displayName = "Project", description = "Ansible Tower Project")
    public FieldInfo getProjectFieldValues(String fieldName) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setAnsibleTowerClientConnectionDetails();

        List<Project> projects = new ArrayList<>();
        try {
            projects = this.getAnsibleTowerClient().getProjects();
        } catch (AnsibleTowerClientException ex) {
            logger.error("Unable to retrieve Ansible Tower Projects: {}", ex.getMessage());
            throw new ProviderException(ex);
        }

        if (projects == null || projects.size() < 1) {
            return null;
        }

        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;

        for (Project project : projects) {
            if (matchesProjectFilter(project.getName())) {
                value = new FieldValueInfo(project.getId().toString(), project.getName());
                logger.debug("Adding new Project id: {} - name: \"{}\"", project.getId().toString(), project.getName());
                values.add(value);
            }
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    @Getter(name = LAUNCH_JOB_PARAM, displayName = "Job Template", description = "Ansible Tower Job Template")
    @Params(params = {
            @Param(fieldName = PROJECT_PARAM, displayName = "Project", description = "Ansible Project", required = true, dataType = DataType.SELECT)})
    public FieldInfo getJobFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (properties == null || properties.size() < 1) {
            throw new ProviderException("Missing required field properties!");
        }

        Field field = Field.getFieldByName(properties, PROJECT_PARAM);
        if (field == null || StringUtils.isEmpty(field.getValue())) {
            throw new ProviderException("Missing required field: " + PROJECT_PARAM);
        }

        FieldInfo fieldInfo = new FieldInfo(fieldName);

        logger.debug("Getting Job Templates for project: {} - \"{}\"", field.getValue(), field.getDisplayValue());
        setAnsibleTowerClientConnectionDetails();

        List<JobTemplate> jobTemplates = new ArrayList<>();
        try {
            jobTemplates = this.getAnsibleTowerClient().getJobTemplates(Long.valueOf(field.getValue()));
        } catch (AnsibleTowerClientException ex) {
            logger.error("Unable to retrieve Ansible Tower Job Templates: {}", ex.getMessage());
            throw new ProviderException(ex);
        }

        if (jobTemplates == null || jobTemplates.size() < 1) {
            return null;
        }

        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;

        for (JobTemplate jobTemplate : jobTemplates) {
            if (matchesJobFilter(jobTemplate.getName())) {
                value = new FieldValueInfo(jobTemplate.getId().toString(), jobTemplate.getName());
                logger.debug("Adding new Job Template id: {} - name: \"{}\"", jobTemplate.getId().toString(), jobTemplate.getName());
                values.add(value);
            }
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    @Getter(name = EXTENDED_FIELDS, displayName = "Extended Fields", description = "Get Ansible Job field property values.")
    @Params(params = {
        @Param(fieldName = LAUNCH_JOB_PARAM, displayName = "Job Template", description = "Ansible Job Template", required = true, dataType = DataType.SELECT)})
    public FieldInfo getExtendedFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (properties == null || properties.size() < 1) {
            throw new ProviderException("Missing required field properties!");
        }

        Field field = Field.getFieldByName(properties, LAUNCH_JOB_PARAM);
        if (field == null || StringUtils.isEmpty(field.getValue())) {
            throw new ProviderException("Missing required field: " + LAUNCH_JOB_PARAM);
        }

        logger.debug("Getting extra parameters for Job Template id: {} - \"{}\"", field.getValue(), field.getDisplayValue());
        setAnsibleTowerClientConnectionDetails();

        Long launchJob = 0L;
        if (field.getValue() != null) {
            launchJob = Long.valueOf(field.getValue());
        }

        FieldInfo fieldInfo = new FieldInfo(fieldName);

        JobTemplate jobTemplate = null;
        try {
            jobTemplate = this.getAnsibleTowerClient().getJobTemplate(launchJob);
        } catch (AnsibleTowerClientException ex) {
            logger.error("Unable to retrieve Ansible Tower Job Template: {}", ex.getMessage());
            throw new ProviderException(ex);
        }

        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value = new FieldValueInfo(EXTENDED_FIELDS, "Extra Variables");
        List<Field> fields = new ArrayList<>();

        // TODO: could request Limit and Job Tags and start a Job directly
        /*// limit
        Field paramField = new Field("limit", "Limit");
        paramField.setType(DataType.TEXTAREA);
        paramField.setEditable(true);
        paramField.setEnvironmentProperty(true);
        paramField.setValue(job.getLimit());
        fields.add(paramField);

        // job_tags
        paramField = new Field("job_tags", "Job Tags");
        paramField.setType(DataType.TEXTAREA);
        paramField.setEditable(true);
        paramField.setEnvironmentProperty(true);
        paramField.setValue(job.getJobTags());
        fields.add(paramField);*/

        String extraVars = jobTemplate.getExtraVars();
        logger.debug("extra_vars={}", extraVars);
        Field paramField = null;
        // do we have some JSON
        if (this.isJSONValid(extraVars)) {
            // prompt for and iterate over extra_vars, e.g. "extra_vars": "{\"username\": \"admin\", \"environment\": \"test\", \"password\": \"password\"}",
            JSONParser parser = new JSONParser();
            try {
                Object parsedObject = parser.parse(jobTemplate.getExtraVars());
                JSONObject jsonObject = (JSONObject) parsedObject;
                //JSONObject extraVars = (JSONObject) jsonObject.get("extra_vars");
                for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext(); ) {
                    String key = (String) iterator.next();
                    paramField = new Field(key, key);
                    paramField.setType(DataType.TEXT);
                    paramField.setEditable(true);
                    paramField.setEnvironmentProperty(true);
                    paramField.setValue(jsonObject.get(key).toString());
                    fields.add(paramField);
                }
            } catch (ParseException e) {
                logger.error("Error while parsing input JSON - " + jobTemplate.getExtraVars(), e);
            }
        } else {
            // fallback to YAML
            Yaml yaml = new Yaml();
            Map<Object, Object> document = yaml.load(extraVars);
            for (Map.Entry<Object, Object> entry : document.entrySet()) {
                paramField = new Field(entry.getKey().toString(), entry.getKey().toString());
                paramField.setType(DataType.TEXT);
                paramField.setEditable(true);
                paramField.setEnvironmentProperty(true);
                paramField.setValue(entry.getValue().toString());
                fields.add(paramField);
            }
        }

        value.setProperties(fields);
        values.add(value);
        fieldInfo.setValues(values);

        return fieldInfo;
    }

    //================================================================================
    // Additional Public Methods
    //================================================================================

    public AnsibleTowerClient getAnsibleTowerClient() {
        if (ansibleTowerClient == null) {
            setAnsibleTowerClientConnectionDetails();
        }
        return ansibleTowerClient;
    }

    //================================================================================
    // Private Methods
    //================================================================================

    private void setAnsibleTowerClientConnectionDetails() {
        try {
            this.ansibleTowerClient.createConnection(this.getAnsibleTowerUrl(), this.getAnsibleTowerApiVersion(), this.getServiceUser(), this.getServicePassword());
        } catch (AnsibleTowerClientException ex) {
            logger.error("Unable to create connection to Ansible Tower: {}", ex.getMessage());
        }
    }

    private boolean matchesJobFilter(String jobName) {
        String pattern = getJobTemplateFilter();

        if (pattern == null || StringUtils.isEmpty(pattern)) {
            return true;
        } else {
            return jobName.matches(pattern.replace("?", ".?").replace("*", ".*?"));
        }
    }

    private boolean matchesProjectFilter(String projectName) {
        String pattern = getProjectFilter();

        if (pattern == null || StringUtils.isEmpty(pattern)) {
            return true;
        } else {
            return projectName.matches(pattern.replace("?", ".?").replace("*", ".*?"));
        }
    }

    public static boolean isJSONValid(String jsonInString ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


}

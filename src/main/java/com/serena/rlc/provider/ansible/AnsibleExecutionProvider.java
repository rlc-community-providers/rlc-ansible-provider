/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.ansible;

import com.serena.rlc.provider.BaseExecutionProvider;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.ansible.client.AnsibleClient;
import com.serena.rlc.provider.ansible.domain.Job;
import com.serena.rlc.provider.ansible.domain.JobTemplate;
import com.serena.rlc.provider.ansible.exception.AnsibleClientException;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Ansible Tower Execution Provider
 * @author klee@serena.com
 */
public class AnsibleExecutionProvider extends BaseExecutionProvider {

    final static Logger logger = LoggerFactory.getLogger(AnsibleExecutionProvider.class);

    final static String LAUNCH_JOB_ACTION = "launchJob";
    final static String LAUNCH_JOB_PARAM = "launchJobParam";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================
    @ConfigProperty(name = "execution_provider_name", displayName = "Execution Provider Name",
            description = "provider name",
            defaultValue = "Ansible Tower Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "execution_provider_description", displayName = "Execution Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "ansible_url", displayName = "Ansible URL",
            description = "Ansible Server URL.",
            defaultValue = "https://<servername>",
            dataType = DataType.TEXT)
    private String ansibleUrl;

    @ConfigProperty(name = "ansible_api_version", displayName = "Ansible API Version",
            description = "Ansible API Version.",
            defaultValue = "v1",
            dataType = DataType.TEXT)
    private String ansibleApiVersion;

    @ConfigProperty(name = "ansible_serviceuser", displayName = "User Name",
            description = "Ansible service username.",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String serviceUser;

    @ConfigProperty(name = "ansible_servicepassword", displayName = "Password",
            description = "Ansible service password",
            defaultValue = "",
            dataType = DataType.PASSWORD)
    private String servicePassword;

    @ConfigProperty(name = "ansible_job_template_filter", displayName = "Job Template Filter",
            description = "Ansible Job Template filter. Used to filter the list of available job templates with a wildcard expression",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String jobTemplateFilter;

    @ConfigProperty(name = "execution_action_wait_for_callback", displayName = "Wait for Callback",
            description = "Set this to false to set the execution status to Completed when the action is executed and a job number is provided.",
            defaultValue = "false",
            dataType = DataType.TEXT)
    private String waitForCallback;

    public String getJobTemplateFilter() {
        return jobTemplateFilter;
    }

    @Autowired(required = false)
    public void setJobTemplateFilter(String jobTemplateFilter) {
        this.jobTemplateFilter = jobTemplateFilter;
    }

    @Autowired(required = false)
    public void setWaitForCallback(String waitForCallback) {
        if (StringUtils.isNotEmpty(waitForCallback)) {
            this.waitForCallback = waitForCallback;
        }
    }

    public String getWaitForCallback() {
        return waitForCallback;
    }

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    @Autowired(required = false)
    @Override
    public void setProviderName(String providerName) {
        if (StringUtils.isNotEmpty(providerName)) {
            providerName = providerName.trim();
        }

        this.providerName = providerName;
    }

    @Override
    public String getProviderDescription() {
        return this.providerDescription;
    }

    @Autowired(required = false)
    @Override
    public void setProviderDescription(String providerDescription) {
        if (StringUtils.isNotEmpty(providerDescription)) {
            providerDescription = providerDescription.trim();
        }

        this.providerDescription = providerDescription;
    }

    public String getAnsibleUrl() {
        return ansibleUrl;
    }

    @Autowired(required = false)
    public void setAnsibleUrl(String ansibleUrl) {
        if (StringUtils.isNotEmpty(ansibleUrl)) {
            this.ansibleUrl = ansibleUrl.replaceAll("^\\s+", "");
        } else {
            this.ansibleUrl = "https://localhost";
        }
    }

    public String getAnsibleApiVersion() {
        return ansibleApiVersion;
    }

    @Autowired(required = false)
    public void setAnsibleApiVersion(String ansibleApiVersion) {
        if (StringUtils.isNotEmpty(ansibleApiVersion)) {
            this.ansibleApiVersion = ansibleApiVersion.replaceAll("^\\s+", "");
        } else {
            this.ansibleApiVersion = "v1";
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

    @Override
    @Service(name = EXECUTE, displayName = "Execute", description = "Execute Ansible Tower action.")
    @Params(params = {
        @Param(fieldName = ACTION, description = "Ansible Tower action to execute", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, description = "Ansible Tower action properties", required = true)})
    public ExecutionInfo execute(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return launchJob(taskTitle, taskDescription, properties);
    }

    @Service(name = VALIDATE, displayName = "Validate", description = "Validate")
    @Params(params = {
        @Param(fieldName = ACTION, displayName = "Action", description = "Ansible Tower action to validate", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, displayName = "Properties", description = "Ansible Tower action properties", required = true)
    })
    @Override
    public ExecutionInfo validate(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return new ExecutionInfo("task is valid", true);

    }

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(LAUNCH_JOB_PARAM)) {
            return getJobFieldValues(LAUNCH_JOB_PARAM);
        } else if (fieldName.equalsIgnoreCase(EXTENDED_FIELDS)) {
            return getExtendedFieldValues(fieldName, properties);
        } else {
            return null;
        }
    }

    @Action(name = LAUNCH_JOB_ACTION, displayName = "Launch Ansible Job", description = "Launch and Ansible Job from a preconfigured Job Template")
    @Params(params = {
        @Param(fieldName = LAUNCH_JOB_PARAM, displayName = "Job Template", description = "Ansible Job Template", required = true, dataType = DataType.SELECT),
        @Param(fieldName = EXTENDED_FIELDS, displayName = "Extended Fields", description = "Ansible Job extended fields", required = false, environmentProperty = false, dataType = DataType.SELECT)
    })
    public ExecutionInfo launchJob(String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        Field launchJobField = Field.getFieldByName(properties, LAUNCH_JOB_PARAM);
        if (launchJobField == null || StringUtils.isEmpty(launchJobField.getValue())) {
            throw new ProviderException("Missing required field: " + LAUNCH_JOB_PARAM);
        }

        logger.debug("Found Job Template: {} - \"{}\"", launchJobField.getValue(), launchJobField.getDisplayValue());
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
        AnsibleClient client = null;
        try {
            client = new AnsibleClient(this.getAnsibleUrl(), this.getAnsibleApiVersion(), this.getServiceUser(), this.getServicePassword());
        } catch (AnsibleClientException ex) {
            logger.error("Unable to create connection to Ansible Tower: {}", ex.getMessage());
            throw new ProviderException(ex);
        }

        boolean statusSet = false;
        Job job = null;
        try {
            job = client.launchJob(launchJob, paramString);

            if (job.getId() != null && !StringUtils.isEmpty(job.getId().toString())) {
                retVal.setExecutionUrl(this.getAnsibleUrl()+"/#/jobs/"+job.getId().toString());
                retVal.setMessage(String.format("Job Number: %d", job.getId()));
                retVal.setExecutionId("ansible-"+job.getId().toString());
            } else {
                retVal.setExecutionUrl(this.getAnsibleUrl()+"/#/jobs/"+job.getId().toString());
                retVal.setMessage("Failed to start Job.");
                retVal.setStatus(ExecutionStatus.FAILED);
                statusSet = true;
            }

            if (!statusSet) {
                if (Boolean.parseBoolean(getWaitForCallback())) {
                    retVal.setStatus(ExecutionStatus.PENDING);
                } else {
                    retVal.setStatus(ExecutionStatus.COMPLETED);
                }
            }

        } catch (AnsibleClientException ex) {
            logger.error("Unable to Launch Ansible Job: {}", ex.getMessage());
            retVal.setStatus(ExecutionStatus.FAILED);
            retVal.setExecutionUrl(client.createUrl(this.getAnsibleUrl(), job.getUrl()));
            retVal.setMessage("Failed to Launch Ansible Job from Template");
        }

        return retVal;
    }

    private boolean matchesJobFilter(String jobName) {
        String pattern = getJobTemplateFilter();

        if (pattern == null || StringUtils.isEmpty(pattern)) {
            return true;
        } else {
            return jobName.matches(pattern.replace("?", ".?").replace("*", ".*?"));
        }
    }

    @Getter(name = LAUNCH_JOB_PARAM, displayName = "Job Template", description = "Ansible Tower Job Template")
    public FieldInfo getJobFieldValues(String fieldName) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        AnsibleClient client = null;
        try {
            client = new AnsibleClient(this.getAnsibleUrl(), this.getAnsibleApiVersion(), this.getServiceUser(), this.getServicePassword());
        } catch (AnsibleClientException ex) {
            logger.error("Unable to create connection to Ansible Tower: {}", ex.getMessage());
            throw new ProviderException(ex);
        }
        List<JobTemplate> jobTemplates = new ArrayList<>();

        try {
            jobTemplates = client.getJobTemplates();
        } catch (AnsibleClientException ex) {
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
        Long launchJob = 0L;
        if (field.getValue() != null) {
            launchJob = Long.valueOf(field.getValue());
        }

        FieldInfo fieldInfo = new FieldInfo(fieldName);

        Job job = null;
        try {
            AnsibleClient client = new AnsibleClient(this.getAnsibleUrl(), this.getAnsibleApiVersion(), this.getServiceUser(), this.getServicePassword());
            job = client.getJob(launchJob);

        } catch (AnsibleClientException ex) {
            logger.error("Unable to retrieve Ansible Tower Job: {}", ex.getMessage());
            throw new ProviderException(ex);
        }

        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value = new FieldValueInfo(EXTENDED_FIELDS, "Job Parameters");
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

        // prompt for and iterate over extra_vars, e.g. "extra_vars": "{\"username\": \"admin\", \"environment\": \"test\", \"password\": \"password\"}",
        JSONParser parser = new JSONParser();
        Field paramField = null;
        try {
            Object parsedObject = parser.parse(job.getExtraVars());
            JSONObject jsonObject = (JSONObject) parsedObject;
            //JSONObject extraVars = (JSONObject) jsonObject.get("extra_vars");
            logger.debug("extra_vars={}",jsonObject.toString());

            for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                paramField = new Field(key, key);
                paramField.setType(DataType.TEXTAREA);
                paramField.setEditable(true);
                paramField.setEnvironmentProperty(true);
                paramField.setValue(jsonObject.get(key).toString());
                fields.add(paramField);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + job.getExtraVars(), e);
        }

        value.setProperties(fields);
        values.add(value);
        fieldInfo.setValues(values);

        return fieldInfo;
    }

}

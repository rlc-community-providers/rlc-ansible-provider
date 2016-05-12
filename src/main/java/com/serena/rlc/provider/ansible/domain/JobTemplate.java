/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.ansible.domain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Ansible Tower Job Template POJO
 * @author klee@serena.com
 */
public class JobTemplate {
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(JobTemplate.class);

    private Long id;
    private String name;
    private String url;
    private String description;
    private String playbook;
    private Long project;
    private Long inventory;
    private String limit;
    private String extraVars;
    private String jobTags;
    private String status;
    private String hostConfigKey;
    private List<JobTemplate> jobTemplates;
    
    public JobTemplate(){}
    public JobTemplate(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaybook() {
        return playbook;
    }

    public void setPlaybook(String playbook) {
        this.playbook = playbook;
    }

    public Long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }

    public Long getInventory() {
        return inventory;
    }

    public void setInventory(Long inventory) {
        this.inventory = inventory;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getExtraVars() {
        return extraVars;
    }

    public void setExtraVars(String extraVars) {
        this.extraVars = extraVars;
    }

    public String getJobTags() {
        return jobTags;
    }

    public void setJobTags(String jobTags) {
        this.jobTags = jobTags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostConfigKey() {
        return hostConfigKey;
    }

    public void setHostConfigKey(String hostConfigKey) {
        this.hostConfigKey = hostConfigKey;
    }

    public List<JobTemplate> getJobTemplates() {
        return jobTemplates;
    }

    public void setJobTemplates(List<JobTemplate> jobTemplates) {
        this.jobTemplates = jobTemplates;
    }
    
    public static JobTemplate parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) parsedObject;
            JobTemplate jobTemplate = parseSingle(jsonObject);
            return jobTemplate;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static List<JobTemplate> parse(String options) {
        List<JobTemplate> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("results");
            for (Object object : array) {
                JobTemplate obj = parseSingle((JSONObject) object);
                list.add(obj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static JobTemplate parseSingle(JSONObject jsonObject) {
        JobTemplate obj = new JobTemplate();
        if (jsonObject != null) {
            obj.setId((Long) jsonObject.get("id"));
            obj.setName((String) jsonObject.get("name"));
            obj.setUrl((String) jsonObject.get("url"));
            obj.setDescription((String) jsonObject.get("description"));
            obj.setPlaybook((String) jsonObject.get("playbook"));
            obj.setProject((Long) jsonObject.get("project"));
            obj.setInventory((Long) jsonObject.get("inventory"));
            obj.setLimit((String) jsonObject.get("limit"));
            obj.setExtraVars((String) jsonObject.get("extra_vars"));
            obj.setJobTags((String) jsonObject.get("job_tags"));
            obj.setStatus((String) jsonObject.get("status"));
            obj.setHostConfigKey((String) jsonObject.get("host_config_key"));
        }
        return obj;
    }

    @Override
    public String toString() {
        return "JobTemplate{" + "id=" + id + ", name=" + name + '}';
    }
    
    
}

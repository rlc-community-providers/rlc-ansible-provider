/*
 *
 * Copyright (c) 2015 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.ansible.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author klee
 */

public class AnsibleClientException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AnsibleClientException.class);

    public AnsibleClientException() {
    }

    public AnsibleClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnsibleClientException(String message) {
        super(message);
    }

    public AnsibleClientException(Throwable cause) {
        super(cause);
    }
}

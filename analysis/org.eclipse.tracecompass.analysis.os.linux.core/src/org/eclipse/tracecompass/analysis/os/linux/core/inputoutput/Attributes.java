/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

/**
 * This file defines all the attribute names used in the handler. Both the
 * construction and query steps should use them.
 *
 * These should not be externalized! The values here are used as-is in the
 * history file on disk, so they should be kept the same to keep the file format
 * compatible. If a view shows attribute names directly, the localization should
 * be done on the viewer side.
 *
 * @author Houssem Daoud
 *
 */
@SuppressWarnings({"nls", "javadoc"})
public interface Attributes {

    /* First-level attributes */
    static final String THREADS = "Threads";
    static final String SYSTEM_CALL = "sys_call";
    static final String SYSTEM_CALLS_ROOT = "system_calls";
    static final String SECTORS_READ="sectors_read";
    static final String SECTORS_WRITTEN="sectors_written";
    static final String BYTES_READ="bytes_read";
    static final String BYTES_WRITTEN="bytes_written";
    static final String STATUS = "Status";
    static final String DISKS="Disks";
    static final String WAITING_QUEUE = "Waiting_queue";
    static final String DRIVER_QUEUE = "Driver_queue";
    static final String DRIVERQUEUE_LENGTH = "driverqueue_length";
    static final String WAITINGQUEUE_LENGTH = "waitingqueue_length";
    static final String CURRENT_REQUEST = "Current_request";
    static final String REQUEST_SIZE = "Request_size";

}

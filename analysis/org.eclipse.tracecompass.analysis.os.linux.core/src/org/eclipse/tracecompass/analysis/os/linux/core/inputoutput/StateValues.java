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

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * @author Houssem Daoud
 *
 */
@SuppressWarnings("javadoc")
public interface StateValues {

    /* CPU Status */
    static final int NULL = 0;
    static final int FILE_OPENED = 1;
    static final int READING = 2;
    static final int WRITING = 3;
    static final int OPENING = 4;
    static final int CHECKING_FILE_STATUS = 5;
    static final int IOCTL = 6;
    static final int MMAP = 7;
    static final int LLSEEK = 8;
    static final int READING_REQUEST = 9;
    static final int WRITING_REQUEST = 10;
    static final int CACHE_HIT = 11;
    static final int MERGED = 12;



    static final ITmfStateValue NULL_VALUE = TmfStateValue.newValueInt(NULL);
    static final ITmfStateValue FILE_OPENED_VALUE = TmfStateValue.newValueInt(FILE_OPENED);
    static final ITmfStateValue READING_VALUE = TmfStateValue.newValueInt(READING);
    static final ITmfStateValue WRITING_VALUE = TmfStateValue.newValueInt(WRITING);
    static final ITmfStateValue CHECKING_FILE_STATUS_VALUE = TmfStateValue.newValueInt(CHECKING_FILE_STATUS);
    static final ITmfStateValue IOCTL_VALUE = TmfStateValue.newValueInt(IOCTL);
    static final ITmfStateValue MMAP_VALUE = TmfStateValue.newValueInt(MMAP);
    static final ITmfStateValue LLSEEK_VALUE = TmfStateValue.newValueInt(LLSEEK);
    static final ITmfStateValue READING_REQUEST_VALUE = TmfStateValue.newValueInt(READING_REQUEST);
    static final ITmfStateValue WRITING_REQUEST_VALUE = TmfStateValue.newValueInt(WRITING_REQUEST);
    static final ITmfStateValue OPENING_VALUE = TmfStateValue.newValueInt(OPENING);
    static final ITmfStateValue CACHE_HIT_VALUE = TmfStateValue.newValueInt(CACHE_HIT);
    static final ITmfStateValue MERGED_VALUE = TmfStateValue.newValueInt(MERGED);
}

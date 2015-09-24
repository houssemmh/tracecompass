/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.blockioqueue;

import org.eclipse.osgi.util.NLS;

/**
 * Translatable strings for the Disk I/O view
 *
 * @author Houssem Daoud
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.ui.views.blockioqueue.messages"; //$NON-NLS-1$
    /** Title of the Disk I/O view */
    public static String BlockIOQueueView_Title;
    /** Title of the Disk I/O activity viewer */
    public static String BlockIOQueueViewer_Title;
    /** X axis caption */
    public static String BlockIOQueueViewer_XAxis;
    /** Y axis caption */
    public static String BlockIOQueueViewer_YAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}

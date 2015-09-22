/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.blockioqueue;

import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Houssem Daoud
 */
public class BlockIOQueueView extends TmfView {

    /** ID string */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.blockioqueue"; //$NON-NLS-1$
    private BlockIOQueueViewer graph = null;
    /**
     * Constructor
     */
    public BlockIOQueueView() {
        super(Messages.BlockIOQueueView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {
        final SashForm sash = new SashForm(parent, SWT.NONE);
        graph = new BlockIOQueueViewer(sash);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            graph.traceSelected(signal);
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        if (graph != null) {
            graph.dispose();
        }
    }
}

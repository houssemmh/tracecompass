/**********************************************************************
 * Copyright (c) 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Create and use base class for XY plots
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.driverioqueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Attributes;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Houssem Daoud
 */
@SuppressWarnings("restriction")
public class DriverIOQueueViewer extends TmfCommonXLineChartViewer {

    private TmfStateSystemAnalysisModule fModule = null;
    private final String diskname = new String("sda"); //$NON-NLS-1$
    // Timeout between updates in the updateData thread
    private static final long BUILD_UPDATE_TIMEOUT = 500;
    private static final double RESOLUTION = 5;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     */
    public DriverIOQueueViewer(Composite parent) {
        super(parent, Messages.DriverIOQueueViewer_Title, Messages.DriverIOQueueViewer_XAxis, Messages.DriverIOQueueViewer_YAxis);
        setResolution(RESOLUTION);
    }

    @SuppressWarnings("null")
    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfStateSystemAnalysisModule.class, InputOutputAnalysisModule.ID);
            if (fModule == null) {
                return;
            }
            fModule.schedule();
        }
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        try {
            if (getTrace() == null || fModule == null) {
                return;
            }
            fModule.waitForInitialization();
            ITmfStateSystem ss = fModule.getStateSystem();
            /*
             * Don't wait for the module completion, when it's ready, we'll know
             */
            if (ss == null) {
                return;
            }

            double[] xvalues = getXAxis(start, end, nb);
            setXAxis(xvalues);

            boolean complete = false;
            long currentEnd = start;

            while (!complete && currentEnd < end) {
                if (monitor.isCanceled()) {
                    return;
                }
                complete = ss.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
                currentEnd = ss.getCurrentEndTime();
                int disksQuark = ss.getQuarkAbsolute(Attributes.DISKS);
                int diskQuark = ss.getQuarkRelative(disksQuark, diskname);
                int driverIOQueueQuark = ss.getQuarkRelative(diskQuark, Attributes.DRIVERQUEUE_LENGTH);
                long traceStart = getStartTime();
                long traceEnd = getEndTime();
                long offset = this.getTimeOffset();

                /* Initialize quarks and series names */
                double[] fYValues = new double[xvalues.length];
                String serieName = new String(diskname).trim();

                /*
                 * TODO: It should only show active threads in the time range.
                 * If a tid does not have any memory value (only 1 interval in
                 * the time range with value null or 0), then its series should
                 * not be displayed.
                 */
                double yvalue = 0.0;
                for (int i = 0; i < xvalues.length; i++) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    double x = xvalues[i];
                    long time = (long) x + offset;
                    // make sure that time is in the trace range after double to
                    // long conversion
                    time = time < traceStart ? traceStart : time;
                    time = time > traceEnd ? traceEnd : time;

                    try {
                        yvalue = ss.querySingleState(time, driverIOQueueQuark).getStateValue().unboxLong();
                        fYValues[i] = yvalue;
                    } catch (TimeRangeException e) {
                        fYValues[i] = 0;
                    }

                }
                setSeries(serieName, fYValues);
                updateDisplay();
            }
        } catch (AttributeNotFoundException | StateValueTypeException | StateSystemDisposedException e) {
            Activator.logError("Error updating the data of the Memory usage view", e); //$NON-NLS-1$
        }
    }

}

/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph views
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.diskrequests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.ui.views.diskrequests.DiskRequestsEntry.Type;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class DiskRequestsView extends AbstractTimeGraphView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.diskrequests"; //$NON-NLS-1$

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.ResourcesView_stateTypeName
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public DiskRequestsView() {
        super(ID, new DiskRequestsPresentationProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected String getNextText() {
        return Messages.ResourcesView_nextResourceActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ResourcesView_nextResourceActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ResourcesView_previousResourceActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ResourcesView_previousResourceActionToolTipText;
    }

    @Override
    protected void buildEventList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, InputOutputAnalysisModule.ID);
        if (ssq == null) {
            return;
        }
        Comparator<ITimeGraphEntry> comparator = new Comparator<ITimeGraphEntry>() {
            @Override
            public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
                return ((DiskRequestsEntry) o1).compareTo(o2);
            }
        };

        Map<Integer, DiskRequestsEntry> entryMap = new HashMap<>();
        TimeGraphEntry traceEntry = null;
        TimeGraphEntry driverEntry = null;
        TimeGraphEntry blockEntry = null;

        long startTime = ssq.getStartTime();
        long start = startTime;
        setStartTime(Math.min(getStartTime(), startTime));
        boolean complete = false;
        while (!complete) {
            if (monitor.isCanceled()) {
                return;
            }
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            if (ssq.isCancelled()) {
                return;
            }
            long end = ssq.getCurrentEndTime();
            if (start == end && !complete) { // when complete execute one last time regardless of end time
                continue;
            }
            long endTime = end + 1;
            setEndTime(Math.max(getEndTime(), endTime));

            if (traceEntry == null) {
                traceEntry = new DiskRequestsEntry(trace, trace.getName(), startTime, endTime, 0);
                traceEntry.sortChildren(comparator);
                List<TimeGraphEntry> entryList = Collections.singletonList(traceEntry);
                addToEntryList(parentTrace, entryList);
            } else {
                traceEntry.updateEndTime(endTime);
            }

            if (driverEntry == null) {
                driverEntry = new DiskRequestsEntry(trace, "Driver Queue", startTime, endTime, 0);
                driverEntry.sortChildren(comparator);
                traceEntry.addChild(driverEntry);
            } else {
                driverEntry.updateEndTime(endTime);
            }

            if (blockEntry == null) {
                blockEntry = new DiskRequestsEntry(trace, "Block Layer Queue", startTime, endTime, 1);
                blockEntry.sortChildren(comparator);
                traceEntry.addChild(blockEntry);
            } else {
                blockEntry.updateEndTime(endTime);
            }

            List<Integer> driverSlotsQuarks = ssq.getQuarks("Disks","sda",Attributes.DRIVER_QUEUE, "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (Integer driverSlotQuark : driverSlotsQuarks) {
                int driverSlot = Integer.parseInt(ssq.getAttributeName(driverSlotQuark));
                DiskRequestsEntry entry = entryMap.get(driverSlotQuark);
                if (entry == null) {
                    entry = new DiskRequestsEntry(driverSlotQuark, trace, startTime, endTime, Type.DRIVER, driverSlot);
                    entryMap.put(driverSlotQuark, entry);
                    driverEntry.addChild(entry);
                } else {
                    entry.updateEndTime(endTime);
                }
            }
            List<Integer> blockSlotsQuarks = ssq.getQuarks("Disks","sda",Attributes.WAITING_QUEUE, "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (Integer blockSlotQuark : blockSlotsQuarks) {
                int blockSlot = Integer.parseInt(ssq.getAttributeName(blockSlotQuark));
                DiskRequestsEntry entry = entryMap.get(blockSlotQuark);
                if (entry == null) {
                    entry = new DiskRequestsEntry(blockSlotQuark, trace, startTime, endTime, Type.BLOCK, blockSlot);
                    entryMap.put(blockSlotQuark, entry);
                    blockEntry.addChild(entry);
                } else {
                    entry.updateEndTime(endTime);
                }
            }

            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            long resolution = Math.max(1, (endTime - ssq.getStartTime()) / getDisplayWidth());
            for (ITimeGraphEntry child : traceEntry.getChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                if (child instanceof TimeGraphEntry) {
                    for (ITimeGraphEntry queueSlot : child.getChildren()) {
                        if (queueSlot instanceof TimeGraphEntry) {
                            TimeGraphEntry entry = (TimeGraphEntry) queueSlot;
                            List<ITimeEvent> eventList = getEventList(entry, start, endTime, resolution, monitor);
                            if (eventList != null) {
                                for (ITimeEvent event : eventList) {
                                    entry.addEvent(event);
                                }
                            }
                            redraw();
                        }
                    }
                }
            }

            start = end;
        }
    }

    @Override
    protected @Nullable List<ITimeEvent> getEventList(TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        DiskRequestsEntry queueSlotEntry = (DiskRequestsEntry) entry;
        ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(queueSlotEntry.getTrace(), InputOutputAnalysisModule.ID);
        if (ssq == null) {
            return null;
        }
        final long realStart = Math.max(startTime, ssq.getStartTime());
        final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = queueSlotEntry.getQuark();

        try {
            if (queueSlotEntry.getType().equals(Type.DRIVER) || queueSlotEntry.getType().equals(Type.BLOCK)) {
                int statusQuark;
                try {
                    statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                } catch (AttributeNotFoundException e) {
                    /*
                     * The sub-attribute "status" is not available. May happen
                     * if the trace does not have sched_switch events enabled.
                     */
                    return null;
                }
                List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ssq, statusQuark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(statusIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval statusInterval : statusIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = statusInterval.getStateValue().unboxInt();
                    long time = statusInterval.getStartTime();
                    long duration = statusInterval.getEndTime() - time + 1;
                    if (!statusInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new TimeEvent(entry, time, duration, status));
                    } else if (lastEndTime == -1 || time + duration >= endTime) {
                        // add null event if it intersects the start or end time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    }
                    lastEndTime = time + duration;
                }
            }
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

}

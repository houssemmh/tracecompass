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
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.diskrequests;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.StateValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITmfTimeGraphDrawingHelper;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

/**
 * Presentation provider for the Resource view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class DiskRequestsPresentationProvider extends TimeGraphPresentationProvider {

    private long lastRequestSector = -1;
    private Color fColorWhite;
    private Color fColorGray;
    private Integer fAverageCharWidth;

    private enum State {
        READ           (new RGB( 200, 0  ,   0)),
        WRITE          (new RGB( 0  , 0  , 200));

        public final RGB rgb;

        private State(RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Default constructor
     */
    public DiskRequestsPresentationProvider() {
        super();
    }

    private static State[] getStateValues() {
        return State.values();
    }

    private static State getEventState(TimeEvent event) {
        if (event.hasValue()) {
            int value = event.getValue();
            if (value == StateValues.READING_REQUEST) {
                return State.READ;
            } else if (value == StateValues.WRITING_REQUEST) {
                return State.WRITE;
            }
        }
        return null;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.ordinal();
        }
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public StateItem[] getStateTable() {
        State[] states = getStateValues();
        StateItem[] stateTable = new StateItem[states.length];
        for (int i = 0; i < stateTable.length; i++) {
            State state = states[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        return stateTable;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.toString();
        }
        if (event instanceof NullTimeEvent) {
            return null;
        }
        return Messages.ResourcesView_multipleStates;
    }

//    @Override
//    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
//
//        Map<String, String> retMap = new LinkedHashMap<>();
//        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
//
//            TimeEvent tcEvent = (TimeEvent) event;
//            DiskRequestsEntry entry = (DiskRequestsEntry) event.getEntry();
//
//            if (tcEvent.hasValue()) {
//                ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(entry.getTrace(), KernelAnalysisModule.ID);
//                if (ss == null) {
//                    return retMap;
//                }
//                // Check for IRQ or Soft_IRQ type
//                if (entry.getType().equals(Type.IRQ) || entry.getType().equals(Type.SOFT_IRQ)) {
//
//                    // Get CPU of IRQ or SoftIRQ and provide it for the tooltip display
//                    int cpu = tcEvent.getValue();
//                    if (cpu >= 0) {
//                        retMap.put(Messages.ResourcesView_attributeCpuName, String.valueOf(cpu));
//                    }
//                }
//
//                // Check for type CPU
//                else if (entry.getType().equals(Type.CPU)) {
//                    int status = tcEvent.getValue();
//
//                    if (status == StateValues.CPU_STATUS_IRQ) {
//                        // In IRQ state get the IRQ that caused the interruption
//                        int cpu = entry.getId();
//
//                        try {
//                            List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
//                            List<Integer> irqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
//
//                            for (int irqQuark : irqQuarks) {
//                                if (fullState.get(irqQuark).getStateValue().unboxInt() == cpu) {
//                                    ITmfStateInterval value = ss.querySingleState(event.getTime(), irqQuark);
//                                    if (!value.getStateValue().isNull()) {
//                                        int irq = Integer.parseInt(ss.getAttributeName(irqQuark));
//                                        retMap.put(Messages.ResourcesView_attributeIrqName, String.valueOf(irq));
//                                    }
//                                    break;
//                                }
//                            }
//                        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
//                            Activator.getDefault().logError("Error in ResourcesPresentationProvider", e); //$NON-NLS-1$
//                        } catch (StateSystemDisposedException e) {
//                            /* Ignored */
//                        }
//                    } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
//                        // In SOFT_IRQ state get the SOFT_IRQ that caused the interruption
//                        int cpu = entry.getId();
//
//                        try {
//                            List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
//                            List<Integer> softIrqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
//
//                            for (int softIrqQuark : softIrqQuarks) {
//                                if (fullState.get(softIrqQuark).getStateValue().unboxInt() == cpu) {
//                                    ITmfStateInterval value = ss.querySingleState(event.getTime(), softIrqQuark);
//                                    if (!value.getStateValue().isNull()) {
//                                        int softIrq = Integer.parseInt(ss.getAttributeName(softIrqQuark));
//                                        retMap.put(Messages.ResourcesView_attributeSoftIrqName, String.valueOf(softIrq));
//                                    }
//                                    break;
//                                }
//                            }
//                        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
//                            Activator.getDefault().logError("Error in ResourcesPresentationProvider", e); //$NON-NLS-1$
//                        } catch (StateSystemDisposedException e) {
//                            /* Ignored */
//                        }
//                    } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL) {
//                        // In running state get the current tid
//
//                        try {
//                            retMap.put(Messages.ResourcesView_attributeHoverTime, Utils.formatTime(hoverTime, TimeFormat.CALENDAR, Resolution.NANOSEC));
//                            int cpuQuark = entry.getQuark();
//                            int currentThreadQuark = ss.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
//                            ITmfStateInterval interval = ss.querySingleState(hoverTime, currentThreadQuark);
//                            if (!interval.getStateValue().isNull()) {
//                                ITmfStateValue value = interval.getStateValue();
//                                int currentThreadId = value.unboxInt();
//                                retMap.put(Messages.ResourcesView_attributeTidName, Integer.toString(currentThreadId));
//                                int execNameQuark = ss.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.EXEC_NAME);
//                                interval = ss.querySingleState(hoverTime, execNameQuark);
//                                if (!interval.getStateValue().isNull()) {
//                                    value = interval.getStateValue();
//                                    retMap.put(Messages.ResourcesView_attributeProcessName, value.unboxStr());
//                                }
//                                if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
//                                    int syscallQuark = ss.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.SYSTEM_CALL);
//                                    interval = ss.querySingleState(hoverTime, syscallQuark);
//                                    if (!interval.getStateValue().isNull()) {
//                                        value = interval.getStateValue();
//                                        retMap.put(Messages.ResourcesView_attributeSyscallName, value.unboxStr());
//                                    }
//                                }
//                            }
//                        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
//                            Activator.getDefault().logError("Error in ResourcesPresentationProvider", e); //$NON-NLS-1$
//                        } catch (StateSystemDisposedException e) {
//                            /* Ignored */
//                        }
//                    }
//                }
//            }
//        }
//
//        return retMap;
//    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (fColorGray == null) {
            fColorGray = gc.getDevice().getSystemColor(SWT.COLOR_GRAY);
        }
        if (fColorWhite == null) {
            fColorWhite = gc.getDevice().getSystemColor(SWT.COLOR_WHITE);
        }
        if (fAverageCharWidth == null) {
            fAverageCharWidth = gc.getFontMetrics().getAverageCharWidth();
        }

        ITmfTimeGraphDrawingHelper drawingHelper = getDrawingHelper();
        if (bounds.width <= fAverageCharWidth) {
            return;
        }

        if (!(event instanceof TimeEvent)) {
            return;
        }
        TimeEvent tcEvent = (TimeEvent) event;
        if (!tcEvent.hasValue()) {
            return;
        }

        DiskRequestsEntry entry = (DiskRequestsEntry) event.getEntry();
//        if (!entry.getType().equals(Type.CPU)) {
//            return;
//        }

        int status = tcEvent.getValue();
        if (status != StateValues.READING_REQUEST && status != StateValues.WRITING_REQUEST) {
            return;
        }

        ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(entry.getTrace(), InputOutputAnalysisModule.ID);
        if (ss == null) {
            return;
        }
        long time = event.getTime();
        try {
            while (time < event.getTime() + event.getDuration()) {
                int queueEntry = entry.getQuark();
                int currentRequestQuark = ss.getQuarkRelative(queueEntry, Attributes.CURRENT_REQUEST);
                ITmfStateInterval requestInterval = ss.querySingleState(time, currentRequestQuark);
                long startTime = Math.max(requestInterval.getStartTime(), event.getTime());
                int x = Math.max(drawingHelper.getXForTime(startTime), bounds.x);
                if (x >= bounds.x + bounds.width) {
                    break;
                }
                if (!requestInterval.getStateValue().isNull()) {
                    ITmfStateValue value = requestInterval.getStateValue();
                    long currentRequestSector = value.unboxLong();
                    long endTime = Math.min(requestInterval.getEndTime() + 1, event.getTime() + event.getDuration());
                    int xForEndTime = drawingHelper.getXForTime(endTime);
                    if (xForEndTime > bounds.x) {
                        int width = Math.min(xForEndTime, bounds.x + bounds.width) - x - 1;
                        if (width > 50) {
                            if (currentRequestSector != lastRequestSector) {
                                gc.setForeground(fColorWhite);
                                int drawn = Utils.drawText(gc, String.valueOf(currentRequestSector), x + 1, bounds.y - 2, width, true, true);
                                if (drawn > 0) {
                                    lastRequestSector = currentRequestSector;
                                }
                            }
                            if (xForEndTime < bounds.x + bounds.width) {
                                gc.setForeground(fColorGray);
                                gc.drawLine(xForEndTime, bounds.y + 1, xForEndTime, bounds.y + bounds.height - 2);
                            }
                        }
                    }
                }
                // make sure next time is at least at the next pixel
                time = Math.max(requestInterval.getEndTime() + 1, drawingHelper.getTimeAtX(x + 1));
            }
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("Error in ResourcesPresentationProvider", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        lastRequestSector = -1;
    }
}

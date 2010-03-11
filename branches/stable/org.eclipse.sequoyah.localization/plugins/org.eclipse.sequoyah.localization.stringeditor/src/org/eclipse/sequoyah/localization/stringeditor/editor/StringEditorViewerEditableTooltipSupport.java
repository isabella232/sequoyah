/********************************************************************************
 * Copyright (c) 2009 Motorola Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Marcelo Marzola Bossoni (Eldorado)
 * 
 * Contributors:
 * name (company) - description.
 ********************************************************************************/
package org.eclipse.sequoyah.localization.stringeditor.editor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.sequoyah.device.common.utilities.BasePlugin;
import org.eclipse.sequoyah.device.common.utilities.exception.SequoyahException;
import org.eclipse.sequoyah.localization.stringeditor.datatype.RowInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class StringEditorViewerEditableTooltipSupport extends ColumnViewerToolTipSupport
{

    private String tooltipText;

    private final ColumnViewer viewer;

    private Event currentTooltipEvent;

    private final StringEditorPart editor;

    protected StringEditorViewerEditableTooltipSupport(ColumnViewer viewer, int style,
            boolean manualActivation, StringEditorPart editor)
    {
        super(viewer, style, manualActivation);
        setHideDelay(0);
        setHideOnMouseDown(false);
        setPopupDelay(100);
        setShift(new Point(-3, 0));
        this.tooltipText = null;
        this.viewer = viewer;
        this.editor = editor;
    }

    public static void enableFor(ColumnViewer viewer, int style, StringEditorPart editor)
    {
        new StringEditorViewerEditableTooltipSupport(viewer, style, false, editor);
    }

    @Override
    protected Composite createViewerToolTipContentArea(Event event, ViewerCell cell,
            Composite parent)
    {

        Composite toReturn = null;

        if (cell.getColumnIndex() != 0)
        {
            final String text = getText(event);
            toReturn = new Composite(parent, SWT.FILL);
            GridLayout layout = new GridLayout();
            toReturn.setLayout(layout);
            final Text textComposite = new Text(toReturn, SWT.MULTI | SWT.WRAP);
            GridData layouData = new GridData(GridData.FILL_BOTH);
            layouData.minimumWidth = 200;
            layouData.minimumHeight = 50;
            layouData.grabExcessHorizontalSpace = true;
            layouData.grabExcessVerticalSpace = true;
            textComposite.setText(text.trim().length() > 0 ? text : Messages.StringEditorViewerEditableTooltipSupport_0);
            textComposite.setLayoutData(layouData);
            textComposite.addModifyListener(new ModifyListener()
            {

                public void modifyText(ModifyEvent e)
                {
                    tooltipText = ((Text) e.widget).getText();
                }
            });
            textComposite.addFocusListener(new FocusListener()
            {

                public void focusLost(FocusEvent e)
                {
                    // do nothing
                }

                public void focusGained(FocusEvent e)
                {
                    if (text.trim().length() == 0)
                    {
                        textComposite.setText(""); //$NON-NLS-1$
                        tooltipText = null;
                    }
                }
            });
        }
        else
        {
            toReturn = super.createViewerToolTipContentArea(event, cell, parent);
        }
        currentTooltipEvent = event;
        return toReturn;
    }

    @Override
    protected void afterHideToolTip(Event event)
    {

        ViewerCell cell = viewer.getCell(new Point(currentTooltipEvent.x, currentTooltipEvent.y));
        if (cell.getColumnIndex() != 0)
        {
            RowInfo row = ((RowInfo) cell.getViewerRow().getElement());
            TableColumn column = ((Table) viewer.getControl()).getColumn(cell.getColumnIndex());
            if (tooltipText != null
                    && !tooltipText.trim()
                            .equals(row.getCells().get(column.getText()).getComment()))
            {
                row.getCells().get(column.getText()).setComment(tooltipText.trim());
                if (this.editor != null)
                {
                    try
                    {
                        editor.getEditorInput().setCellTooltip(column.getText(), row.getKey(),
                                tooltipText);
                        editor.fireDirtyPropertyChanged();
                    }
                    catch (SequoyahException e)
                    {
                        BasePlugin.logError(Messages.StringEditorViewerEditableTooltipSupport_2 + column.getText()
                                + ", " + row.getKey() + Messages.StringEditorViewerEditableTooltipSupport_4 + tooltipText, e);
                    }
                }
            }
            this.tooltipText = null;
            currentTooltipEvent = null;
        }
        super.afterHideToolTip(event);
    }
}

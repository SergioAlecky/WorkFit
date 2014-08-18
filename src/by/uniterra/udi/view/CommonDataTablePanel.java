/**
 * Filename  : CommonDataTablePanel.java
 *
 * ***************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************************
 * Project    : WorkFit
 *
 * Author     : Anton Nedbailo
 *
 * last change by : $Author:$ 
 * last check in  : $Date: $
 */

package by.uniterra.udi.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;    
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import by.uniterra.dai.eao.ServiceBaseEAO;
import by.uniterra.udi.iface.IModelOwner;
import by.uniterra.udi.model.AbstractFlexTableModel;
import by.uniterra.udi.model.UDIPropSingleton;

/**
 * The <code>CommonDataTablePanel</code> is used for editind some DB table values
 *
 * @author Anton Nedbailo
 * @since 15 авг. 2014 г.
 */
public class CommonDataTablePanel extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -8892734531119563251L;
    
    // members
    private AbstractFlexTableModel model;
    private JTable tTable;
    @SuppressWarnings("rawtypes")
    private ServiceBaseEAO eaoCommon;
    private KeyEventDispatcher keyDispatcher;

    private List<Serializable> lstChangedRows;
    private List<Serializable> lstMonthToDelete;

    private IModelOwner moPanel;

    private static final String ACTION_SAVE_TO_MODEL = "Save to model";
    private static final String ACTION_REFRESH_TABLE = "Refresh";
    private static final String ACTION_DEL_ROW = "Delete row";
    private static final String ACTION_EDIT_ROW = "Edit row";

    public CommonDataTablePanel(AbstractFlexTableModel atmModel, IModelOwner moPanel, @SuppressWarnings("rawtypes") ServiceBaseEAO eaoCommon)
    {
        lstChangedRows = new ArrayList<Serializable>();
        lstMonthToDelete = new ArrayList<Serializable>();
        // init members
        this.model = atmModel;
        this.moPanel = moPanel;
        this.eaoCommon = eaoCommon;
        
        // create UI
        jbInit();

        // bring data from DB to view
        readValues();

        // unregister key dispatcher on shutdown action
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                if (keyDispatcher != null)
                {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
                }
            }
        });
    }

    private void readValues()
    {
        lstMonthToDelete.clear();
        lstChangedRows.clear();
        
        model.setTableData(eaoCommon.loadAll());
    }

    @SuppressWarnings("unchecked")
    public void writeValues()
    {
        for (Serializable obj : lstMonthToDelete)
        {
            eaoCommon.delete(obj);
        }
        for (Serializable obj : lstChangedRows)
        {
            eaoCommon.save(obj);
        }
    }

    private void jbInit()
    {
        setLayout(new GridBagLayout());
        tTable = new JTable(model);
        tTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // popup menu del row
        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItemDelRow = new JMenuItem(UDIPropSingleton.getString(this, "menuItemDelRow.label"));
        menuItemDelRow.setActionCommand(ACTION_DEL_ROW);
        menuItemDelRow.addActionListener(this);
        popup.add(menuItemDelRow);

        // popup edit row
        JMenuItem menuItemEditRow = new JMenuItem(UDIPropSingleton.getString(this, "menuItemEditRow.label"));
        menuItemEditRow.setActionCommand(ACTION_EDIT_ROW);
        menuItemEditRow.addActionListener(this);
        popup.add(menuItemEditRow);

        // add new row in db
        JButton buttonSaveDB = new JButton(UDIPropSingleton.getString(this, "buttonSaveDB.label"));
        buttonSaveDB.setActionCommand(ACTION_SAVE_TO_MODEL);
        buttonSaveDB.addActionListener(this);
        super.add(buttonSaveDB, new GridBagConstraints(2, 2, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

        JButton buttonRefreshDB = new JButton(UDIPropSingleton.getString(this, "buttonRefreshDB.label"));
        buttonRefreshDB.setActionCommand(ACTION_REFRESH_TABLE);
        buttonRefreshDB.addActionListener(this);
        super.add(buttonRefreshDB, new GridBagConstraints(2, 3, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

        final JScrollPane yearTableScrollPage = new JScrollPane(tTable);
        yearTableScrollPage.setPreferredSize(new Dimension(400, 400));
        super.add(yearTableScrollPage,
                new GridBagConstraints(2, 1, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

        // Register keyboard
        keyDispatcher = new KeyEventDispatcher()
        {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e)
            {
                // check key ID (reakt only on KEY_RELEASED event) and code
                // (react only on F5)
                boolean bResult = e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F5;
                // check result
                if (bResult)
                {
                    // do table data refresh
                    readValues();
                }
                return bResult;
            }
        };

        // register new KeyEventDispatcher
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);

        tTable.addMouseListener(new MouseAdapter()
        {

            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    editValuesFromModel();
                }
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showPopup(e);
            }

            private void showPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    // popup.show(e.getComponent(), e.getX(), e.getY());
                    // get the coordinates of the mouse click
                    Point p = e.getPoint();
                    // get source
                    JTable jtTable = (JTable) e.getSource();
                    // get the row index that contains that coordinate
                    int rowNumber = jtTable.rowAtPoint(p);
                    // Get the ListSelectionModel of the JTable
                    ListSelectionModel model = jtTable.getSelectionModel();
                    // set the selected interval of rows. Using the "rowNumber"
                    // variable for the beginning and end selects only that one
                    // row.
                    model.setSelectionInterval(rowNumber, rowNumber);
                    // show popup menu
                    popup.show(jtTable, e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        try
        {
            switch (arg0.getActionCommand())
            {
            case ACTION_SAVE_TO_MODEL:
                addValuesToModel();
                break;
            case ACTION_REFRESH_TABLE:
                readValues();
                break;
            case ACTION_DEL_ROW:
                removeValuesFromModel();
                break;
            case ACTION_EDIT_ROW:
                editValuesFromModel();
                break;
            default:
                break;
            }
        }
        catch (Exception e)
        {
            System.out.println("actionPerformed expressions");
            e.printStackTrace();
        }

    }

    public void addValuesToModel()
    {
        try
        {
            moPanel.setModel(eaoCommon.getClassType().newInstance());
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            System.out.println("addValuesToModel error ");
        }
        
        if (JOptionPane.showConfirmDialog(tTable, moPanel, UDIPropSingleton.getString(this, "addMonthOptionPanel.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
        {
            try
            {
                Serializable month = (Serializable) moPanel.getModel();
                model.addTableData(month);
                lstChangedRows.add(month);
            }
            catch (NumberFormatException ex)
            {
                showNumberErrorMessage();
            }
        }
        else
        {
            System.out.println("Input Canceled");
        }
    }

    public void removeValuesFromModel()
    {
        int[] arrSelIndexes = tTable.getSelectedRows();

        for (int i = 0; i < arrSelIndexes.length; i++)
        {
            // convert view to model index number
            int iModelIndex = (tTable.convertRowIndexToModel(arrSelIndexes[i]));
            Serializable yCurMonth = (Serializable) model.getRowData(iModelIndex);

            // check if it exists in DB
            if (eaoCommon.contains(yCurMonth))
            {
                lstMonthToDelete.add(yCurMonth);
            }

            // check if we edited the entity
            if (lstChangedRows.contains(yCurMonth))
            {
                lstChangedRows.remove(yCurMonth);
            }

            // dell data by index value
            model.removeTableData(iModelIndex);
        }
    }

    public void editValuesFromModel()
    {
        int[] arrSelIndexes = tTable.getSelectedRows();
        // convert view to model index number
        int iModelIndex = (tTable.convertRowIndexToModel(arrSelIndexes[0]));
        // get data by index value
        Serializable monthEditedModel = (Serializable) model.getRowData(iModelIndex);
        // check if it's already in changed list
        int iChangedListIndex = lstChangedRows.indexOf(monthEditedModel);

        moPanel.setModel(monthEditedModel);
        if (JOptionPane.showConfirmDialog(tTable, moPanel, UDIPropSingleton.getString(this, "editMonthOptionPanel.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
        {
            try
            {
                Serializable month = (Serializable) moPanel.getModel();
                model.setTableData(month, iModelIndex);
                // FIXME check if we already edited the same value
                if (iChangedListIndex != -1)
                {
                    lstChangedRows.set(iChangedListIndex, month);
                }
                else
                {
                    lstChangedRows.add(month);
                }
            }
            catch (NumberFormatException ex)
            {
                showNumberErrorMessage();
            }

        }
        else
        {
            System.out.println("Input Canceled");
        }
    }

    public void showNumberErrorMessage()
    {
        JOptionPane.showMessageDialog(null, UDIPropSingleton.getString(this, "errorMessageDialog.lable"),
                UDIPropSingleton.getString(this, "errorMessageDialog.title"), JOptionPane.ERROR_MESSAGE);
    }
}
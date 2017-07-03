/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.ArrayUtils;

import com.michaelbaranov.microba.calendar.DatePicker;

import ij.IJ;
import ij.plugin.PlugIn;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

public class VueRest extends JFrame implements PlugIn{
	private static final long serialVersionUID = 1L;

	JTabbedPane tabbedPane;
	private TableDataPatient modele = new TableDataPatient(); // model for the main JTable (tableau)
	private TableDataDetails modeleDetails = new TableDataDetails(); // model for the details JTable (tableauDetails) in the main tab
	private TableDataPatient modeleH = new TableDataPatient(); // model for the history JTable (tab History)
	private TableDataDetails modeleDetailsH = new TableDataDetails(); // model for the details JTable (tableauDetails) in the history tab
	private JTable tableau; // displayed table in the main tab
	private JTable tableauDetails; // displayed table containing the details in the main tab
	private JTable tableauH; // displayed table in the history tab
	private JTable tableauDetailsH; // displayed table containing the details in the history tab
	private JPopupMenu popMenu = new JPopupMenu(); // popMenu that will pop when the user right-clicks on a row
	//	private JPopupMenu popMenuH = new JPopupMenu(); // popMenu that will pop when the user right-clicks on a row

	/*
	 * The following components will be used to filter the tables, or make new searches
	 */
	private JComboBox<String> searchingParam; // indexes the "main" searching parameter (name, id, accession number)
	private JComboBox<Object> queryAET; // indexes every AETs available that the user can query from
	private JComboBox<Object> retrieveAET; // indexes every AETs available that the user can retrieve instances to
	private JLabel state; // allows the user to know the state of the retrieve query 
	private JTextField userInput; // associated with searchingParam to get the input
	private JPanel checkboxes; // contains every checkboxes
	private JCheckBox cr,ct,cmr,nm,pt,us,xa,mg; // the chosen modalities 
	private JTextField description; // allows to search for a particular description
	private DatePicker from, to; // allow to make a research in a user defined time frame
	private TableRowSorter<TableDataPatient> sorter; // used to filter and sort the rows for the main JTable
	private TableRowSorter<TableDataDetails> sorterDetails; // used to filter and sort the rows for the details JTable
	private JButton retrieve;
	
	// Tab History
	private JComboBox<Object> queryAETH; // indexes every AETs available that the user can get patient from (usually PACS)
	private JComboBox<Object> retrieveAETH; // indexes every AETs available that the user can retrieve instances to
	private JLabel stateH; // allows the user to know the state of the retrieve query
	private JPanel checkboxesH;
	private JCheckBox crH,ctH,cmrH,nmH,ptH,usH,xaH,mgH;  
	private DatePicker fromH, toH; // allow to make a research in a user defined time frame
	private TableRowSorter<TableDataPatient> sorterH; // used to sort the rows for the main JTable
	private TableRowSorter<TableDataDetails> sorterDetailsH; // used to filter and sort the rows for the details JTable
	private JButton retrieveH;
	
	// Tab Setup
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	// these list contains the selected rows's model's indexes in order to retrieve the series
	private ArrayList<Integer> rowsModelsIndexes = new ArrayList<>();
	private ArrayList<Integer> rowsModelsIndexesH = new ArrayList<>();


	public VueRest(){

		this.setTitle("Orthanc queries");
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////// SETUP ////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		int curDb = jprefer.getInt("current database", 0);

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////// END SETUP ///////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////


		///////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////     TAB 1 : QUERIES/RETRIEVE ///////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		this.sorter = new TableRowSorter<TableDataPatient>(modele);
		this.sorterDetails = new TableRowSorter<TableDataDetails>(modeleDetails);
		this.sorter.setSortsOnUpdates(true);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		JPanel north = new JPanel(new GridLayout(1,2));
		JPanel northG = new JPanel(new GridLayout(2,1));
		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.LINE_AXIS));
		JPanel southD = new JPanel(new FlowLayout());

		// Creating the main JTable containing the patients (the left one)
		tableau = new JTable(modele);
		tableau.setRowSorter(sorter);
		tableau.getTableHeader().setReorderingAllowed(false);

		// We configure the columns
		tableau.getColumnModel().getColumn(0).setMinWidth(170);
		tableau.getColumnModel().getColumn(0).setMaxWidth(170);
		tableau.getColumnModel().getColumn(0).setResizable(false);
		tableau.getColumnModel().getColumn(1).setMinWidth(140);
		tableau.getColumnModel().getColumn(1).setMaxWidth(140);
		tableau.getColumnModel().getColumn(1).setResizable(false);
		tableau.getColumnModel().getColumn(2).setMinWidth(95);
		tableau.getColumnModel().getColumn(2).setMaxWidth(95);
		tableau.getColumnModel().getColumn(2).setResizable(false);
		tableau.getColumnModel().getColumn(3).setMinWidth(130);
		tableau.getColumnModel().getColumn(3).setResizable(false);
		tableau.getColumnModel().getColumn(4).setMinWidth(120);
		tableau.getColumnModel().getColumn(4).setMaxWidth(120);
		tableau.getColumnModel().getColumn(4).setResizable(false);
		tableau.getColumnModel().getColumn(5).setMinWidth(0);
		tableau.getColumnModel().getColumn(5).setMaxWidth(0);
		tableau.getColumnModel().getColumn(5).setResizable(false);
		tableau.setPreferredScrollableViewportSize(new Dimension(655,400));

		// We sort the array this way by default :
		// first, alphabetically by name, then by date.
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();

		// Setting the table's sorter, renderer and popupmenu
		JMenuItem menuItemDisplayH = new JMenuItem("Display history");
		menuItemDisplayH.addActionListener(new displayHistoryAction());
		popMenu.add(menuItemDisplayH);
		tableau.setComponentPopupMenu(popMenu);
		tableau.addMouseListener(new TableMouseListener(tableau, modele, modeleDetails, queryAET, tableau.getSelectionModel(), state));

		tableau.setRowSorter(sorter);
		tableau.setDefaultRenderer(Date.class, new DateRenderer());

		// Creating tableauDetails which will contain the patients's details
		tableauDetails = new JTable(modeleDetails);
		tableauDetails.getTableHeader().setReorderingAllowed(false);

		// We configure the columns
		tableauDetails.getColumnModel().getColumn(0).setMinWidth(200);
		tableauDetails.getColumnModel().getColumn(0).setResizable(false);
		tableauDetails.getColumnModel().getColumn(1).setMinWidth(100);
		tableauDetails.getColumnModel().getColumn(1).setMaxWidth(100);
		tableauDetails.getColumnModel().getColumn(1).setResizable(false);
		tableauDetails.setPreferredScrollableViewportSize(new Dimension(300,400));
		tableauDetails.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				try{
					if(state.getText().equals("Done") && state != null){
						state.setText(null);
					}
				}catch(Exception e1){
					// Ignore
				}
			}
		});

		List<RowSorter.SortKey> sortKeysDetails = new ArrayList<>();
		sortKeysDetails.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeysDetails.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorterDetails.setSortKeys(sortKeysDetails);
		sorterDetails.setSortsOnUpdates(true);
		sorterDetails.sort();
		tableauDetails.setRowSorter(sorterDetails);

		// Creating the modalities checkboxes
		JPanel filtersPanel = new JPanel(new FlowLayout());
		checkboxes = new JPanel(new GridLayout(2,4));
		cr = new JCheckBox("CR");
		ct = new JCheckBox("CT");
		cmr = new JCheckBox("CMR");
		nm = new JCheckBox("NM");
		pt = new JCheckBox("PT");
		us = new JCheckBox("US");
		xa = new JCheckBox("XA");
		mg = new JCheckBox("MG");
		checkboxes.add(cr); checkboxes.add(ct);
		checkboxes.add(cmr); checkboxes.add(nm);
		checkboxes.add(pt); checkboxes.add(us);
		checkboxes.add(xa); checkboxes.add(mg);

		Object[] tabAETs = {""};

		// Creating the queryAET comboBox
		boolean successful = false;
		try{
			tabAETs = modele.getAETs();
			successful = true;
			queryAET = new JComboBox<Object>(tabAETs);
		}catch(IOException e1){
			e1.printStackTrace();
		}catch(NullPointerException e){
			JOptionPane.showMessageDialog(null, "Please set an AET before using this app (You will have to close it).",
					"No AET found", JOptionPane.INFORMATION_MESSAGE);
		}finally{
			if(!successful){
				if(jprefer.get("db type" + curDb , "99").equals("5")){
					String newAdress = JOptionPane.showInputDialog("A problem occurred, \n"
							+ "You may not have declared any AET, or the connection setup is false"
							+ " (check your BI database/AET settings) \n"
							+ "The program will have to be reloaded", jprefer.get("db path" + curDb, "http://localhost:8042"));
					jprefer.put("db path" + curDb, newAdress);
				}else{
					IJ.runMacro("run(\"Launch setup\");");
				}
			}
			queryAET = new JComboBox<Object>(tabAETs);
			try {
				tabAETs = modele.getAETs();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NullPointerException e){
				//Ignore
			}

		}

		queryAET.setBorder(new EmptyBorder(0, 30, 0, 0));
		if(tabAETs.length > 0){
			if(jpreferPerso.getInt("SearchAET", 99) < tabAETs.length){
				queryAET.setSelectedIndex(jpreferPerso.getInt("SearchAET", 99));
			}else{
				queryAET.setSelectedIndex(0);	
			}
		}
		queryAET.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				modele.clear();
				modeleDetails.clear();
			}
		});

		// Creating the text inputs
		JPanel textInput = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String[] patientParam = {"Patient name", "Patient ID", "Accession number"};
		searchingParam = new JComboBox<String>(patientParam);
		searchingParam.setSelectedIndex(jpreferPerso.getInt("InputParameter", 0));
		userInput = new JTextField();
		userInput.setToolTipText("Set your input accordingly to the field combobox on the left. ('*' stands for any character)");
		description = new JTextField();
		description.setToolTipText("Study's description. ('*' stands for any character)");
		userInput.setText("*");
		description.setText("*");
		userInput.setPreferredSize(new Dimension(90,20));
		description.setPreferredSize(new Dimension(90,20));

		retrieveAET = new JComboBox<Object>(new Object[]{""});

		// Creating the "search" button (ajouter)
		JButton ajouter = new JButton(new SearchAction(this));
		try {
			Object[] listRetrieveAET = ArrayUtils.addAll(modeleDetails.getDicomAETs(), tabAETs);
			retrieveAET = new JComboBox<Object>(listRetrieveAET);
		} catch (IOException e3) {
			e3.printStackTrace();
		} catch (NullPointerException e){
			//Ignore
		}

		// Creating the datepickers
		JPanel dates = new JPanel(new FlowLayout(FlowLayout.LEFT));
		from = new DatePicker(new Date(), new SimpleDateFormat("MM-dd-yyyy"));
		from.setBorder(new EmptyBorder(0, 5, 0 ,0));
		from.setToolTipText("Date format : MM-dd-yyyy");
		to = new DatePicker(new Date(), new SimpleDateFormat("MM-dd-yyyy"));
		to.setBorder(new EmptyBorder(0, 5, 0 ,0));
		to.setToolTipText("Date format : MM-dd-yyyy");

		// Creating the dates JPanel
		dates.add(new JLabel("From", SwingConstants.RIGHT));
		dates.add(from);
		dates.add(new JLabel("To", SwingConstants.RIGHT));
		dates.add(to);
		dates.add(queryAET);
		dates.add(ajouter);

		// Building the components for the southern part of the window : the AETs combobox, the Search and Filter buttons
		this.state = new JLabel();
		southD.add(retrieveAET);
		retrieve = new JButton(new RetrieveAction(rowsModelsIndexes, tableauDetails, modeleDetails, state, retrieveAET));
		southD.add(retrieve);
		southD.add(state);
		south.add(southD);

		// Setting the mouse listener on tableau
		tableau.addMouseListener(new TableMouseListener(tableau, modele, modeleDetails, queryAET, tableau.getSelectionModel(), state));

		// Setting the rowSelection that will allow for retrieves on specific series
		ListSelectionModel rowSelectionModel = tableauDetails.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		rowSelectionModel.addListSelectionListener(new TableListSelectionListener(rowsModelsIndexes, tableauDetails));

		///////////////////////////////////////////////////////////////////////////////////////////////////////		
		////////////////////////////////// END OF TAB 1 : QUERIES/RETRIEVE ////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////



		///////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////     TAB 2 : HISTORY ////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		sorterH = new TableRowSorter<TableDataPatient>(modeleH);
		sorterH.setSortsOnUpdates(true);
		sorterDetailsH = new TableRowSorter<TableDataDetails>(modeleDetailsH);

		JPanel mainPanelH = new JPanel(new GridBagLayout());
		JPanel northH = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tableauH = new JTable(modeleH);

		tableauH.setRowSorter(sorterH);
		tableauH.getTableHeader().setReorderingAllowed(false);

		// We configure the columns
		tableauH.getColumnModel().getColumn(0).setMinWidth(170);
		tableauH.getColumnModel().getColumn(0).setMaxWidth(170);
		tableauH.getColumnModel().getColumn(0).setResizable(false);
		tableauH.getColumnModel().getColumn(1).setMinWidth(140);
		tableauH.getColumnModel().getColumn(1).setMaxWidth(140);
		tableauH.getColumnModel().getColumn(1).setResizable(false);
		tableauH.getColumnModel().getColumn(2).setMinWidth(95);
		tableauH.getColumnModel().getColumn(2).setMaxWidth(95);
		tableauH.getColumnModel().getColumn(2).setResizable(false);
		tableauH.getColumnModel().getColumn(3).setMinWidth(130);
		tableauH.getColumnModel().getColumn(3).setResizable(false);
		tableauH.getColumnModel().getColumn(4).setMinWidth(120);
		tableauH.getColumnModel().getColumn(4).setMaxWidth(120);
		tableauH.getColumnModel().getColumn(4).setResizable(false);
		tableauH.getColumnModel().getColumn(5).setMinWidth(0);
		tableauH.getColumnModel().getColumn(5).setMaxWidth(0);
		tableauH.getColumnModel().getColumn(5).setResizable(false);
		// 655 400
		tableauH.setPreferredScrollableViewportSize(new Dimension(655,400));

		// We sort the array this way by default :
		// first, alphabetically by name, then by modality and finally, by description.
		sorterH.setSortKeys(sortKeys);
		sorterH.sort();

		// Setting the table's sorter and renderer
		tableauH.setRowSorter(sorterH);
		tableauH.setDefaultRenderer(Date.class, new DateRenderer());


		// Creating tableauDetailsH which will contain the patients's details
		tableauDetailsH = new JTable(modeleDetailsH);		
		tableauDetailsH.getTableHeader().setReorderingAllowed(false);

		// We configure the columns
		tableauDetailsH.getColumnModel().getColumn(0).setMinWidth(200);
		tableauDetailsH.getColumnModel().getColumn(0).setResizable(false);
		tableauDetailsH.getColumnModel().getColumn(1).setMinWidth(100);
		tableauDetailsH.getColumnModel().getColumn(1).setMaxWidth(100);
		tableauDetailsH.getColumnModel().getColumn(1).setResizable(false);
		tableauDetailsH.setPreferredScrollableViewportSize(new Dimension(300,400));

		//Setting the sorter for tableauDetailsH
		sorterDetailsH.setSortsOnUpdates(true);
		sorterDetailsH.setSortKeys(sortKeysDetails);
		sorterDetailsH.sort();
		tableauDetailsH.setRowSorter(sorterDetailsH);


		// Creating the modalities checkboxes
		JPanel filtersPanelH = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		checkboxesH = new JPanel(new GridLayout(2,4));
		crH = new JCheckBox("CR");
		ctH = new JCheckBox("CT");
		cmrH = new JCheckBox("CMR");
		nmH = new JCheckBox("NM");
		ptH = new JCheckBox("PT");
		usH = new JCheckBox("US");
		xaH = new JCheckBox("XA");
		mgH = new JCheckBox("MG");
		checkboxesH.add(crH); checkboxesH.add(ctH);
		checkboxesH.add(cmrH); checkboxesH.add(nmH);
		checkboxesH.add(ptH); checkboxesH.add(usH);
		checkboxesH.add(xaH); checkboxesH.add(mgH);

		// Creating the user input's components
		JPanel datesH = new JPanel(new FlowLayout());		 

		// For the tab history, fromH date is set to 01/01/1980 by default
		try {
			fromH = new DatePicker(new SimpleDateFormat("MM-dd-yy").parse("01-01-1980"), new SimpleDateFormat("MM-dd-yyyy"));
			fromH.setBorder(new EmptyBorder(0, 5, 0 ,0));
			fromH.setToolTipText("Date format : MM-dd-yyyy");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		toH = new DatePicker(new Date(), new SimpleDateFormat("MM-dd-yyyy"));
		toH.setBorder(new EmptyBorder(0, 5, 0 ,0));
		toH.setToolTipText("Date format : MM-dd-yyyy");

		Object[] tabAETsH = {""};
		try {
			tabAETsH = modeleH.getAETs();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NullPointerException e){
			//Ignore
		}

		queryAETH = new JComboBox<Object>(tabAETsH);
		queryAETH.setMinimumSize(new Dimension(200,20));
		queryAETH.setMaximumSize(new Dimension(200,20));
		if(tabAETsH.length > 0){
			if(jpreferPerso.getInt("HistoryAET", 0) < tabAETsH.length){
				queryAETH.setSelectedIndex(jpreferPerso.getInt("HistoryAET", 0));
			}else{
				queryAETH.setSelectedIndex(0);
			}
		}
		queryAETH.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if(tableau.getRowCount() != 0){
					String patientName = (String)tableau.getValueAt(tableau.getSelectedRow(), 0);
					String patientID = (String)tableau.getValueAt(tableau.getSelectedRow(), 1);
					// We clear the table completely before any queries
					modeleH.clear();
					modeleDetailsH.clear();
					try {
						modeleH.addPatient(patientName, patientID, "*", "*", "*", "*", queryAETH.getSelectedItem().toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		Object[] listRetrieveAETH = {""};

		try {
				listRetrieveAETH = ArrayUtils.addAll(modeleDetailsH.getDicomAETs(), tabAETsH);
		} catch (IOException e3) {
			e3.printStackTrace();
		} catch (NullPointerException e){
			//Ignore
		}

		retrieveAETH = new JComboBox<Object>(listRetrieveAETH);

		datesH.add(new JLabel("From"));
		datesH.add(fromH);
		datesH.add(new JLabel("To"));
		datesH.add(toH);

		// Creating the JPanel for datesH and queryAETH
		JPanel northGH = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northGH.add(queryAETH);
		northGH.add(datesH);
		JButton filter = new JButton("Filter");
		filter.addActionListener(new FilterAction(stateH, checkboxesH, modeleH, modeleDetailsH, fromH, toH, queryAETH));

		//Creating the southern panel
		JPanel southH = new JPanel(new FlowLayout());
		stateH = new JLabel();
		stateH.setText(null);
		southH.add(this.retrieveAETH);
		retrieveH = new JButton(new RetrieveAction(rowsModelsIndexesH, tableauDetailsH, modeleDetailsH, stateH, retrieveAETH));
		southH.add(retrieveH);
		southH.add(this.stateH);
		tableauH.addMouseListener(new TableMouseListener(tableauH, modeleH, modeleDetailsH, queryAETH, tableauH.getSelectionModel(), stateH));		

		// Setting the rowSelection that will allow for retrieves
		ListSelectionModel rowSelectionModelH = tableauDetailsH.getSelectionModel();
		rowSelectionModelH.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		rowSelectionModelH.addListSelectionListener(new TableListSelectionListener(rowsModelsIndexesH, tableauDetailsH));


		///////////////////////////////////////////////////////////////////////////////////////////////////////		
		////////////////////////////////// END OF TAB 2 : HISTORY /////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////


		// Adding the components for the main tab p1
		textInput.add(searchingParam);
		textInput.add(userInput);
		textInput.add(new JLabel("Description"));
		textInput.add(description);
		northG.add(textInput);
		northG.add(dates);
		north.add(northG);
		filtersPanel.add(checkboxes);
		north.add(filtersPanel);

		GridBagConstraints c = new GridBagConstraints();
		JScrollPane jscp = new JScrollPane(tableau);
		jscp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jscp.setPreferredSize(new Dimension((int)tableau.getPreferredSize().getWidth() + 20 , (int)tableau.getPreferredSize().getHeight()));
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(jscp,c);

		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(new JScrollPane(tableauDetails),c);

		// Creating and adding the JPanels to the contentPane
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1,BoxLayout.PAGE_AXIS));
		p1.add(north);
		p1.add(mainPanel);
		p1.add(south);

		// Adding the components for the main tab p1
		northH.add(northGH);
		filtersPanelH.add(checkboxesH);
		northH.add(filtersPanelH);
		northH.add(filter);

		GridBagConstraints cH = new GridBagConstraints();
		JScrollPane jscpH = new JScrollPane(tableauH);
		jscpH.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jscpH.setPreferredSize(new Dimension((int)tableauH.getPreferredSize().getWidth() + 20 , (int)tableauH.getPreferredSize().getHeight()));
		cH.gridx = 0;
		cH.gridy = 0;
		cH.weightx = 1;
		cH.weighty = 1;
		cH.fill = GridBagConstraints.BOTH;
		mainPanelH.add(jscpH,cH);

		cH.gridx = 1;
		cH.gridy = 0;
		cH.weightx = 1;
		cH.weighty = 1;
		cH.fill = GridBagConstraints.BOTH;
		mainPanelH.add(new JScrollPane(tableauDetailsH),c);
		mainPanelH.setBackground(Color.DARK_GRAY);

		// Adding components to p2
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2,BoxLayout.PAGE_AXIS));
		p2.add(northH);
		p2.add(mainPanelH);
		p2.add(southH);

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Queries/Retrieve", p1);
		tabbedPane.add("History", p2);

		// Initially, the default button is ajouter, but we add a changelistener
		// on the tab so that the default button changes accordingly
		this.getRootPane().setDefaultButton(ajouter);
		tabbedPane.addChangeListener(new ChangeButtonListener(this, ajouter, filter));

		Image image = new ImageIcon(ClassLoader.getSystemResource("OrthancIcon.png")).getImage();
		this.setIconImage(image);
		this.getContentPane().add(tabbedPane);
	}

	/*
	 * This class defines the action on the "Search" button, that is, adding patients to the model.
	 */
	private class SearchAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private JFrame frame;

		private SearchAction(JFrame frame) {
			super("Search");
			this.frame = frame;
		}

		public void actionPerformed(ActionEvent e) {
			state.setText(null);

			// Saving search parameters
			jpreferPerso.putInt("InputParameter", searchingParam.getSelectedIndex());
			jpreferPerso.putInt("SearchAET", queryAET.getSelectedIndex());
			jpreferPerso.putInt("HistoryAET", queryAETH.getSelectedIndex());

			// Making a DateFormat for the query
			DateFormat df = new SimpleDateFormat("yyyyMMdd");

			StringBuilder modalities = new StringBuilder();

			// We append a StringBuilder with every selected modalities.
			// We append "\\\\" in order to get the double \ which allows for multiple modalities in the query
			for(Component c : checkboxes.getComponents()){
				if(c instanceof JCheckBox){
					if(((JCheckBox) c).isSelected()){
						modalities.append((((JCheckBox) c).getText()));
						modalities.append("\\\\");
					}
				}
			}

			boolean successful = false;
			// If the checkbox is the last chosen checkbox, we delete the '\\\\' at the end
			if(modalities.length() != 0 && modalities.charAt(modalities.length() - 1) == '\\'){
				modalities.deleteCharAt(modalities.length() - 1);
				if(modalities.charAt(modalities.length() - 1) == '\\'){
					modalities.deleteCharAt(modalities.length() - 1);
				}
			}
			// We clear the tables completely before any queries
			modele.clear();
			modeleH.clear();
			modeleDetails.clear();
			modeleDetailsH.clear();

			// We make the query, based on the user's input
			try {
				if(modalities.toString().length() == 0){
					modalities.append("*");
				}

				// Query with the patient's name
				if (searchingParam.getSelectedItem().equals("Patient name")){
					successful = modele.addPatient(userInput.getText().toUpperCase(), "*", 
							df.format(from.getDate().getTime())+"-"+df.format(to.getDate().getTime()), 
							modalities.toString(), description.getText(),"*", queryAET.getSelectedItem().toString());
				}
				// Query with the patient's ID
				else if(searchingParam.getSelectedItem().equals("Patient ID")){
					successful = modele.addPatient("*", userInput.getText(), 
							df.format(from.getDate().getTime())+"-"+df.format(to.getDate().getTime()), 
							modalities.toString(), description.getText(),"*", queryAET.getSelectedItem().toString());
				}else{
					// Query with the patient's accession number
					successful = modele.addPatient("*", "*", df.format(from.getDate().getTime())+"-"+df.format(to.getDate().getTime()), 
							modalities.toString(), description.getText(),userInput.getText(), queryAET.getSelectedItem().toString());
				}
			} catch (Exception e1) {
				// ignore
			}finally{
				if(!successful){
					String curDb = jprefer.get("current database", "99");
					if(jprefer.get("db type" + curDb , "99") == "5"){
						String newAdress = JOptionPane.showInputDialog("Re-write a valid path "
								+ "(check your settings in BI database plugin; orthanc query will"
								+ " have to be reloaded)", jprefer.get("db path" + curDb, "http://localhost:8042"));
						jprefer.put("db path" + curDb, newAdress);
					}else{
						IJ.runMacro("run(\"Launch setup\");");
					}
					frame.dispose();
				}
			}
		}
	}



	/*
	 * This class defines the action on pop menu, that is, displaying the patient's history.
	 */
	private class displayHistoryAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent event) {
			state.setText(null);
			String patientName = (String)tableau.getValueAt(tableau.getSelectedRow(), 0);
			String patientID = (String)tableau.getValueAt(tableau.getSelectedRow(), 1);
			// We clear the table completely before any queries
			modeleH.clear();
			modeleDetailsH.clear();
			try {
				modeleH.addPatient(patientName, patientID, "*", "*", "*", "*", queryAETH.getSelectedItem().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			tabbedPane.setSelectedIndex(1);
		}

	}

	private class RetrieveAction extends AbstractAction{

		private static final long serialVersionUID = 1L;
		private ArrayList<Integer> rowsModelsIndexes;
		private JTable tableauDetails;
		private TableDataDetails modeleDetails;
		private JLabel state;
		private JComboBox<Object> retrieveAET;

		public RetrieveAction(ArrayList<Integer> rowsModelsIndexes, JTable tableauDetails, 
				TableDataDetails modeleDetails, JLabel state, JComboBox<Object> retrieveAET){
			super("Retrieve");
			this.rowsModelsIndexes = rowsModelsIndexes;
			this.tableauDetails = tableauDetails;
			this.modeleDetails = modeleDetails;
			this.state = state;
			this.retrieveAET = retrieveAET;
		}


		@Override
		public void actionPerformed(ActionEvent arg0) {
			SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

				@Override
				protected Void doInBackground() throws Exception {
					retrieve.setEnabled(false);
					retrieveH.setEnabled(false);
					try {
						if(rowsModelsIndexes.size() == 0){
							// If whole studies/study were/was selected
							DateFormat df = new SimpleDateFormat("yyyyMMdd");
							for(Integer row : tableau.getSelectedRows()){
								modeleDetails.clear();
								Date date = (Date)tableau.getValueAt(row, 2);
								String patientName = (String)tableau.getValueAt(row, 0);
								String patientID = (String)tableau.getValueAt(row, 1);
								String studyDate = df.format(date); 
								String studyDescription = (String)tableau.getValueAt(row, 3);
								String accessionNumber = (String)tableau.getValueAt(row, 4);
								String studyInstanceUID = (String)tableau.getValueAt(row, 5);

								modeleDetails.addDetails(patientName, patientID, studyDate, studyDescription, accessionNumber, studyInstanceUID, queryAET.getSelectedItem().toString());
								for(int i = 0; i < tableauDetails.getRowCount(); i++){
									state.setText("<html>Patient " + (row+1) + "/" + tableau.getSelectedRows().length + " - Retrieve state  " + (i+1) + "/" + tableauDetails.getRowCount() + 
											" <font color='red'> (Do not touch any buttons or any tables while the retrieve is not done)</font></html>");
									modeleDetails.retrieve(modeleDetails.getQueryID(i), String.valueOf(i), 
											retrieveAET.getSelectedItem().toString());
								}
							}
							tableau.setRowSelectionInterval(0,0);
						}else{
							// If only series were selected
							int i = 0;
							for(int j : rowsModelsIndexes){
								state.setText("<html>Retrieve state  " + (i+1) + "/" + rowsModelsIndexes.size()  + 
										" <font color='red'>(Do not touch any buttons or any tables while the retrieve is not done)</font></html>");
								modeleDetails.retrieve(modeleDetails.getQueryID(j), String.valueOf(j), 
										retrieveAET.getSelectedItem().toString());
							}
							i++;
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}catch (Exception e){
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void done(){
					retrieve.setEnabled(true);
					retrieveH.setEnabled(true);
					state.setText("<html><font color='green'>The data have successfully been retrieved.</font></html>");
				}
			};
			worker.execute();
		}
	}

	/*
	 * Displaying the frame
	 */
	public static void main(String... args){
		VueRest vue = new VueRest();	
		vue.setSize(1200, 400);
		vue.pack();
		vue.setVisible(true);
	}

	@Override
	public void run(String string) {
		VueRest vue = new VueRest();
		vue.setSize(1200, 400);
		vue.pack();
		vue.setVisible(true);
	}
}


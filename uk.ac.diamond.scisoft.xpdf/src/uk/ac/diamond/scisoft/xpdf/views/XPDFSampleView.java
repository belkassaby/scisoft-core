/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class XPDFSampleView extends ViewPart {
	
	private TableViewer sampleTV;
	
	private List<XPDFSampleParameters> samples;
	
	private Button cifButton;
	private Button eraButton;
	private Button simButton;
	private Button savButton;
	
	private Action loadTestDataAction;
	private Action simPDFAction;
	private Action saveAction;
	private Action newCifAction;
	private Action newEraAction;
	private Action pointBreakAction;
	
	public XPDFSampleView() {
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public XPDFSampleView(Composite parent, int style) {
		createPartControl(parent);
	}

	@Override
	public void createPartControl(Composite parent) {
		
		// Make the data container not-null
		samples = new ArrayList<XPDFSampleParameters>();
		
		// Overall composite of the view
		Composite sampleTableCompo = new Composite(parent, SWT.BORDER);
		sampleTableCompo.setLayout(new FormLayout());
		
		// Composite to define the table columns
		Composite tableCompo = new Composite(sampleTableCompo, SWT.NONE);
		TableColumnLayout tCL = new TableColumnLayout();
		tableCompo.setLayout(tCL);
		FormData formData= new FormData(800, 600);
		tableCompo.setLayoutData(formData);
		// Table viewer to hold the main data table
		sampleTV = new TableViewer(tableCompo);

		sampleTV.getTable().setHeaderVisible(true);
		sampleTV.getTable().setLinesVisible(true);
		
		sampleTV.setContentProvider(new SampleParametersContentProvider());
		sampleTV.setInput(getViewSite());
		
		createColumns(tCL);
		
		createActions();
		createLoadButtons(tableCompo);
		createRHSButtons(tableCompo);
	}

	private void createColumns(TableColumnLayout tCL) {
		TableViewerColumn col;
		Deque<String> columnNames = new LinkedList<String>(Arrays.asList(new String[]
				{"Sample name", "code", "", "Phases", "Composition", "Density", "Vol. frac.", "Energy", "μ", "Max capillary ID", "Energy", "Container"})); 
		Deque<Integer> columnWeights = new LinkedList<Integer>(Arrays.asList(new Integer[]
				{20, 10, 2, 10, 15, 10, 5, 5, 5, 15, 10, 10}));
		Deque<EditingSupport> columnES = new LinkedList<EditingSupport>(Arrays.asList(new EditingSupport[]
				{new StringCES(), null, null, new StringCES(), new StringCES()}));
		while (!columnNames.isEmpty()) {
			col = new TableViewerColumn(sampleTV, SWT.NONE);
			col.getColumn().setText(columnNames.pop());
			tCL.setColumnData(col.getColumn(), new ColumnWeightData(columnWeights.pop(), 10, true));
			col.setLabelProvider(new primitiveCLP());
			if (!columnES.isEmpty()) {
				if (columnES.peek() != null)
					col.setEditingSupport(columnES.pop());
				else
					columnES.pop();
			}
		}
	}
	
	class primitiveCLP extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof String)
				return (String) element;
			else if (element instanceof Integer)
				return ((Integer) element).toString();
			else if (element instanceof Double)
				return ((Double) element).toString();
			else
				return "+";
			}
	}
	
	
	interface StringCESFunctor {
		
	}
	
	class StringCES extends EditingSupport {
		
//		private final StringCESFunctor functor;
		
		public StringCES() {//StringCESFunctor functor){
			super(sampleTV);
//			this.functor = functor; 
		};
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(sampleTV.getTable());
		}
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
		@Override
		protected Object getValue(Object element) {
			return element;
		}
		@Override
		protected void setValue(Object element, Object value) {
			element = value;
		}
	}
		
		
	private void createActions() {
		loadTestDataAction = new LoadTestDataAction();
		loadTestDataAction.setText("Load test data");
		loadTestDataAction.setToolTipText("Load the test data");
		loadTestDataAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		
		pointBreakAction = new Action() {
			@Override
			public void run() {
				samples.get(0).getDensity();
			}
		};
		pointBreakAction.setToolTipText("Ze goggles, zey...");
		pointBreakAction.setText("...do nothing");
		pointBreakAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
		
		hookIntoContextMenu();
	}

	private void createRHSButtons(Composite compoAbove) {
		int rightMargin = -10;
		int topMargin = 10;
		Composite stCompo = compoAbove.getParent();
		simButton = new Button(stCompo, SWT.NONE);
		FormData formData= new FormData();
		formData.right = new FormAttachment(100, rightMargin);
		formData.top = new FormAttachment(compoAbove, topMargin);
		simButton.setLayoutData(formData);
		simButton.setText("Simulate PDF");
		simButton.setToolTipText("Produce a simulated pair distribution function for the selected sample");
		savButton = new Button(stCompo, SWT.NONE);
		formData = new FormData();
		formData.right = new FormAttachment(100, rightMargin);
		formData.top = new FormAttachment(simButton, topMargin);
		savButton.setLayoutData(formData);
		savButton.setText("Save");
		savButton.setToolTipText("Save the sample data to file (or the database?)");
		
	}

	private void createLoadButtons(Composite compoAbove) {
		int leftMargin = 10;
		int topMargin = 10;
		Composite stCompo = compoAbove.getParent();
		cifButton = new Button(stCompo, SWT.NONE);
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, leftMargin);
		formData.top = new FormAttachment(compoAbove, topMargin);
		cifButton.setLayoutData(formData);
		cifButton.setText("New sample from CIF file");
		cifButton.setToolTipText("Create new sample from the data contained in a specified Crystallographic Information File.");
		eraButton = new Button(stCompo, SWT.NONE);
		formData = new FormData();
		formData.left = new FormAttachment(0, leftMargin);
		formData.top = new FormAttachment(cifButton, topMargin);
		eraButton.setLayoutData(formData);
		eraButton.setText("New sample from ERA file");
		eraButton.setToolTipText("Create a new sample from the data contained in a specified ERA file.");
	}

	@Override
	public void setFocus() {
		sampleTV.getControl().setFocus();
	}
	
	// Hook actions into the context menu
	private void hookIntoContextMenu() {
		MenuManager menuMan = new MenuManager("#PopupMenu");
		menuMan.setRemoveAllWhenShown(true);
		menuMan.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);				
			}
		});
		Menu popupMenu = menuMan.createContextMenu(sampleTV.getControl());
		sampleTV.getControl().setMenu(popupMenu);
		getSite().registerContextMenu(menuMan, sampleTV);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(loadTestDataAction);
		manager.add(new Separator("Debug"));
		manager.add(pointBreakAction);
	}


	class SampleParametersContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {} // TODO Auto-generated method stub

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {} // TODO Auto-generated method stub

		@Override
		public Object[] getElements(Object inputElement) {
			return samples.toArray(new XPDFSampleParameters[]{});
		}
		
	}
	
	// Sample data with integer multiplicities
	class LoadTestDataAction extends Action {

		@Override
		public void run() {
			samples = new ArrayList<XPDFSampleParameters>();
			
			int currentID = 154;
			
			// barium titanate
			XPDFSampleParameters bto = new XPDFSampleParameters();
			bto.setName("Barium Titanate");
			bto.setId(currentID++);
			bto.setPhases(new ArrayList<String>(Arrays.asList(new String[] {"BTO", "CaTiO3"})));
			bto.setComposition("BaTiO3"); // Should be "Ba0.9Ca0.1TiO3"
			bto.setDensity(3.71);
			// Packing fraction as default
			bto.setSuggestedEnergy(76.6);
			bto.setSuggestedCapDiameter(1.0);
			bto.setBeamState("76.6 Hi Flux");
			bto.setContainer("0.3 mm B");
			
			samples.add(bto);
			
			// rutile
			XPDFSampleParameters rutile = new XPDFSampleParameters();
			rutile.setName("Rutile");
			rutile.setId(currentID++);
			rutile.setPhases(new ArrayList<String>(Arrays.asList(new String[] {"TiO2"})));
			rutile.setComposition("TiO2");
			rutile.setDensity(6.67);
			// Packing fraction as result
			rutile.setSuggestedEnergy(76.6);
			rutile.setSuggestedCapDiameter(5.0);
			rutile.setBeamState("76.6 Hi Flux");
			rutile.setContainer("0.5 mm B");
			
			samples.add(rutile);
			
			// and something else
			XPDFSampleParameters explodite = new XPDFSampleParameters();
			explodite.setName("Explodite");
			explodite.setId(currentID++);
			explodite.setPhases(new ArrayList<String>(Arrays.asList(new String[] {"LF", "O"})));
			explodite.setComposition("K2S4P");
			explodite.setDensity(1.1);
			//packing fraction as default
			explodite.setSuggestedEnergy(76.6);
			explodite.setSuggestedCapDiameter(5.0);
			explodite.setBeamState("76.6 Hi Flux");
			explodite.setContainer("0.5 mm");
			
			samples.add(explodite);
			
			sampleTV.refresh();
		}
	}
}
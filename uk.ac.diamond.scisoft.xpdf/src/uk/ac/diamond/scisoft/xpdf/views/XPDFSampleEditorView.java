/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf.views;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class XPDFSampleEditorView extends ViewPart {
	
	private List<XPDFSampleParameters> samples;
	
	private Button cifButton;
	private Button eraButton;
	private Button simButton;
	private Button savButton;
	private Button delButton;
	private Button clrButton;
	
	private Action loadTestDataAction;
	private Action simPDFAction;
	private Action saveAction;
	private Action newCifAction;
	private Action newEraAction;
	private Action newBlankAction;
	private Action pointBreakAction;
	private Action deleteAction;
	private Action clearAction;
		
//	private Map<ColumnGroup, TableViewer> groupViewers;
	private List<TableViewer> groupViewers; // To MyGroupedTable
	private List<String> groupNames; // To MyGroupedTable
	
	private boolean propagateSelectionChange; // To MyGroupedTable

	private SortedSet<Integer> usedIDs; // The set of assigned ID numbers. Should come from the database eventually?	

	private SampleGroupedTable sampleTable;

	// members that will be moved to within sampleTable
	List<String> groupNamesST; // To SampleGroupedTable
	List<List<String>> groupedColumnNames; // To SampleGroupedTable
	List<List<Integer>> groupedColumnWeights; // To SampleGroupedTable
	
	public XPDFSampleEditorView() {
		propagateSelectionChange = true;
	}
	
	public XPDFSampleEditorView(Composite parent, int style) {
		createPartControl(parent);
	}

	@Override
	public void createPartControl(Composite parent) {
		
		// Make the data container not-null
		samples = new ArrayList<XPDFSampleParameters>();
		
		// Overall composite of the view
		Composite sampleTableCompo = new Composite(parent, SWT.BORDER);
		sampleTableCompo.setLayout(new FormLayout());
		
		sampleTable = new SampleGroupedTable(sampleTableCompo, SWT.NONE);
		Composite buttonCompo = new Composite(sampleTableCompo, SWT.NONE);

		FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(buttonCompo);
		//		formData.height = 800;
		sampleTable.setLayoutData(formData);
		
		
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
//		formData.top = new FormAttachment(sampleTable);
		formData.bottom = new FormAttachment(100);
		buttonCompo.setLayoutData(formData);
		buttonCompo.setLayout(new FormLayout());
		
		createActions();
		createLoadButtons(buttonCompo);
		createCentreButtons(buttonCompo);
		createRHSButtons(buttonCompo);
	}

	class SampleGroupedTable extends Composite {
		
		MyGroupedTable groupedTable;
		
		public SampleGroupedTable(Composite parent, int style) {
			super(parent, style);
				
			groupedTable = new MyGroupedTable(this, SWT.NONE);
			this.setLayout(new FormLayout());
			FormData formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(0);
			formData.bottom = new FormAttachment(100);
			groupedTable.setLayoutData(formData);

			groupNamesST = new ArrayList<String>();
			groupedColumnNames = new ArrayList<List<String>>();
			groupedColumnWeights = new ArrayList<List<Integer>>();
			
			groupNamesST.add("Sample Identification");
			groupedColumnNames.add(Arrays.asList(new String[] {"Sample name", "Code"}));
			groupedColumnWeights.add(Arrays.asList(new Integer[] {20, 10}));
			
			groupNamesST.add("Sample Details");
			groupedColumnNames.add(Arrays.asList(new String[] {"", "Phases", "Composition", "Density", "Vol. frac."}));
			groupedColumnWeights.add(Arrays.asList(new Integer[] {2, 10, 15, 10, 5}));
			
			groupNamesST.add("Suggested Parameters");
			groupedColumnNames.add(Arrays.asList(new String[] {"Energy", "μ", "Max capillary ID"}));
			groupedColumnWeights.add(Arrays.asList(new Integer[] {5, 5, 15}));
			
			groupNamesST.add("Chosen Parameters");
			groupedColumnNames.add(Arrays.asList(new String[] {"Beam state", "Container"}));
			groupedColumnWeights.add(Arrays.asList(new Integer[] {10, 10}));

			groupedTable.createColumnGroups();

//			for (String name : groupNamesST)
//				groupedTable.createColumnGroup(name);
			
			groupedTable.setContentProvider(new IStructuredContentProvider() {
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}	// TODO Auto-generated method stub

				@Override
				public void dispose() {} // TODO Auto-generated method stub

				@Override
				public Object[] getElements(Object inputElement) {
					return samples.toArray();
				}
			});
			
			groupedTable.setInput(getSite()); // FIXME: watch out for this becoming invalid when the class is broken out 
			
			List<String> allColumnNames = new ArrayList<String>();
			for (List<String> groupedNames : groupedColumnNames)
				allColumnNames.addAll(groupedNames);
			groupedTable.setLabelProvider(new SampleTableLP(allColumnNames));

			
			// The Drag Listener and the Drop Adapter need the Viewer, which 
			// we do not (and should not) have access to at this level. The
			// final argument in each case is an object that returns the class
			// when the method generate(Viewer) is called.
//			groupedTable.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, new LocalDragSupportListener(null));
//			groupedTable.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, new LocalViewerDropAdapter(null));
			
		}
		
		/**
		 * Refresh the table
		 */
		public void refresh() {
			groupedTable.refresh();
		}

		// The table label provider does nothing except delegate to the column label provider
		class SampleTableLP extends LabelProvider implements ITableLabelProvider {

			final List<String> columns;
			
			public SampleTableLP(List <String> columns) {
				this.columns = columns;
			}
			
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return (new SampleTableCLP(columns.get(columnIndex))).getText(element);
			}	
		}		
	}
	
	class MyGroupedTable extends Composite {
		
		private SashForm tableCompo;
		// Only one of each of these per grouped table
		private IStructuredContentProvider cachedContentProvider;
		private Object cachedInput;
		private ITableLabelProvider cachedLabelProvider;
		private ISelectionChangedListener cachedUserSelectionChangedListener;
		private int cachedDragFlags;
		private Transfer[] cachedDragTransfers;
		private DragSourceAdapter cachedDragSourceListener;
		private int cachedDropFlags;
		private Transfer[] cachedDropTransfers;
		private ViewerDropAdapter cachedDropTargetListener;
		
		public MyGroupedTable(Composite parent, int style) {
			super(parent, style);
			// Attach to the left and right edges of the parent, and set to a fixed height
			this.setLayout(new FormLayout());
			
			// Composite to hold the table column groups
			tableCompo = new SashForm(this, SWT.HORIZONTAL);
			FormData formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(0);
			formData.bottom = new FormAttachment(100);
			tableCompo.setLayoutData(formData);
			// To properly fake a table, set the sash width to 0
			tableCompo.setSashWidth(0);
			
			// null the cached table classes
			cachedContentProvider = null;
			cachedInput = null;
			cachedLabelProvider = null;
			cachedUserSelectionChangedListener = null;
			cachedDragTransfers = null;
			cachedDragSourceListener = null;
			cachedDropTransfers = null;
			cachedDropTargetListener = null;
		}			
	
		public void createColumnGroups() {
			createColumnGroups(tableCompo);
			
		}
		
		// Create the column groupings
		private void createColumnGroups(Composite outerComposite) {

			// TableViewers for each sub-table
			groupViewers = new ArrayList<TableViewer>();
			groupNames = new ArrayList<String>();

			// Column weights, and column group weights
			List<Integer> groupWeights = new ArrayList<Integer>();

			// Iterate over the groups.
			for (int i = 0; i < groupNamesST.size(); i++) {

				String groupName = groupNamesST.get(i);
				groupNames.add(i, groupName);
				
				TableViewer tV = createColumnGroupInternal(outerComposite, groupNames.get(i));
				
				// Composite to hold the group header and table
				groupViewers.add(tV);

				// Create the columns of the sub-table, according to the definition of the group and the overall weights 
				createColumns(tV, groupedColumnNames.get(i), groupedColumnWeights.get(i));

				// Set the listener that sets the selection as the same on each sub-table
				tV.addSelectionChangedListener(new SubTableSelectionChangedListener());

				// Drag and drop support
//				tV.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, new LocalDragSupportListener(tV));
//				tV.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[]{LocalSelectionTransfer.getTransfer()}, new LocalViewerDropAdapter(tV));

				// Calculate the relative weighting of each group
				int groupWeight = 0;
				for (int j = 0; j < groupedColumnWeights.get(i).size(); j++) groupWeight += groupedColumnWeights.get(i).get(j);
				groupWeights.add(i, groupWeight);
				
			}

			// Set the weights of the SashForm, if the parent Composite is one
			if (outerComposite instanceof SashForm) {
				
				int[] sashWeights = new int[groupWeights.size()];
				for (int iWeight = 0; iWeight < groupWeights.size(); iWeight++) sashWeights[iWeight] = groupWeights.get(iWeight);
				
				((SashForm) outerComposite).setWeights(sashWeights);
			}

		}

		/**
		 * Refreshes all sub-tables.
		 */
		// refresh the entire table
		public void refresh() {
			for (TableViewer tV : groupViewers)
				tV.refresh();
		}

		/**
		 * Creates a new column group.
		 * <p>
		 * Adds a new named or anonymous column group to the end of the list of
		 * groups. Repeated named groups are not allowed, but an new anonymous
		 * group can be added to the end of the list at any point.
		 * @param groupNameIn
		 * 					the name to give the group of columns
		 */
		public void createColumnGroup(String groupNameIn) {
			String groupName  = (groupNameIn != null) ? groupNameIn : "";
			// Do not add duplicate named groups
			if (!groupName.equals("") && groupNames.contains(groupName)) return;
			// Otherwise, add a group with this name to the end of the list of groups
			groupNames.add(groupName);
			groupViewers.add(createColumnGroupInternal(tableCompo, groupName));
		}
		
		
		private TableViewer createColumnGroupInternal(Composite parent, String groupName) {
			Composite groupCompo = new Composite(parent, SWT.NONE);		
			groupCompo.setLayout(new FormLayout());

			// Add the Group column header as a do-nothing button
			Button headerButton = new Button(groupCompo, SWT.PUSH);

			FormData formData = new FormData();
			formData.top = new FormAttachment(0, 0);
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			headerButton.setLayoutData(formData);

			headerButton.setText(groupName);

			// Add the table that will hold this subset of the columns
			Composite subTableCompo = new Composite(groupCompo, SWT.NONE);

			formData = new FormData();
			formData.top = new FormAttachment(headerButton);
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			formData.bottom = new FormAttachment(100, 0);
			subTableCompo.setLayoutData(formData);

			// Define the sub-table
			TableViewer tV = new TableViewer(subTableCompo);

			// Style of each sub-table
			tV.getTable().setHeaderVisible(true);
			tV.getTable().setLinesVisible(true);

			// Column Layout
			tV.getTable().getParent().setLayout(new TableColumnLayout());
			
			// Previously set cached classes
			if (cachedContentProvider != null) {
				tV.setContentProvider(cachedContentProvider);
				if (cachedInput != null)
					tV.setInput(cachedInput);
			}
			
			tV.setLabelProvider(new SubTableLabelProvider(tV));

			tV.addSelectionChangedListener(new SubTableSelectionChangedListener());
			
			return tV;
		}

		public void setContentProvider(IStructuredContentProvider iSCP) {
			cachedContentProvider = iSCP;
			for (TableViewer tV : groupViewers)
				tV.setContentProvider(iSCP);
			if (cachedInput != null)
				for (TableViewer tV : groupViewers)
					tV.setInput(cachedInput);
		}
		
		public void setInput(Object input) {
			cachedInput = input;
			if (cachedContentProvider != null)
				for (TableViewer tV : groupViewers)
					tV.setInput(cachedInput);
		}
		
		public void setLabelProvider(ITableLabelProvider iTLP) {
			cachedLabelProvider = iTLP;
		}
		
		public void addSelectionChangedListener(ISelectionChangedListener iSCL) {
			cachedUserSelectionChangedListener = iSCL;
		}
		
		public void addDragSupport(int dragFlags, Transfer[] transfers, DragSourceAdapter dSL) {
			cachedDragFlags = dragFlags;
			cachedDragTransfers = transfers;
			cachedDragSourceListener = dSL;
			for (TableViewer tV : groupViewers)
				try {
					tV.addDragSupport(cachedDragFlags, cachedDragTransfers, cachedDragSourceListener.getClass().newInstance());
				} catch (Exception e) {
					// If there are any exceptions, log it and carry on; drag and drop is not critical
					// TODO: Real logger, not stderr
					System.err.println("Exception setting drag and drop support in " + this.getClass() + ":" + e);
				}
		}
		
		public void addDropSupport(int dropFlags, Transfer[] transfers, ViewerDropAdapter dTL) {
			cachedDropFlags = dropFlags;
			cachedDropTransfers = transfers;
			cachedDropTargetListener = dTL;
			for (TableViewer tV : groupViewers)
				try {
					tV.addDropSupport(cachedDropFlags, cachedDropTransfers, cachedDropTargetListener.getClass().getDeclaredConstructor(Viewer.class).newInstance(tV));
				} catch (Exception e) {
					// If there are any exceptions, log it and carry on; drag and drop is not critical
					// TODO: Real logger, not stderr
					System.err.println("Exception setting drag and drop support in " + this.getClass() + ":" + e);
				}
		}
		
		/**
		 * Changes the selection on all sub-tables.
		 * <p>
		 * When the selection changes, change the selection on the other
		 * sub-tables.
		 *
		 */
		// By using a object-wide flag we can prevent each call triggering four
		// more and causing a stack overflow.
		class SubTableSelectionChangedListener implements ISelectionChangedListener {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					if (propagateSelectionChange) {
						// Oh, the race conditions we shall have.
						// Tell the tables not to further propagate the selection
						// change when we programmatically change the selection
						propagateSelectionChange = false;

						// Fire off the user defined SelectionChangedListener once
						if (cachedUserSelectionChangedListener != null)
							cachedUserSelectionChangedListener.selectionChanged(event);
						
						for (TableViewer tV : groupViewers)
							tV.setSelection(selection, true);
					
						propagateSelectionChange= true;
					}
				}
			}
		}

		class SubTableLabelProvider extends LabelProvider implements ITableLabelProvider {

			TableViewer tV;
			
			public SubTableLabelProvider(TableViewer tV) {
				this.tV = tV;
			}
			
			private int getOffset() {
				int columnCount = 0;
				if (groupViewers != null && groupViewers.size() > 1) {
					for (int iTV = 0; iTV < groupViewers.size()-1 && groupViewers.get(iTV) != tV; iTV++) {
						columnCount += groupViewers.get(iTV).getTable().getColumnCount();
					}
				}
				return columnCount;
			}
			
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return (cachedLabelProvider != null) ? 
						cachedLabelProvider.getColumnImage(element, columnIndex+getOffset()) :
						null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return (cachedLabelProvider != null) ?
						cachedLabelProvider.getColumnText(element, columnIndex+getOffset()) :
						null;
			}
	
		}
	}

	// drag support for local moves. Copy data
	class LocalDragSupportListener extends DragSourceAdapter {
		private TableViewer tV;
		public LocalDragSupportListener(Viewer tV) {
			this.tV = (tV instanceof TableViewer) ? (TableViewer) tV : null;
		}
		
		@Override
		public void dragSetData(DragSourceEvent event) {
			LocalSelectionTransfer.getTransfer().setSelection(tV.getSelection());
		}
	}
	
	// Deals with both dragging and copy-dragging
	class LocalViewerDropAdapter extends ViewerDropAdapter {

		public LocalViewerDropAdapter(Viewer tV) {
			super(tV);
		}

		@Override
		public boolean performDrop(Object data) {
			XPDFSampleParameters targetEntry = (XPDFSampleParameters) getCurrentTarget();
			// Create the List of new sample parameters to be dropped in.
			List<XPDFSampleParameters> samplesToAdd = new ArrayList<XPDFSampleParameters>(((IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection()).size());
			for (Object oSample: ((IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection()).toList()) {
				XPDFSampleParameters sampleToAdd;
				if (getCurrentOperation() == DND.DROP_COPY) {
					sampleToAdd = new XPDFSampleParameters((XPDFSampleParameters) oSample);
					sampleToAdd.setId(generateUniqueID());
				} else {
					sampleToAdd = (XPDFSampleParameters) oSample;
				}
				samplesToAdd.add(sampleToAdd);
			}

			int targetIndex;
			// Deal with removing the originals when moving and get the index to insert the dragees before
			if (getCurrentOperation() == DND.DROP_MOVE) {
				// Remove the originals, except the target if it is in the dragged set
				List<XPDFSampleParameters> samplesToRemove = new ArrayList<XPDFSampleParameters>(samplesToAdd);
				boolean moveInitialTarget = samplesToAdd.contains(targetEntry);
				if (moveInitialTarget)
					samplesToRemove.remove(targetEntry);
				samples.removeAll(samplesToRemove);

				// Get the index before which to insert the moved data
				targetIndex = getDropTargetIndex(targetEntry, getCurrentLocation(), moveInitialTarget);
				if (targetIndex == -1) return false;

				if (moveInitialTarget)
					samples.remove(targetEntry);
			} else {
				// Get the index before which to insert the moved data
				targetIndex = getDropTargetIndex(targetEntry, getCurrentLocation());
				if (targetIndex == -1) return false;
			}				
			boolean success = samples.addAll(targetIndex, samplesToAdd);
			sampleTable.refresh();
			
			return success;
		}

		@Override
		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			// Fine, whatever. Just don't try anything funny.
			// TODO: real validation.
			return true;
		}
		
	}

	private int getDropTargetIndex(XPDFSampleParameters targetEntry, int currentLocation) {
		return getDropTargetIndex(targetEntry, currentLocation, false);
	}
	
	private int getDropTargetIndex(XPDFSampleParameters targetEntry, int currentLocation, boolean isTargetRemoved) {
		// If they are identical, there is only one dragee, and the location is ON, then do nothing.
		if (((IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection()).size() == 1 &&
				targetEntry == ((XPDFSampleParameters) ((IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection()).getFirstElement()) &&
				currentLocation == ViewerDropAdapter.LOCATION_ON)
			return -1;
		
		// Otherwise, copy all the data dragged.
		int targetIndex;
		if (targetEntry != null) {
			targetIndex = samples.indexOf(targetEntry);

			switch (currentLocation) {
			case ViewerDropAdapter.LOCATION_BEFORE:
			case ViewerDropAdapter.LOCATION_ON:
				break;
			case ViewerDropAdapter.LOCATION_AFTER:
				if (!isTargetRemoved) targetIndex++;
				break;
			case ViewerDropAdapter.LOCATION_NONE:
				return -1;
			default: return -1;

			}
		} else {
			targetIndex = samples.size();
		}
		return targetIndex;
	}
	
	
	// Create a sub-set of the columns of the table
	private void createColumns(TableViewer tV, List<String> columns, List<Integer> columnWeights) {
		TableColumnLayout tCL = (TableColumnLayout) tV.getTable().getParent().getLayout();
		TableViewerColumn col;
//		String[] columnNames = {"Sample name", "Code", "", "Phases", "Composition", "Density", "Vol. frac.", "Energy", "μ", "Max capillary ID", "Energy", "Container"}; 
		for (int i = 0; i < columns.size(); i++) {
			col = new TableViewerColumn(tV, SWT.NONE);
			col.getColumn().setText(columns.get(i));
			tCL.setColumnData(col.getColumn(), new ColumnWeightData(columnWeights.get(i), 10, true));
			col.setLabelProvider(new SampleTableCLP(columns.get(i)));
			col.setEditingSupport(new SampleTableCES(columns.get(i), tV));
			col.getColumn().addSelectionListener(getColumnSelectionAdapter(col.getColumn(), columns.get(i)));
		}
	}

	// The table label provider does nothing except delegate to the column label provider
	class SampleTableLP extends LabelProvider implements ITableLabelProvider {

		final List<String> columns;
		
		public SampleTableLP(List <String> columns) {
			this.columns = columns;
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return (new SampleTableCLP(columns.get(columnIndex))).getText(element);
		}
		
	}

	class SampleTableHackLP extends LabelProvider implements ITableLabelProvider {
		List<String> columns;

		public SampleTableHackLP() {
			columns = Arrays.asList(new String[] {"Sample name", "Code", "", "Phases", "Composition", "Density", "Vol. frac.", "Energy", "μ", "Max capillary ID", "Beam state", "Container"});
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return (new SampleTableCLP(columns.get(columnIndex))).getText(element);
		}
	}
	
	
	// Column label provider. Use a switch to provide the different data labels for the different columns.
	class SampleTableCLP extends ColumnLabelProvider {
		
		final String columnName;
		
		public SampleTableCLP(String columnName) {
			this.columnName = columnName;
		}
		
		@Override
		public String getText(Object element) {
			XPDFSampleParameters sample = (XPDFSampleParameters) element;
			switch (columnName) {
			case "Sample name": return sample.getName();
			case "Code": return Integer.toString(sample.getId());
			case "": return "+";
			case "Phases": 
				StringBuilder sb = new StringBuilder();
				for (String phase : sample.getPhases()) {
					sb.append(phase);
					sb.append(", ");
				}
				if (sb.length() > 2) sb.delete(sb.length()-2, sb.length());
				return sb.toString();
			case "Composition": return sample.getComposition();
			case "Density": return Double.toString(sample.getDensity());
			case "Vol. frac.": return Double.toString(sample.getPackingFraction());
			case "Energy": return Double.toString(sample.getSuggestedEnergy());
			case "μ": return String.format("%.4f", sample.getMu());
			case "Max capillary ID": return Double.toString(sample.getSuggestedCapDiameter());
			case "Beam state": return sample.getBeamState();
			case "Container": return sample.getContainer();
			default: return "";
			}
		}
	}

	// Column editing support. The different cases for the different columns are just switched by a switch statements or if-then-else
	class SampleTableCES extends EditingSupport {

		final String column;
		final TableViewer tV;	

		public SampleTableCES(String column, TableViewer tV) {
			super(tV);
			this.column = column;
			this.tV = tV;
		};
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(tV.getTable());
		}
		@Override
		protected boolean canEdit(Object element) {
			if (column == "Code" || column == "" || column == "μ") 
				return false;
			else
				return true;
		}
		@Override
		protected Object getValue(Object element) {
			XPDFSampleParameters sample = (XPDFSampleParameters) element;
			switch (column) {
			case "Sample name": return sample.getName();
			case "Code": return sample.getId();
			case "": return null; // TODO: This should eventually show something, but nothing for now.
			case "Phases": return (new SampleTableCLP("Phases")).getText(element);
			case "Composition": return sample.getComposition();
			case "Density": return Double.toString(sample.getDensity());
			case "Vol. frac.": return Double.toString(sample.getPackingFraction());
			case "Energy": return Double.toString(sample.getSuggestedEnergy());
			case "μ": return 1.0;
			case "Max capillary ID": return Double.toString(sample.getSuggestedCapDiameter());
			case "Beam state": return sample.getBeamState();
			case "Container": return sample.getContainer();
			default: return null;
			}
		}
		@Override
		protected void setValue(Object element, Object value) {
			XPDFSampleParameters sample = (XPDFSampleParameters) element;
			String sValue = (String) value;
			switch (column) {
			case "Sample name": sample.setName(sValue); break;
			case "Code": break;
			case "": break; // TODO: This should eventually do something. Call a big function, probably.
			case "Phases": { // Parse a comma separated list of phases to a list of Strings
				String[] arrayOfPhases = sValue.split(","); 
				List<String> listOfPhases = new ArrayList<String>();
				for (int i = 0; i < arrayOfPhases.length; i++)
					listOfPhases.add(arrayOfPhases[i].trim());
				sample.setPhases(listOfPhases);
			} break;
			case "Composition": sample.setComposition(sValue); break;
			case "Density": sample.setDensity(Double.parseDouble(sValue)); break;
			case "Vol. frac.": sample.setPackingFraction(Double.parseDouble(sValue)); break;
			case "Energy": sample.setSuggestedEnergy(Double.parseDouble(sValue)); break;
			case "μ": break;
			case "Max capillary ID": sample.setSuggestedCapDiameter(Double.parseDouble(sValue)); break;
			case "Beam state": sample.setBeamState(sValue); break;
			case "Container": sample.setContainer(sValue); break;
			default: break;
			}
			// Here, only this table needs updating
			tV.update(element, null);
		}
	}

	// Set a Comparator, depending on the column selected
	private Comparator<XPDFSampleParameters> getColumnSorting(String column) {
		Comparator<XPDFSampleParameters> columnSorter;
		switch (column) {
		case "Sample name":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
		break;
		case "Code":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Integer.compare(o1.getId(), o2.getId());
			}
		};
		break;
		case "":
		case "Phases":
		case "Composition":
			columnSorter = null;
			break;
		case "Density":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Double.compare(o1.getDensity(), o2.getDensity());
			}
		};
		break;
		case "Vol. frac.":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Double.compare(o1.getPackingFraction(), o2.getPackingFraction());
			}
		};
		break;
		case "Energy":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Double.compare(o1.getSuggestedEnergy(), o2.getSuggestedEnergy());
			}
		};
		break;
		case "μ":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Double.compare(o1.getMu(), o2.getMu());
			}
		};
		break;
		case "Max capillary ID":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return Double.compare(o1.getSuggestedCapDiameter(), o2.getSuggestedCapDiameter());
			}
		};
		break;
		case "Beam state":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return o1.getBeamState().compareTo(o2.getBeamState());
			}
		};
		case "Container":
			columnSorter = new Comparator<XPDFSampleParameters>() {
			@Override
			public int compare(XPDFSampleParameters o1, XPDFSampleParameters o2) {
				return o1.getContainer().compareTo(o2.getContainer());
			}
		};
		default:
			columnSorter = null;
		}
		return columnSorter;
	}

	// Column selection listeners.
	/**
	 * Creates a listener that sorts the data depending on the column selected.
	 * <p>
	 * Clicking on the individual column headers will sort, or reverse the sort on the data in all sub-tables.
	 * @param tableColumn
	 * 					the SWT column object
	 * @param column
	 * 				the enum identifier of the column
	 * @return the new anonymous sub-class of Selection Adapter
	 */
	private SelectionAdapter getColumnSelectionAdapter(final TableColumn tableColumn, final String column) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// If the present column has no defined Comparator, then return
				if (getColumnSorting(column) == null) return;
				
				// Find the present sorted column, if any
				TableColumn presentSorted = null;
				int sortDirection = SWT.NONE;
				for (TableViewer tV : groupViewers) {
					if (tV.getTable().getSortColumn() != null) {
						presentSorted = tV.getTable().getSortColumn();
						sortDirection = tV.getTable().getSortDirection();
					}
					// Set each table to unsorted
					tV.getTable().setSortDirection(SWT.NONE);
					tV.getTable().setSortColumn(null);
				}

				// If the same column is sorted as is now selected, then reverse the sorting
				if (presentSorted == tableColumn)
					sortDirection = (sortDirection == SWT.UP) ? SWT.DOWN : SWT.UP;

				// Do the sort
				if (sortDirection != SWT.NONE) {
					if (getColumnSorting(column) != null) {
						Collections.sort(samples, getColumnSorting(column));
						if (sortDirection == SWT.UP)
							Collections.reverse(samples);
					}
				}
				
				tableColumn.getParent().setSortDirection(sortDirection);
				tableColumn.getParent().setSortColumn(tableColumn);
				
				sampleTable.refresh();
			}
		};
	}
		
	private void createActions() {
		// load the nonsense test data
		loadTestDataAction = new LoadTestDataAction();
		loadTestDataAction.setText("Load test data");
		loadTestDataAction.setToolTipText("Load the test data");
		loadTestDataAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		
		// do nothing but allow a breakpoint hook
		pointBreakAction = new Action() {
			@Override
			public void run() {
				samples.get(0).getDensity();
			}
		};
		pointBreakAction.setToolTipText("Ze goggles, zey...");
		pointBreakAction.setText("...do nothing");
		pointBreakAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
		
		// save the data
		saveAction = new Action() {
			@Override
			public void run() {
				for (XPDFSampleParameters sample : samples)
					System.err.println(sample.toString());
			}
		};
		saveAction.setToolTipText("Save data to stderr");
		saveAction.setText("Save");
		saveAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		
		// add a new entry to the end of the list
		newBlankAction = new Action() {
			@Override
			public void run() {
				XPDFSampleParameters blankSample = new XPDFSampleParameters();
				blankSample.setId(generateUniqueID());
				samples.add(blankSample);
				sampleTable.refresh();
			}
		};
		newBlankAction.setText("New sample");
		newBlankAction.setToolTipText("Add an empty sample to the table");
		newBlankAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
		
		// clear all the data from the list
		clearAction = new Action() {
			@Override
			public void run() {
				samples.clear();
				usedIDs.clear();
				sampleTable.refresh();
			}
		};
		clearAction.setText("Clear");
		clearAction.setToolTipText("Clear all entries");
		clearAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
		
		// delete a selected entry
		deleteAction = new DeleteSampleAction();
		deleteAction.setText("Delete");
		deleteAction.setToolTipText("Delete selected samples");
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
		
		// simulate a PDF
		simPDFAction = new SimulatePDFAction();
		simPDFAction.setText("Simulate");
		simPDFAction.setToolTipText("Simulate a PDF for each of the selected samples");
		
		hookIntoContextMenu();
	}

	private void createRHSButtons(Composite parent) {
		int rightMargin = -10;
		int topMargin = 10;
//		Composite stCompo = compoAbove.getParent();

		simButton = new Button(parent, SWT.NONE);
		FormData formData= new FormData();
		formData.right = new FormAttachment(100, rightMargin);
		formData.top = new FormAttachment(0, topMargin);
		simButton.setLayoutData(formData);
		simButton.setText("Simulate PDF");
		simButton.setToolTipText("Produce a simulated pair distribution function for the selected sample");
		simButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				simPDFAction.run();
			}
		});
		
		savButton = new Button(parent, SWT.NONE);
		formData = new FormData();
		formData.right = new FormAttachment(100, rightMargin);
		formData.top = new FormAttachment(simButton, topMargin);
		formData.bottom = new FormAttachment(100, -topMargin);
		savButton.setLayoutData(formData);
		savButton.setText("Save");
		savButton.setToolTipText("Save the sample data to file (or the database?)");
		savButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				saveAction.run();
			}
		});
	}

	private void createCentreButtons(Composite parent) {
		int offset = 10;
		int bottomMargin = -10;
//		Composite stCompo = compoAbove.getParent();
		
		delButton = new Button(parent, SWT.NONE);
		FormData formData = new FormData();
		formData.left = new FormAttachment(50);
		formData.top = new FormAttachment(0, offset);
		delButton.setLayoutData(formData);
		delButton.setText(deleteAction.getText());
		delButton.setToolTipText(deleteAction.getToolTipText());
		delButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				deleteAction.run();
			}
		});
		
		clrButton = new Button(parent, SWT.NONE);
		formData = new FormData();
		formData.left = new FormAttachment(50);
		formData.top = new FormAttachment(delButton, offset);
		formData.bottom = new FormAttachment(100, bottomMargin);
		clrButton.setLayoutData(formData);
		clrButton.setText(clearAction.getText());
		clrButton.setToolTipText(clearAction.getToolTipText());
		clrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				clearAction.run();
			}
		});
	}
	
	private void createLoadButtons(Composite parent) {
		int leftMargin = 10;
		int topMargin = 10;
//		Composite stCompo = compoAbove.getParent();

		cifButton = new Button(parent, SWT.NONE);
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, leftMargin);
		formData.top = new FormAttachment(0, topMargin);
		cifButton.setLayoutData(formData);
		cifButton.setText("New sample from CIF file");
		cifButton.setToolTipText("Create new sample from the data contained in a specified Crystallographic Information File.");

		eraButton = new Button(parent, SWT.NONE);
		formData = new FormData();
		formData.left = new FormAttachment(0, leftMargin);
		formData.top = new FormAttachment(cifButton, topMargin);
		formData.bottom = new FormAttachment(100, -topMargin);
		eraButton.setLayoutData(formData);
		eraButton.setText("New sample from ERA file");
		eraButton.setToolTipText("Create a new sample from the data contained in a specified ERA file.");
	}

	@Override
	public void setFocus() {
		// Can all the TableViewers have focus?
		for (TableViewer iTV : groupViewers ) {
			iTV.getControl().setFocus();
		}
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
		
		for (TableViewer iTV : groupViewers) {
			Menu popupMenu = menuMan.createContextMenu(iTV.getControl());
			iTV.getControl().setMenu(popupMenu);
			getSite().registerContextMenu(menuMan, iTV);
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(loadTestDataAction);
		manager.add(newBlankAction);
		manager.add(saveAction);
		manager.add(deleteAction);
		manager.add(clearAction);
		manager.add(new Separator("Data"));
		manager.add(simPDFAction);
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

	
	
	// Action class that simulates the pair-distribution function of the given sample.
	// At the moment, this plots the NIST ceria standard PDF
	class SimulatePDFAction extends Action {
		@Override
		public void run() {
			List<XPDFSampleParameters> selectedXPDFParameters = getSelectedSampleParameters();
			if (selectedXPDFParameters.isEmpty()) return;
			// Get the pair-distribution function data for each sample
			List<Dataset> simulatedPDFs = new ArrayList<Dataset>(selectedXPDFParameters.size());
			for (int i = 0; i < selectedXPDFParameters.size(); i++)
				simulatedPDFs.add(i, selectedXPDFParameters.get(i).getSimulatedPDF());
			// Because the XPDF Sample View is not necessarily going to be 
			// used in a Perspective containing a plot view, and because there
			// may be many selected samples, open an new plot window, and plot
			// the data there.
			
		// TODO: Do something with the data
			for (Dataset pdf : simulatedPDFs) {
				XPDFPopUpPlotDialog popUp = new XPDFPopUpPlotDialog(XPDFSampleEditorView.this.getViewSite().getShell(), null, (IDataset) pdf);
				popUp.open();
//				popUp.createDialogArea(XPDFSampleView.this.getViewSite().getShell().getParent());
			}
		}
	}

	private class DeleteSampleAction extends Action {
		@Override
		public void run() {
			List<XPDFSampleParameters> selectedXPDFParameters = getSelectedSampleParameters();
			if (selectedXPDFParameters.isEmpty()) return;
			samples.removeAll(selectedXPDFParameters);
			sampleTable.refresh();
		}
	}

	// get all the selected elements, regardless of the sub-table on which the action is initiated.
	private List<XPDFSampleParameters> getSelectedSampleParameters() {
		List<XPDFSampleParameters> selectedXPDFParameters = new ArrayList<XPDFSampleParameters>();
		// The selection listeners make sure the selection is the same in
		// all tables, so get the selection from the first table.
		ISelection selection = groupViewers.get(0).getSelection();
		// No items? return, having done nothing.
		if (selection.isEmpty()) return selectedXPDFParameters;
		// If it is not an IStructureSelection, then I don't know what to do with it.
		if (!(selection instanceof IStructuredSelection)) return selectedXPDFParameters;
		// Get the list of all selected data
		List<?> selectedData = ((IStructuredSelection) selection).toList();
		for (Object datum : selectedData)
			if (datum instanceof XPDFSampleParameters)
				selectedXPDFParameters.add((XPDFSampleParameters) datum);
		return selectedXPDFParameters;		
	}
	
	
	// Generate a new id
	private int generateUniqueID() {
		final int lowestID = 154;
		if (usedIDs == null)
			usedIDs = new TreeSet<Integer>();
		int theID = (usedIDs.isEmpty()) ? lowestID : usedIDs.last()+1;
		usedIDs.add(theID);
		return theID;
	}
	
	// Sample data with integer multiplicities
	class LoadTestDataAction extends Action {

		@Override
		public void run() {
			if (samples == null)
				samples = new ArrayList<XPDFSampleParameters>();
			
			// barium titanate
			XPDFSampleParameters bto = new XPDFSampleParameters();
			bto.setName("Barium Titanate");
			bto.setId(generateUniqueID());
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
			rutile.setId(generateUniqueID());
			rutile.setPhases(new ArrayList<String>(Arrays.asList(new String[] {"TiO2"})));
			rutile.setComposition("TiO2");
			rutile.setDensity(6.67);
			// Packing fraction as default
			rutile.setSuggestedEnergy(76.6);
			rutile.setSuggestedCapDiameter(5.0);
			rutile.setBeamState("76.6 Hi Flux");
			rutile.setContainer("0.5 mm B");
			
			samples.add(rutile);
			
			// and something else
			XPDFSampleParameters explodite = new XPDFSampleParameters();
			explodite.setName("Explodite");
			explodite.setId(generateUniqueID());
			explodite.setPhases(new ArrayList<String>(Arrays.asList(new String[] {"LF", "O"})));
			explodite.setComposition("K2S4P");
			explodite.setDensity(1.1);
			//packing fraction as default
			explodite.setSuggestedEnergy(76.6);
			explodite.setSuggestedCapDiameter(5.0);
			explodite.setBeamState("76.6 Hi Flux");
			explodite.setContainer("0.5 mm");
			
			samples.add(explodite);
			
			for (TableViewer iTV : groupViewers) {
				iTV.getTable().setSortDirection(SWT.NONE);
				iTV.getTable().setSortColumn(null);
				iTV.refresh();
			}
		}
	}
}
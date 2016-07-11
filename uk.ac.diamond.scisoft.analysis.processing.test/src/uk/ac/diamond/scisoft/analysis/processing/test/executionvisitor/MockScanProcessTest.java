/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.test.executionvisitor;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.ILiveOperationInfo;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.model.EmptyModel;
import org.eclipse.dawnsci.analysis.api.processing.model.SleepModel;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.processing.Activator;
import uk.ac.diamond.scisoft.analysis.processing.operations.DataWrittenOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.SleepOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.roiprofile.BoxMeanOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.roiprofile.BoxModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.twod.DownsampleImageModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.twod.DownsampleImageOperation;
import uk.ac.diamond.scisoft.analysis.processing.runner.OperationRunnerImpl;
import uk.ac.diamond.scisoft.analysis.processing.runner.SeriesRunner;
import uk.ac.diamond.scisoft.analysis.processing.visitor.NexusFileExecutionVisitor;

public class MockScanProcessTest {
	
	private static IOperationService service;
	
	@BeforeClass
	public static void before() throws Exception {
		
		OperationRunnerImpl.setRunner(ExecutionType.SERIES,   new SeriesRunner());
		OperationRunnerImpl.setRunner(ExecutionType.PARALLEL, new SeriesRunner());
		
		ServiceManager.setService(IPersistenceService.class, PersistenceServiceCreator.createPersistenceService());
		NexusFileExecutionVisitor.setPersistenceService(PersistenceServiceCreator.createPersistenceService());
		service = (IOperationService)Activator.getService(IOperationService.class);
		service.createOperations(service.getClass().getClassLoader(), "uk.ac.diamond.scisoft.analysis.processing.test.executionvisitor");
	}
	
	@Test
	public void test() throws Exception {
		int sleep = 200;
		int[] dataShape = {5,10,101,102};
		ExecutorService ste = Executors.newSingleThreadExecutor();
		final File tmp = File.createTempFile("Test", ".h5");
		tmp.deleteOnExit();
		tmp.createNewFile();
		startMockScan(dataShape, sleep,ste,tmp);
		ste.shutdown();
		
		ExecutorService ste2 = Executors.newSingleThreadExecutor();
		final File tmpProc = File.createTempFile("Test", ".h5");
		tmpProc.deleteOnExit();
		tmpProc.createNewFile();
		
		starProcessing(ste2, tmpProc, tmp);
		ste2.shutdown();
		
		while (!ste.awaitTermination(200,TimeUnit.MILLISECONDS) || !ste2.awaitTermination(200,TimeUnit.MILLISECONDS)) {
			//nothing
		}
		
		ste.toString();
		
	}
	
	private void startMockScan(int[] shape, int sleep, ExecutorService ste, File tmp) throws Exception{
		
		
		ILazyDataset lazy = ExecutionVisitorTestUtils.getLazyDataset(shape, 1);
		
		final IOperationContext context = service.createContext();
		context.setData(lazy);
		context.setDataDimensions(new int[]{2,3});
		
		SleepOperation ops = new SleepOperation();
		ops.setModel(new SleepModel());
		ops.getModel().setMilliseconds(sleep);
		DataWrittenOperation odo = new DataWrittenOperation();
		odo.setModel(new EmptyModel());

		//FIXME or rather fix swmr. Not currently testing swmr since wont read from a 
		//different thread to writing thread
		context.setVisitor(new NexusFileExecutionVisitor(tmp.getAbsolutePath(),true));
		context.setSeries(ops,odo);
		context.setExecutionType(ExecutionType.SERIES);
		
		ste.submit(new Runnable() {
			
			@Override
			public void run() {
				service.execute(context);
				
			}
		});
		
		
	}
	
	private void starProcessing(ExecutorService ste, File tmpProc, File tmp) throws Exception{
		
		String data = "/entry/result/data";
		String key = "/entry/auxiliary/1-DataWritten/key/data";
		String ax0 = "/entry/result/Axis_0";
		String ax1 = "/entry/result/Axis_1";
		String ax2 = "/entry/result/Axis_2";
		String ax3 = "/entry/result/Axis_3";
		
		
		final IOperationContext context = service.createContext();
		
		IDataHolder dh = null;
		int count = 0;
		while (count < 100 &&  (dh == null || !dh.contains("/entry/result/data"))){
			Thread.sleep(100);
			count++;
			dh = LoaderFactory.getData(tmp.getAbsolutePath());
		}
		
		if (count == 100) Assert.fail("Couldnt read file!");
		
		final IDataHolder fdh = dh;
		
		ILazyDataset lz = dh.getLazyDataset(data);
		
		AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 4);
		ax.addAxis(0, dh.getLazyDataset(ax0));
		ax.addAxis(1, dh.getLazyDataset(ax1));
		ax.addAxis(2, dh.getLazyDataset(ax2));
		ax.addAxis(3, dh.getLazyDataset(ax3));
		lz.addMetadata(ax);
		
		context.setData(lz);
		context.setDataDimensions(new int[]{2,3});
		context.setLiveInfo(new ILiveOperationInfo() {
			
			@Override
			public IDynamicDataset[] getKeys() {
				ILazyDataset lazyDataset = fdh.getLazyDataset("/entry/auxiliary/1-DataWritten/key/data");
				return new IDynamicDataset[]{(IDynamicDataset)lazyDataset};
			}
			
			@Override
			public IDynamicDataset getComplete() {
				return (IDynamicDataset)fdh.getLazyDataset("/entry/live/finished");
			}
		});
		
		BoxMeanOperation bmo = new BoxMeanOperation();
		BoxModel bmm = new BoxModel();
		bmm.setBox(new RectangularROI(10,10, 10, 10, 0));
		bmo.setModel(bmm);
		
		DownsampleImageOperation dso = new DownsampleImageOperation();
		DownsampleImageModel dsm = new DownsampleImageModel();
		dsm.setDownsampleSizeY(10);
		dsm.setDownsampleSizeX(10);
		dso.setModel(dsm);

		//FIXME or rather fix swmr. Not currently testing swmr since wont read from a 
		//different thread to writing thread
		context.setVisitor(new NexusFileExecutionVisitor(tmpProc.getAbsolutePath(),true));
		context.setSeries(bmo,dso);
		context.setExecutionType(ExecutionType.SERIES);
		
		ste.submit(new Runnable() {
			
			@Override
			public void run() {
				service.execute(context);
				
			}
		});
		
		
	}
}

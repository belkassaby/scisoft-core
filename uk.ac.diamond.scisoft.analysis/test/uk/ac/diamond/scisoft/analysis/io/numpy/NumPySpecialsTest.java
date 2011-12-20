/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.io.numpy;

import gda.analysis.io.ScanFileHolderException;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.PythonHelper;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.NumPyFileSaver;

/**
 * This tests special cases not covered by the other tests which simply sweeps data types
 */
public class NumPySpecialsTest {

	@Test(expected = ScanFileHolderException.class)
	public void testExceptionOnRankOver255() throws Exception {
		int[] shape = new int[500];
		for (int i = 0; i < shape.length; i++) {
			shape[i] = 1;
		}
		AbstractDataset ds = AbstractDataset.ones(shape, AbstractDataset.FLOAT64);
		NumPyTest.saveNumPyFile(ds, NumPyTest.getTempFile());
	}

	@Test(expected = ScanFileHolderException.class)
	public void testExceptionOnEmptyFile() throws Exception {
		AbstractDataset ds = AbstractDataset.ones(new int[] { 2, 3 }, AbstractDataset.FLOAT64);
		final DataHolder dh = new DataHolder();
		dh.addDataset("", ds);
		new NumPyFileSaver("").saveFile(dh);
	}

	@Test(expected = ScanFileHolderException.class)
	public void testExceptionOnNullFile() throws Exception {
		AbstractDataset ds = AbstractDataset.ones(new int[] { 2, 3 }, AbstractDataset.FLOAT64);
		final DataHolder dh = new DataHolder();
		dh.addDataset("", ds);
		new NumPyFileSaver(null).saveFile(dh);
	}

	@Test
	public void testSaveMultipleFiles() throws Exception {
		AbstractDataset ds1 = AbstractDataset.zeros(new int[] { 20 }, AbstractDataset.FLOAT64);
		AbstractDataset ds2 = AbstractDataset.ones(new int[] { 20 }, AbstractDataset.FLOAT64);
		final DataHolder dh = new DataHolder();
		dh.addDataset("", ds1);
		dh.addDataset("", ds2);

		// Determine file names for each of the two data sets
		File fileName = NumPyTest.getTempFile();
		String fileString = fileName.toString();
		String baseName = fileString.substring(0, fileString.length() - ".npy".length());
		File file1 = new File(baseName + "00001.npy");
		File file2 = new File(baseName + "00002.npy");

		// Make sure that the files we should be creating don't already exist
		Assert.assertFalse(file1.exists());
		Assert.assertFalse(file2.exists());

		new NumPyFileSaver(fileString).saveFile(dh);

		// Make sure they do exist now and schedule them for deletion
		Assert.assertTrue(file1.exists() && file1.canRead());
		Assert.assertTrue(file2.exists() && file2.canRead());
		file1.deleteOnExit();
		file2.deleteOnExit();

		// Check the files load correctly in python
		verifySave(0, file1);
		verifySave(1, file2);
	}

	private void verifySave(int value, File file1) throws Exception {
		StringBuilder script1 = new StringBuilder();
		script1.append("import numpy; ");
		script1.append("exp=numpy.array([" + value + "]*20, dtype=numpy.float64); ");
		script1.append("act=numpy.load(r'" + file1.toString() + "');");
		script1.append(NumPyTest.PYTHON_NUMPY_PRINT_MATCHES);
		String pythonStdout1 = PythonHelper.runPythonScript(script1.toString(), false);
		Assert.assertTrue(Boolean.parseBoolean(pythonStdout1.trim()));
	}

}

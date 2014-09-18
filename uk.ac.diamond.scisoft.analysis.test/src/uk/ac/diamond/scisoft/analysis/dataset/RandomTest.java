/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.dataset;

import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.junit.Test;

/**
 * Basic statistical tests of Random class
 */
public class RandomTest {

	private final static String msg = "This test is statistical in nature and can fail. Try re-running.";

	/**
	 * Uniform distribution
	 */
	@Test
	public void TestRand() {
		DoubleDataset ta = Random.rand(10000);

		assertEquals(msg, 0.5, ((Number) ta.mean()).doubleValue(), 4e-2);
		assertEquals(msg, 0.5/Math.sqrt(3), ta.stdDeviation().doubleValue(), 4e-2);
	}

	/**
	 * Seeding
	 */
	@Test
	public void TestSeed() {
		Random.seed(103);
		DoubleDataset ta = Random.rand(100);
		Random.seed(103);
		DoubleDataset tb = Random.rand(100);

		for (int i = 0; i < 100; i++) {
			assertEquals(ta.get(i), tb.get(i), 1e-6);
		}
	}


	/**
	 * Gaussian distribution
	 */
	@Test
	public void TestRandn() {
		Random.seed(103);
		DoubleDataset ta = Random.randn(10000);

		assertEquals(msg, 0., ((Number) ta.mean()).doubleValue(), 4e-2);
		assertEquals(msg, 1., ta.stdDeviation().doubleValue(), 4e-2);
	}

	/**
	 * Discrete uniform distribution
	 */
	@Test
	public void TestRandInt() {
		Random.seed(107);
		IntegerDataset ta = Random.random_integers(13, 27, new int[] {10000});

		assertEquals(msg, (27+13)/2., ((Number) ta.mean()).doubleValue(), 7e-2);
		assertEquals(msg, Math.sqrt(((27-13+1)*(27-13+1) -1)/12.), ta.stdDeviation().doubleValue(), 7e-2);
		assertEquals(msg, 13, ta.min().doubleValue(), 0);
		assertEquals(msg, 27, ta.max().doubleValue(), 0);

		ta = Random.randint(2, 8, new int[] {10000});
		assertEquals(msg, 2, ta.min().doubleValue(), 0);
		assertEquals(msg, 7, ta.max().doubleValue(), 0);
	}

	/**
	 * Exponential distribution
	 */
	@Test
	public void TestExp() {
		Random.seed(103);
		DoubleDataset ta = Random.exponential(2.3, new int[] {10000});

		assertEquals(msg, 2.3, ((Number) ta.mean()).doubleValue(), 2.3*5e-2);
		assertEquals(msg, 2.3, ta.stdDeviation().doubleValue(), 2.3*5e-2);
	}

	/**
	 * Poisson distribution
	 */
	@Test
	public void TestPoi() {
		Random.seed(103);
		IntegerDataset ta = Random.poisson(2.3, new int[] {10000});

		assertEquals(msg, 2.3, ((Number) ta.mean()).doubleValue(), 2.3*5e-2);
		assertEquals(msg, Math.sqrt(2.3), ta.stdDeviation().doubleValue(), 2.3*5e-2);
	}
}

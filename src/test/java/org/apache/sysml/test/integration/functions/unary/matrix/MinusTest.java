/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.test.integration.functions.unary.matrix;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.sysml.api.DMLScript.RUNTIME_PLATFORM;
import org.apache.sysml.lops.LopProperties.ExecType;
import org.apache.sysml.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.integration.TestConfiguration;
import org.apache.sysml.test.utils.TestUtils;

public class MinusTest extends AutomatedTestBase 
{
	private final static String TEST_NAME1 = "Minus";
	private final static String TEST_DIR = "functions/unary/matrix/";
	private static final String TEST_CLASS_DIR = TEST_DIR + MinusTest.class.getSimpleName() + "/";

	private final static int rows = 1501;
	private final static int cols = 2502;
	
	private final static double sparsityDense = 0.7;
	private final static double sparsitySparse = 0.07;
	
	@Override
	public void setUp() 
	{
		TestUtils.clearAssertionInformation();
		
		addTestConfiguration(TEST_NAME1, 
			new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[] { "Y" }) ); 

		if (TEST_CACHE_ENABLED) {
			setOutAndExpectedDeletionDisabled(true);
		}
	}

	@BeforeClass
	public static void init()
	{
		TestUtils.clearDirectory(TEST_DATA_DIR + TEST_CLASS_DIR);
	}

	@AfterClass
	public static void cleanUp()
	{
		if (TEST_CACHE_ENABLED) {
			TestUtils.clearDirectory(TEST_DATA_DIR + TEST_CLASS_DIR);
		}
	}
	
	@Test
	public void testMinusDenseCP() 
	{
		runTestMinus( false, ExecType.CP );
	}
	
	@Test
	public void testMinusSparseCP() 
	{
		runTestMinus( true, ExecType.CP );
	}
	
	@Test
	public void testMinusDenseSP() 
	{
		if(rtplatform == RUNTIME_PLATFORM.SPARK)
		runTestMinus( false, ExecType.SPARK );
	}
	
	@Test
	public void testMinusSparseSP() 
	{
		if(rtplatform == RUNTIME_PLATFORM.SPARK)
		runTestMinus( true, ExecType.SPARK );
	}
	
	@Test
	public void testMinusDenseMR() 
	{
		runTestMinus( false, ExecType.MR );
	}
	
	@Test
	public void testMinusSparseMR() 
	{
		runTestMinus( true, ExecType.MR );
	}
		
	
	private void runTestMinus( boolean sparse, ExecType et )
	{		
		//handle rows and cols
		RUNTIME_PLATFORM platformOld = rtplatform;
		if(et == ExecType.SPARK) {
	    	rtplatform = RUNTIME_PLATFORM.SPARK;
	    }
		else {
	    	rtplatform = (et==ExecType.MR)? RUNTIME_PLATFORM.HADOOP : RUNTIME_PLATFORM.SINGLE_NODE;
	    }
	
		try
		{
			//register test configuration
			TestConfiguration config = getTestConfiguration(TEST_NAME1);
			config.addVariable("rows", rows);
			config.addVariable("cols", cols);
			
			String TEST_CACHE_DIR = "";
			if (TEST_CACHE_ENABLED)
			{
				TEST_CACHE_DIR = sparse + "/";
			}
			
			loadTestConfiguration(config, TEST_CACHE_DIR);
			
			/* This is for running the junit test the new way, i.e., construct the arguments directly */
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + TEST_NAME1 + ".dml";
			programArgs = new String[]{"-args", 
				input("X"), String.valueOf(rows), String.valueOf(cols), output("Y") };
			
			fullRScriptName = HOME + TEST_NAME1 + ".R";
			rCmd = "Rscript" + " " + fullRScriptName + " " + inputDir() + " " + expectedDir();
			
			double sparsity = sparse ? sparsitySparse : sparsityDense;
			double[][] X = getRandomMatrix(rows, cols, 0, 1, sparsity, 7);
			writeInputMatrix("X", X, true);
	
			runTest(true, false, null, -1);
			runRScript(true);
			
			//compare matrices 
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS("Y");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromFS("Y");
			TestUtils.compareMatrices(dmlfile, rfile, 1e-12, "Stat-DML", "Stat-R");
		}
		finally
		{
			//reset platform for additional tests
			rtplatform = platformOld;
		}
	}
}

/*
 *  Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.runner;

import com.technophobia.substeps.runner.JunitFeatureRunner.SubStepsConfiguration;
import com.technophobia.substeps.stepimplementations.MockStepImplementations;
import org.junit.runner.RunWith;

/**
 * @author imoore
 */

// @Ignore("this is only for local dev, not to actually run as a test!")
@SubStepsConfiguration(featureFile = "./target/core-tests/features/allFeatures.feature", subStepsFile = "./target/core-tests/substeps/allFeatures.substeps", stepImplementations = {MockStepImplementations.class})
@RunWith(JunitFeatureRunner.class)
public class AllFeaturesJunit {

}

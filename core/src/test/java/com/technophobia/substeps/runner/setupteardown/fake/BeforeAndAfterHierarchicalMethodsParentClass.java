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
package com.technophobia.substeps.runner.setupteardown.fake;

import com.technophobia.substeps.runner.setupteardown.Annotations.*;

import java.util.ArrayList;
import java.util.List;


public class BeforeAndAfterHierarchicalMethodsParentClass {

    public static List<String> isBeforeAllFeaturesHierarchyExecuted = new ArrayList<String>();
    public static List<String> isBeforeEveryFeatureHierarchyExecuted = new ArrayList<String>();
    public static List<String> isBeforeEveryScenarioHierarchyExecuted = new ArrayList<String>();

    public static List<String> isAfterEveryScenarioHierarchyExecuted = new ArrayList<String>();
    public static List<String> isAfterEveryFeatureHierarchyExecuted = new ArrayList<String>();
    public static List<String> isAfterAllFeaturesHierarchyExecuted = new ArrayList<String>();


    @BeforeAllFeatures
    public void beforeAllFeaturesParent() {
        isBeforeAllFeaturesHierarchyExecuted.add("Parent");
    }


    @BeforeEveryFeature
    public void beforeEveryFeaturesParent() {
        isBeforeEveryFeatureHierarchyExecuted.add("Parent");
    }


    @BeforeEveryScenario
    public void beforeEveryScenarioParent() {
        isBeforeEveryScenarioHierarchyExecuted.add("Parent");
    }


    @AfterEveryScenario
    public void afterEveryScenarioParent() {
        isAfterEveryScenarioHierarchyExecuted.add("Parent");
    }


    @AfterEveryFeature
    public void afterEveryFeaturesParent() {
        isAfterEveryFeatureHierarchyExecuted.add("Parent");
    }


    @AfterAllFeatures
    public void afterAllFeaturesParent() {
        isAfterAllFeaturesHierarchyExecuted.add("Parent");
    }
}

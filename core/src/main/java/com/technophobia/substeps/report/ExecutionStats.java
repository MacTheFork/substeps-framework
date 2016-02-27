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
package com.technophobia.substeps.report;

import com.google.common.collect.Sets;
import com.technophobia.substeps.execution.AbstractExecutionNodeVisitor;
import com.technophobia.substeps.execution.node.*;

import java.io.Serializable;
import java.util.*;

/**
 * @author ian
 */
public class ExecutionStats extends AbstractExecutionNodeVisitor<Void> {

    private final TestCounterSet totals = new TestCounterSet();

    private final Map<String, TestCounterSet> taggedStats = new HashMap<String, TestCounterSet>();

    private List<TestCounterSet> sortedList = null;

    public void buildStats(final ReportData data) {

        for (RootNode rootNode : data.getRootNodes()) {

            buildStatsForRootNode(rootNode);
        }
    }

    private void buildStatsForRootNode(RootNode rootNode) {

        for (FeatureNode featureNode : rootNode.getChildren()) {

            buildStatsForFeatureNode(featureNode);

            for (ScenarioNode<?> scenarioNode : featureNode.getChildren()) {

                buildStatsForScenarioNode(scenarioNode);
            }
        }
    }

    private void buildStatsForScenarioNode(ScenarioNode<?> scenarioNode) {
        for (TestCounterSet stats : getAllStatsForTags(scenarioNode.getTags())) {
            stats.getScenarioStats().apply(scenarioNode);
        }
        scenarioNode.dispatch(this);
    }

    private void buildStatsForFeatureNode(FeatureNode featureNode) {

        for (TestCounterSet stats : getAllStatsForTags(featureNode.getTags())) {
            stats.getFeatureStats().apply(featureNode);
        }
    }

    @Override
    public Void visit(BasicScenarioNode scenarioNode) {

        Set<TestCounterSet> testStats = getAllStatsForTags(scenarioNode.getTags());

        for (StepNode childNode : scenarioNode.getChildren()) {

            for (TestCounterSet stats : testStats) {

                stats.getScenarioStepStats().apply(childNode);
            }
        }

        return null;
    }

    @Override
    public Void visit(OutlineScenarioNode outlineScenarioNode) {

        for (OutlineScenarioRowNode row : outlineScenarioNode.getChildren()) {

            visit(row.getBasicScenarioNode());
        }

        return null;
    }

    private Set<TestCounterSet> getAllStatsForTags(Set<String> tags) {

        Set<TestCounterSet> testStats = Sets.newHashSetWithExpectedSize(tags.size() + 1);
        testStats.add(totals);
        for (String tag : tags) {
            testStats.add(getOrCreateStatsForTag(tag));
        }
        return testStats;
    }

    private TestCounterSet getOrCreateStatsForTag(String tag) {
        if (taggedStats.get(tag) == null) {
            TestCounterSet newStats = new TestCounterSet();
            newStats.setTag(tag);
            taggedStats.put(tag, newStats);
        }

        return taggedStats.get(tag);
    }

    public int getTotalFeatures() {
        return totals.getFeatureStats().getCount();
    }

    public int getTotalFeaturesRun() {
        return totals.getFeatureStats().getRun();
    }

    public int getTotalFeaturesPassed() {
        return totals.getFeatureStats().getPassed();
    }

    public int getTotalFeaturesFailed() {
        return totals.getFeatureStats().getFailed();
    }

    public int getTotalFeaturesSkipped() {
        return totals.getFeatureStats().getIgnored();
    }

    public double getTotalFeaturesSuccess() {

        return totals.getFeatureStats().getSuccessPc();
    }

    public double getTotalFeaturesFailedPC() {

        return 100 - totals.getFeatureStats().getSuccessPc();
    }

    public int getTotalScenarios() {
        return totals.getScenarioStats().getCount();
    }

    public int getTotalScenariosRun() {
        return totals.getScenarioStats().getRun();
    }

    public int getTotalScenariosPassed() {
        return totals.getScenarioStats().getPassed();
    }

    public int getTotalScenariosFailed() {
        return totals.getScenarioStats().getFailed();
    }

    public int getTotalScenariosSkipped() {
        return totals.getScenarioStats().getIgnored();
    }

    public double getTotalScenariosSuccess() {

        return totals.getScenarioStats().getSuccessPc();
    }

    public int getTotalScenarioSteps() {
        return totals.getScenarioStepStats().getCount();
    }

    public int getTotalScenarioStepsRun() {
        return totals.getScenarioStepStats().getRun();
    }

    public int getTotalScenarioStepsPassed() {
        return totals.getScenarioStepStats().getPassed();
    }

    public int getTotalScenarioStepsFailed() {
        return totals.getScenarioStepStats().getFailed();
    }

    public int getTotalScenarioStepsSkipped() {
        return totals.getScenarioStepStats().getIgnored();
    }

    public double getTotalScenarioStepsSuccess() {

        return totals.getScenarioStepStats().getSuccessPc();

    }

    private static class TestStatSetComparator implements Comparator<TestCounterSet>, Serializable {

        private static final long serialVersionUID = -1736428075471005357L;

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final TestCounterSet t1, final TestCounterSet t2) {
            // not sure which way around this is!!
            return t1.getScenarioStepStats().getFailed() - t2.getScenarioStepStats().getFailed();
        }

    }

    /**
     * @return the sortedList
     */
    public List<TestCounterSet> getSortedList() {

        if (taggedStats != null) {

            sortedList = new ArrayList<TestCounterSet>();
            sortedList.addAll(taggedStats.values());

            if (taggedStats.size() > 1) {
                Collections.sort(sortedList, new TestStatSetComparator());
            }
        }

        return sortedList;
    }
}

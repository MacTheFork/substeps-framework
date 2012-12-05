/*
 *	Copyright Technophobia Ltd 2012
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNode;
import com.technophobia.substeps.execution.ImplementationCache;
import com.technophobia.substeps.execution.MethodExecutor;
import com.technophobia.substeps.model.Scope;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.runner.setupteardown.SetupAndTearDown;
import com.technophobia.substeps.runner.syntax.SyntaxBuilder;

/**
 * Takes a tree of execution nodes and executes them, all variables, args,
 * backgrounds already pre-determined
 * 
 * @author ian
 * 
 */
public class ExecutionNodeRunner implements SubstepsRunner {

    private final Logger log = LoggerFactory.getLogger(ExecutionNodeRunner.class);

    private boolean noTestsRun = true;

    private boolean dryRun;

    private ExecutionNode rootNode;
    private final INotificationDistributor notificationDistributor = new NotificationDistributor();
    private SetupAndTearDown setupAndTearDown;
    private ExecutionConfigWrapper config;
    private TagManager nonFatalTagmanager = null;

    private final MethodExecutor methodExecutor = new ImplementationCache();

    public void addNotifier(final INotifier notifier) {

        notificationDistributor.addListener(notifier);
    }

    public void prepareExecutionConfig(final SubstepsExecutionConfig theConfig) {

        config = new ExecutionConfigWrapper(theConfig);
        config.initProperties();

        setupAndTearDown = new SetupAndTearDown(config.getInitialisationClasses(), methodExecutor);

        final String loggingConfigName = config.getDescription() != null ? config.getDescription() : "SubStepsMojo";

        setupAndTearDown.setLoggingConfigName(loggingConfigName);

        final TagManager tagmanager = new TagManager(config.getTags());

        if (config.getNonFatalTags() != null) {
            nonFatalTagmanager = new TagManager(config.getNonFatalTags());
        }

        File subStepsFile = null;

        if (config.getSubStepsFileName() != null) {
            subStepsFile = new File(config.getSubStepsFileName());
        }

        final Syntax syntax = SyntaxBuilder.buildSyntax(config.getStepImplementationClasses(), subStepsFile,
                config.isStrict(), config.getNonStrictKeywordPrecedence());

        final TestParameters parameters = new TestParameters(tagmanager, syntax, config.getFeatureFile());

        parameters.setFailParseErrorsImmediately(config.isFastFailParseErrors());
        parameters.init();

        final ExecutionNodeTreeBuilder nodeTreeBuilder = new ExecutionNodeTreeBuilder(parameters);

        // building the tree can throw critical failures if exceptions are found
        rootNode = nodeTreeBuilder.buildExecutionNodeTree();

        ExecutionContext.put(Scope.SUITE, INotificationDistributor.NOTIFIER_DISTRIBUTOR_KEY, notificationDistributor);

        final String dryRunProperty = System.getProperty("dryRun");
        if (dryRunProperty != null && Boolean.parseBoolean(dryRunProperty)) {

            log.info("**** DRY RUN ONLY **");

            setupAndTearDown.setDryRun(true);
            setDryRun(true);
        }

    }

    public List<SubstepExecutionFailure> run() {
        log.trace("run root node");
        noTestsRun = true;

        // TODO - why is this here twice?
        ExecutionContext.put(Scope.SUITE, INotificationDistributor.NOTIFIER_DISTRIBUTOR_KEY, notificationDistributor);

        final List<SubstepExecutionFailure> failures = runExecutionNodeHierarchy(Scope.SUITE, rootNode);

        if (noTestsRun) {

            final Throwable t = new IllegalStateException("No tests executed");
            rootNode.getResult().setFailed(t);
            notificationDistributor.notifyNodeFailed(rootNode, t);

            addFailure(failures, new SubstepExecutionFailure(t, rootNode));

        }

        return failures;
    }

    private void addFailure(final List<SubstepExecutionFailure> failures, final SubstepExecutionFailure failure) {
        failures.add(failure);
        logFailure(failure);

        // set the criticality of this failure

        if (!failure.isSetupOrTearDown() && this.nonFatalTagmanager != null
                && nonFatalTagmanager.acceptTaggedScenario(failure.getExeccutionNode().getTagsFromHierarchy())) {

            failure.setNonCritical(true);
        }

    }

    private List<SubstepExecutionFailure> runExecutionNodeHierarchy(final Scope scope, final ExecutionNode node) {

        log.trace("run Node Hierarchy @ " + scope.name() + ":" + node.getDebugStringForThisNode());

        final List<SubstepExecutionFailure> failures = Lists.newArrayList();

        notificationDistributor.notifyNodeStarted(node);

        // node may have parsing error, in which case, bail immediately
        if (node.hasError()) {

            notificationDistributor.notifyNodeFailed(node, node.getResult().getThrown());
            addFailure(failures, new SubstepExecutionFailure(node.getResult().getThrown(), node));

        } else {

            node.getResult().setStarted();

            runSetupIfNecessary(scope, node, failures);

            runBackgroundNodes(node, failures);

            if (failures.isEmpty()) {
                executeNodeOrRunChildren(scope, node, failures);
            }

            runTearDown(scope, node, failures);

            recordResult(node, failures);
        }
        return failures;
    }

    private void runSetupIfNecessary(final Scope scope, final ExecutionNode node,
            final List<SubstepExecutionFailure> failures) {

        try {
            if (!node.isOutlineScenario()) {
                setupAndTearDown.runSetup(scope);
            }
        } catch (final Throwable t) {
            log.debug("setup failed", t);

            addFailure(failures, new SubstepExecutionFailure(t, node, true));
        }
    }

    private void runBackgroundNodes(final ExecutionNode node, final List<SubstepExecutionFailure> failures) {
        if (failures.isEmpty() && node.hasBackground() && !node.isOutlineScenario()) {
            // any of these fail then bail & mark this node as failed

            Iterator<ExecutionNode> backgroundNodesIt = node.getBackgrounds().iterator();

            while (failures.isEmpty() && backgroundNodesIt.hasNext()) {

                final List<SubstepExecutionFailure> backgroundScenarioFailures = runExecutionNodeHierarchy(
                        Scope.SCENARIO_BACKGROUND, backgroundNodesIt.next());

                if (!backgroundScenarioFailures.isEmpty()) {

                    log.debug("running background scenarios failed");
                    failures.addAll(backgroundScenarioFailures);
                }
            }
        }
    }

    private void executeNodeOrRunChildren(final Scope scope, final ExecutionNode node,
            final List<SubstepExecutionFailure> failures) {

        if (node.isExecutable()) {

            final SubstepExecutionFailure methodInvocationFailure = executeNodeMethod(node);
            if (methodInvocationFailure != null) {

                addFailure(failures, methodInvocationFailure);
            }

        } else {
            runChildren(scope, node, failures);
        }
    }

    private void runChildren(final Scope scope, final ExecutionNode node, final List<SubstepExecutionFailure> failures) {

        if (node.shouldHaveChildren() && !node.hasChildren()) {
            // TODO - better error message required
            addFailure(failures, new SubstepExecutionFailure(new IllegalStateException(
                    "node should have children but doesn't"), node));
        } else if (node.hasChildren()) {
            log.trace("node has children");
            // if any fail, mark this as failed. if current scope is
            // suite, feature then continue even if failure
            // if scenario or outline or step, bail
            for (final ExecutionNode child : node.getChildren()) {

                final Scope childScope = getChildScope(node, scope);

                final List<SubstepExecutionFailure> childFailures = runExecutionNodeHierarchy(childScope, child);

                if (!childFailures.isEmpty()) {
                    log.debug("running children failed");
                    failures.addAll(childFailures);
                }

                // bail out if current scope is Step or SCENARIO_OUTLINE_ROW

                // bail if current scope is scenario and childscope is step
                if (!failures.isEmpty()
                        && (scope == Scope.STEP || scope == Scope.SCENARIO_OUTLINE_ROW || (scope == Scope.SCENARIO && childScope == Scope.STEP))) {
                    log.debug("bailing out of execution");
                    break;
                }
            }
        }
    }

    private void runTearDown(final Scope scope, final ExecutionNode node, final List<SubstepExecutionFailure> failures) {
        try {
            // run tear down if necessary for this depth and step
            if (!node.isOutlineScenario()) {
                setupAndTearDown.runTearDown(scope);
                ExecutionContext.clear(scope);

            }
        } catch (final Throwable t) {
            log.debug("tear down failed", t);

            failures.add(new SubstepExecutionFailure(t, node, true));
        }
    }

    private void recordResult(final ExecutionNode node, final List<SubstepExecutionFailure> failures) {

        if (failures.isEmpty()) {
            log.debug("node success");
            notificationDistributor.notifyNodeFinished(node);

            node.getResult().setFinished();

        } else {

            log.debug("node failures");
            SubstepExecutionFailure lastFailure = failures.get(failures.size() - 1);
            // just notify on the last one in..?
            final Throwable lastException = lastFailure.getCause();
            notificationDistributor.notifyNodeFailed(node, lastException);

            // TODO should this have been set earlier...?
            node.getResult().setFailed(lastException);
            
            if(node.isExecutable()) {
                
                node.getResult().setScreenshot(lastFailure.getScreenshot());
            }
        }
    }

    /**
     * @param failure
     */
    private void logFailure(final SubstepExecutionFailure failure) {

        final Throwable failureCause = failure.getCause();
        final Throwable here = new Throwable();

        final StackTraceElement[] failureTrace = failureCause.getStackTrace();
        final StackTraceElement[] hereTrace = here.getStackTrace();

        final int requiredTraceSize = failureTrace.length - hereTrace.length;

        if (requiredTraceSize > 0 && requiredTraceSize < failureTrace.length) {

            final StringBuilder stackTraceBuilder = new StringBuilder();

            stackTraceBuilder.append(failureCause.toString()).append("\n");

            for (int i = 0; i < requiredTraceSize; i++) {
                stackTraceBuilder.append("\tat ").append(failureTrace[i]).append("\n");
            }

            log.info("SubstepExecutionFailure @ " + failure.getExeccutionNode().getDebugStringForThisNode() + "\n"
                    + stackTraceBuilder.toString());

        } else {
            // fallback position - just normal logging
            log.info("SubstepExecutionFailure @ " + failure.getExeccutionNode().getDebugStringForThisNode(),
                    failureCause);
        }

    }

    /**
     * @param node
     * @param theException
     * @return
     */
    private SubstepExecutionFailure executeNodeMethod(final ExecutionNode node) {

        SubstepExecutionFailure theFailure = null;
        log.trace("executing node method");

        // if executable invoke
        try {

            if (!dryRun) {
                methodExecutor.executeMethod(node.getTargetClass(), node.getTargetMethod(), node.getMethodArgs());
            }
            noTestsRun = false;

        } catch (final InvocationTargetException e) {

            byte[] screenshotBytes = attemptScreenshot(node.getTargetClass());
            log.info("screenshotBytes=" + screenshotBytes);
            theFailure = new SubstepExecutionFailure(e.getTargetException(), node, screenshotBytes);

        } catch (final Throwable e) {

            byte[] screenshotBytes = attemptScreenshot(node.getTargetClass());
            log.info("screenshotBytes=" + screenshotBytes);
            theFailure = new SubstepExecutionFailure(e, node, screenshotBytes);
        }
        return theFailure;
    }

    @SuppressWarnings("unchecked")
    private <T> byte[] attemptScreenshot(Class<T> targetClass) {

        try {
            log.info(Boolean.valueOf(targetClass.newInstance() instanceof ProvidesScreenshot).toString());
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info(targetClass + " is Assignable? " + ProvidesScreenshot.class.isAssignableFrom(targetClass));

        return ProvidesScreenshot.class.isAssignableFrom(targetClass) ? getScreenshot((Class<? extends ProvidesScreenshot>) targetClass)
                : null;
    }

    private <T extends ProvidesScreenshot> byte[] getScreenshot(Class<T> screenshotClass) {

        T screenshotTakingInstance = methodExecutor.getImplementation(screenshotClass);
        return screenshotTakingInstance.getScreenshotBytes();
    }

    private Scope getChildScope(final ExecutionNode node, final Scope currentScope) {
        Scope rtn = null;

        // maybe a neater way of doing this rather than a case statement...
        switch (currentScope) {
            case SUITE: {
                rtn = Scope.FEATURE;
                break;
            }
            case FEATURE: {
                rtn = Scope.SCENARIO;

                break;
            }
            case SCENARIO: {
                // a scenario can go into outline row or steps
                if (node.isOutlineScenario()) {
                    rtn = Scope.SCENARIO_OUTLINE_ROW;
                } else {
                    rtn = Scope.STEP;
                }
                break;
            }
            case SCENARIO_BACKGROUND:
            case SCENARIO_OUTLINE_ROW:
            case STEP: {
                rtn = Scope.STEP;
                break;
            }
            default: {
                throw new IllegalStateException("impossible state");
            }

        }

        log.trace("child scope: " + rtn.name() + " for: " + currentScope.name());

        return rtn;
    }

    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    public ExecutionNode getRootNode() {

        return this.rootNode;
    }

}

package org.jbehave.web.selenium;

import java.lang.reflect.Method;

import org.jbehave.core.steps.DelegatingStepMonitor;
import org.jbehave.core.steps.StepMonitor;

/**
 * Decorator of {@link StepMonitor} which adds communication of current context
 * to {@link ContextView}.
 */
public class SeleniumStepMonitor extends DelegatingStepMonitor {

    private final ContextView contextView;
    private final SeleniumContext context;

    public SeleniumStepMonitor(ContextView contextView, SeleniumContext context, StepMonitor delegate) {
        super(delegate);
        this.contextView = contextView;
        this.context = context;
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
        String currentScenario = context.getCurrentScenario();
        contextView.show(currentScenario, step);
        super.beforePerforming(step, dryRun, method);
    }

}

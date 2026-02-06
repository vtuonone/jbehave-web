package org.jbehave.web.selenium;

import java.lang.reflect.Method;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.StepMonitor;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SeleniumConfigurationTest {

    private ContextView contextView = mock(ContextView.class);
    private StepMonitor stepMonitor = mock(StepMonitor.class);

    @Test
    public void canConfigureSeleniumContextToShowCurrentScenario() throws Throwable {
        SeleniumContext seleniumContext = new SeleniumContext();
        String currentScenario = "current scenario";
        String step = "a step";
        boolean dryRun = false;
        Configuration configuration = new SeleniumConfiguration()
                .useSeleniumContext(seleniumContext)
                .useStepMonitor(new SeleniumStepMonitor(contextView, seleniumContext, stepMonitor));
        seleniumContext.setCurrentScenario(currentScenario);
        configuration.stepMonitor().beforePerforming(step, dryRun, null);

        verify(contextView).show(currentScenario, step);
        verify(stepMonitor).beforePerforming(step, dryRun, null);
    }

}

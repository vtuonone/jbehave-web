package org.jbehave.web.selenium;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.reporters.NullStoryReporter;

public class SeleniumContextStoryReporter extends NullStoryReporter {
    private final SeleniumContext seleniumContext;

    public SeleniumContextStoryReporter(SeleniumContext seleniumContext) {
        this.seleniumContext = seleniumContext;
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        seleniumContext.setCurrentScenario(scenario.getTitle());
    }
}

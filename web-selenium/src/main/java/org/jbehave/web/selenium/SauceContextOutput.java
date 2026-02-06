package org.jbehave.web.selenium;

import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * A {@link Format} that uses {@link SauceContextStoryReporter}.
 */
public class SauceContextOutput extends Format {

    private final WebDriverProvider webDriverProvider;
    private final SeleniumContext seleniumContext;
    private final java.util.Map<String, String> storyToSauceUrlMap;

    public SauceContextOutput(WebDriverProvider webDriverProvider, SeleniumContext seleniumContext, java.util.Map<String, String> storyToSauceUrlMap) {
        super("SAUCE_CONTEXT");
        this.webDriverProvider = webDriverProvider;
        this.seleniumContext = seleniumContext;
        this.storyToSauceUrlMap = storyToSauceUrlMap;
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory filePrintStreamFactory,
                                             StoryReporterBuilder storyReporterBuilder) {
        return new SauceContextStoryReporter(webDriverProvider, seleniumContext, storyToSauceUrlMap);
    }

}

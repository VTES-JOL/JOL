package net.deckserver.dwr.model.bdd;

import io.cucumber.core.options.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/do-command")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "net.deckserver.dwr.model.bdd")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
public class RunCucumberTest {
}

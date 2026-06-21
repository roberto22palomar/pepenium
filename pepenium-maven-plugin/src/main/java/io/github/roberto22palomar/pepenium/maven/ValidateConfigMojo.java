package io.github.roberto22palomar.pepenium.maven;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Validates a Pepenium YAML configuration without opening a driver session.
 */
@Mojo(name = "validate-config", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ValidateConfigMojo extends AbstractMojo {

    @Parameter(property = "pepenium.config", defaultValue = "${project.basedir}/pepenium.yml")
    private File configFile;

    @Parameter(property = "pepenium.profile")
    private String profileId;

    @Parameter(property = "pepenium.config.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Pepenium configuration validation skipped.");
            return;
        }
        if (configFile == null) {
            throw new MojoExecutionException("Pepenium configuration path was not provided.");
        }
        try {
            PepeniumConfig.validate(configFile.toPath(), profileId);
            String profileSuffix = profileId == null || profileId.isBlank()
                    ? ""
                    : " for profile '" + profileId.trim() + "'";
            getLog().info("Valid Pepenium configuration: " + configFile.getAbsolutePath() + profileSuffix);
        } catch (IllegalArgumentException | IllegalStateException error) {
            throw new MojoFailureException(error.getMessage(), error);
        }
    }

    void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    void setSkip(boolean skip) {
        this.skip = skip;
    }
}

package io.mvnpm.esbuild.maven.mojos;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import io.mvnpm.esbuild.Bundler;
import io.mvnpm.esbuild.model.*;

@Mojo(name = "esbuild", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.TEST, requiresDependencyCollection = ResolutionScope.TEST)
public class EsbuildPluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "src/main/web")
    private String sourceDirectory;

    @Parameter(defaultValue = "target/classes/META-INF/resources/static/bundle")
    private String outputDirectory;

    @Parameter(defaultValue = "index.js")
    private String entryPoint;

    @Parameter(defaultValue = "node_modules")
    private String nodeModules;

    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    @Component
    private MavenProject project;

    @Component
    private MavenSession session;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Plugin plugin = resolvePlugin();

        List<WebDependency> webDeps = new ArrayList<>();
        for (Dependency dependency : plugin.getDependencies()) {
            if (WebDependency.WebDependencyType.anyMatch(dependency.getGroupId())) {
                webDeps.add(toWebDep(dependency));
            }
        }

        final Path basedirPath = basedir.toPath();
        final BundleOptionsBuilder builder = BundleOptions.builder()
                .withWorkDir(basedirPath.resolve(sourceDirectory))
                .withDependencies(webDeps)
                .withEsConfig(EsBuildConfig.builder().fixedEntryNames().build())
                .withNodeModulesDir(basedirPath.resolve(nodeModules))
                .addEntryPoint(entryPoint);
        try {
            final BundleResult bundle = Bundler.bundle(builder.build(), true);
            final Path outputDir = basedirPath.resolve(outputDirectory);
            Files.createDirectories(outputDir);
            deleteDirectoryIfExists(outputDir);
            Files.move(bundle.dist(), outputDir);
            getLog().info("Generated bundle in: " + outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    private Plugin resolvePlugin() throws MojoFailureException {
        for (Plugin buildPlugin : project.getBuildPlugins()) {
            if (buildPlugin.getArtifactId().equals("esbuild-maven-plugin")) {
                return buildPlugin;
            }
        }
        throw new MojoFailureException("esbuild-maven-plugin configuration not found");
    }

    private WebDependency toWebDep(Dependency d) throws MojoExecutionException {

        final Artifact a = resolveArtifact(d);

        final String gav = d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion();
        return new WebDependency(gav, a.getFile().toPath(), WebDependency.WebDependencyType.resolveType(gav).orElseThrow());

    }

    private Artifact resolveArtifact(Dependency d) throws MojoExecutionException {
        Artifact a = new DefaultArtifact(
                d.getGroupId(),
                d.getArtifactId(),
                d.getClassifier(),
                d.getType(),
                d.getVersion());

        ArtifactRequest req = new ArtifactRequest().setRepositories(this.project.getRemoteProjectRepositories()).setArtifact(a);
        ArtifactResult resolutionResult;
        try {
            resolutionResult = this.repoSystem.resolveArtifact(this.repoSession, req);
            return resolutionResult.getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Artifact " + d.getArtifactId() + " could not be resolved.", e);
        }
    }

    private static void deleteDirectoryIfExists(final Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    // ignored
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                } catch (IOException e) {
                    // ignored
                }
                return FileVisitResult.CONTINUE;
            }

        });
    }

}

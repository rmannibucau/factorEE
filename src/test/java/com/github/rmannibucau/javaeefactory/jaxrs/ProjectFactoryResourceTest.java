package com.github.rmannibucau.javaeefactory.jaxrs;

import com.github.rmannibucau.javaeefactory.jaxrs.model.FactoryConfiguration;
import com.github.rmannibucau.javaeefactory.jaxrs.model.ProjectModel;
import com.github.rmannibucau.javaeefactory.service.IOService;
import com.github.rmannibucau.javaeefactory.test.JavaEEFactory;
import org.apache.openejb.testing.Application;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JavaEEFactory.Runner.class)
public class ProjectFactoryResourceTest {
    @Application
    private JavaEEFactory blog;

    @Inject
    private IOService io;

    @Test
    public void config() {
        final FactoryConfiguration config = blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(FactoryConfiguration.class);
        assertEquals(new HashSet<>(asList("Gradle", "Maven")), new HashSet<>(config.getBuildTypes()));
        assertEquals(new HashMap<String, List<FactoryConfiguration.Facet>>() {{
            put("Core", asList(
                    new FactoryConfiguration.Facet("JAX-RS", "Generate a Hello World JAX-RS endpoint."),
                    new FactoryConfiguration.Facet("OpenJPA", "OpenJPA as JPA provider")
            ));
            put("Libraries", asList(
                    new FactoryConfiguration.Facet("Deltaspike Configuration", "@Inject your @ConfigProperty values."),
                    new FactoryConfiguration.Facet("Lombok", "Synthaxic sugar for Java.")
            ));
            put("Test", asList(
                    new FactoryConfiguration.Facet("ApplicationComposer", "Generates ApplicationComposer based test(s)."),
                    new FactoryConfiguration.Facet("Arquillian", "Generates Arquillian test(s) with TomEE Remote.")
            ));
        }}, new HashMap<>(config.getFacets()));
    }

    @Test
    public void getZip() throws IOException { // this is the same as POST but using a GEt as workaround for Safari
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory/zip")
                .queryParam("buildType", "Maven")
                .queryParam("facets", "JAX-RS")
                .queryParam("facets", "Arquillian")
                .queryParam("facets", "OpenJPA")
                .queryParam("facets", "Lombok")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .get(InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }

        // impl reuse the POST one so should be fine, just check files are created as expected
        // just a sanity check to ensure we generated a project matching facets
        assertEquals(30, files.size());
        Stream.of(
                "application/src/main/java/com/application/jaxrs/HelloResource.java",
                "application/src/test/resources/arquillian.xml",
                "application/src/main/java/com/application/jpa/HelloEntity.java")
                .forEach(path -> assertTrue(files.containsKey(path)));
        assertTrue(files.get("application/pom.xml").contains("lombok"));
    }

    @Test
    public void generateDefaultMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel(), MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(8, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"\n" +
                        "          http://maven.apache.org/POM/4.0.0\n" +
                        "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>com.application</groupId>\n" +
                        "  <artifactId>application</artifactId>\n" +
                        "  <version>0.0.1-SNAPSHOT</version>\n" +
                        "  <packaging>war</packaging>\n" +
                        "\n" +
                        "  <name>A Factory generated Application</name>\n" +
                        "  <description>An application generated by the Factory</description>\n" +
                        "\n" +
                        "  <properties>\n" +
                        "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                        "  </properties>\n" +
                        "\n" +
                        "  <dependencies>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.apache.tomee</groupId>\n" +
                        "      <artifactId>javaee-api</artifactId>\n" +
                        "      <version>7.0</version>\n" +
                        "      <scope>provided</scope>\n" +
                        "    </dependency>\n" +
                        "  </dependencies>\n" +
                        "\n" +
                        "  <build>\n" +
                        "    <plugins>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                        "        <version>3.5.1</version>\n" +
                        "        <configuration>\n" +
                        "          <source>1.8</source>\n" +
                        "          <target>1.8</target>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.tomee.maven</groupId>\n" +
                        "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                        "        <version>7.0.0-M3</version>\n" +
                        "        <configuration>\n" +
                        "          <context>/${project.artifactId}</context>\n" +
                        "          <classpathAsWar>true</classpathAsWar>\n" +
                        "          <webResourceCached>false</webResourceCached>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.tomee.maven</groupId>\n" +
                        "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                        "        <version>7.0.0-M3</version>\n" +
                        "        <configuration>\n" +
                        "          <context>/${project.artifactId}</context>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-war-plugin</artifactId>\n" +
                        "        <version>2.6</version>\n" +
                        "        <configuration>\n" +
                        "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                        "        <version>2.19</version>\n" +
                        "        <configuration>\n" +
                        "          <trimStackTrace>false</trimStackTrace>\n" +
                        "          <runOrder>alphabetical</runOrder>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "    </plugins>\n" +
                        "  </build>\n" +
                        "</project>\n", files.get("application/pom.xml"));
    }

    @Test
    public void generateJAXRSMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(singletonList("JAX-RS"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(15, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== JAX-RS\n" +
                "\n" +
                "JAX-RS is the big boom of JavaEE 6. It allows to create a smooth frontend naturally integrated with Javascript \n" +
                "application when combined with JSON.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jaxrs/ApplicationConfig.java\n" +
                "- src/main/java/com/application/jaxrs/HelloResource.java\n" +
                "- src/main/java/com/application/jaxrs/Hello.java\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jaxrs/"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "public class Hello {\n" +
                "    private String name;\n" +
                "\n" +
                "    public String getName() {\n" +
                "        return name;\n" +
                "    }\n" +
                "\n" +
                "    public void setName(final String n) {\n" +
                "        name = n;\n" +
                "    }\n" +
                "}\n", files.get("application/src/main/java/com/application/jaxrs/Hello.java"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "import javax.ws.rs.GET;\n" +
                "import javax.ws.rs.Path;\n" +
                "import javax.ws.rs.Produces;\n" +
                "import javax.ws.rs.QueryParam;\n" +
                "import javax.ws.rs.core.MediaType;\n" +
                "\n" +
                "@Path(\"hello\")\n" +
                "public class HelloResource {\n" +
                "    @GET\n" +
                "    @Produces(MediaType.APPLICATION_JSON)\n" +
                "    public Hello hello(@QueryParam(\"name\") final String name) {\n" +
                "        final Hello hello = new Hello();\n" +
                "        hello.setName(name == null ? \"test\" : name);\n" +
                "        return hello;\n" +
                "    }\n" +
                "}\n", files.get("application/src/main/java/com/application/jaxrs/HelloResource.java"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "import javax.ws.rs.ApplicationPath;\n" +
                "import javax.ws.rs.core.Application;\n" +
                "\n" +
                "@ApplicationPath(\"api\")\n" +
                "public class ApplicationConfig extends Application {\n" +
                "    // empty means scanning\n" +
                "}\n", files.get("application/src/main/java/com/application/jaxrs/ApplicationConfig.java"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"\n" +
                        "          http://maven.apache.org/POM/4.0.0\n" +
                        "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>com.application</groupId>\n" +
                        "  <artifactId>application</artifactId>\n" +
                        "  <version>0.0.1-SNAPSHOT</version>\n" +
                        "  <packaging>war</packaging>\n" +
                        "\n" +
                        "  <name>A Factory generated Application</name>\n" +
                        "  <description>An application generated by the Factory</description>\n" +
                        "\n" +
                        "  <properties>\n" +
                        "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                        "  </properties>\n" +
                        "\n" +
                        "  <dependencies>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.apache.tomee</groupId>\n" +
                        "      <artifactId>javaee-api</artifactId>\n" +
                        "      <version>7.0</version>\n" +
                        "      <scope>provided</scope>\n" +
                        "    </dependency>\n" +
                        "  </dependencies>\n" +
                        "\n" +
                        "  <build>\n" +
                        "    <plugins>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                        "        <version>3.5.1</version>\n" +
                        "        <configuration>\n" +
                        "          <source>1.8</source>\n" +
                        "          <target>1.8</target>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.tomee.maven</groupId>\n" +
                        "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                        "        <version>7.0.0-M3</version>\n" +
                        "        <configuration>\n" +
                        "          <context>/${project.artifactId}</context>\n" +
                        "          <classpathAsWar>true</classpathAsWar>\n" +
                        "          <webResourceCached>false</webResourceCached>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.tomee.maven</groupId>\n" +
                        "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                        "        <version>7.0.0-M3</version>\n" +
                        "        <configuration>\n" +
                        "          <context>/${project.artifactId}</context>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-war-plugin</artifactId>\n" +
                        "        <version>2.6</version>\n" +
                        "        <configuration>\n" +
                        "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.apache.maven.plugins</groupId>\n" +
                        "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                        "        <version>2.19</version>\n" +
                        "        <configuration>\n" +
                        "          <trimStackTrace>false</trimStackTrace>\n" +
                        "          <runOrder>alphabetical</runOrder>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "    </plugins>\n" +
                        "  </build>\n" +
                        "</project>\n", files.get("application/pom.xml"));
    }

    @Test
    public void applicationComposerJAXRS() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("ApplicationComposer", "JAX-RS"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(21, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== JAX-RS\n" +
                "\n" +
                "JAX-RS is the big boom of JavaEE 6. It allows to create a smooth frontend naturally integrated with Javascript \n" +
                "application when combined with JSON.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jaxrs/ApplicationConfig.java\n" +
                "- src/main/java/com/application/jaxrs/HelloResource.java\n" +
                "- src/main/java/com/application/jaxrs/Hello.java\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== ApplicationComposer\n" +
                "\n" +
                "ApplicationComposer is a TomEE feature allowing to:\n" +
                "\n" +
                "- test with JUnit or TestNG any EE application in embedded mode building programmatically the EE model instead of relying on scanning\n" +
                "- deploy a standalone application using ApplicationComposers helper (very useful for application without frontend like batch)\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jaxrs/ApplicationComposerHelloResourceTest.java\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/test/"));
        assertEquals("", files.get("application/src/test/java/"));
        assertEquals("", files.get("application/src/test/java/com/"));
        assertEquals("", files.get("application/src/test/java/com/application/"));
        assertEquals("", files.get("application/src/test/java/com/application/jaxrs/"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "import org.apache.openejb.junit.ApplicationComposer;\n" +
                "import org.apache.openejb.testing.CdiExtensions;\n" +
                "import org.apache.openejb.testing.Classes;\n" +
                "import org.apache.openejb.testing.EnableServices;\n" +
                "import org.apache.openejb.testing.RandomPort;\n" +
                "import org.apache.openejb.testing.SimpleLog;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.ws.rs.client.ClientBuilder;\n" +
                "import javax.ws.rs.core.MediaType;\n" +
                "import java.net.URL;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@SimpleLog\n" +
                "@EnableServices(\"jaxrs\")\n" +
                "@CdiExtensions(/* skip extensions since not needed in this test */)\n" +
                "@Classes(cdi = true, context = \"app\", value = {ApplicationConfig.class, HelloResource.class})\n" +
                "@RunWith(ApplicationComposer.class)\n" +
                "public class ApplicationComposerHelloResourceTest {\n" +
                "    @RandomPort(\"http\")\n" +
                "    private URL base;\n" +
                "\n" +
                "    @Test\n" +
                "    public void sayHelloToTheDefaultUser() {\n" +
                "        assertEquals(\n" +
                "                \"test\",\n" +
                "                ClientBuilder.newBuilder().build()\n" +
                "                        .target(base.toExternalForm()).path(\"app/api/hello\")\n" +
                "                        .request(MediaType.APPLICATION_JSON_TYPE)\n" +
                "                        .get(Hello.class)\n" +
                "                        .getName());\n" +
                "    }\n" +
                "}\n", files.get("application/src/test/java/com/application/jaxrs/ApplicationComposerHelloResourceTest.java"));
    }

    @Test
    public void openjpaMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(singletonList("OpenJPA"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(16, files.size());
        // just testing additional ones compared to simple JAXRS
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jpa/"));
        assertEquals(
                "package com.application.jpa;\n" +
                        "\n" +
                        "import javax.persistence.Column;\n" +
                        "import javax.persistence.Entity;\n" +
                        "import javax.persistence.GeneratedValue;\n" +
                        "import javax.persistence.Id;\n" +
                        "import javax.persistence.NamedQueries;\n" +
                        "import javax.persistence.NamedQuery;\n" +
                        "import javax.persistence.Table;\n" +
                        "import javax.persistence.Version;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@NamedQueries({\n" +
                        "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                        "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                        "})\n" +
                        "@Table(name = \"hello\")\n" +
                        "public class HelloEntity {\n" +
                        "    @Id\n" +
                        "    @GeneratedValue\n" +
                        "    private long id;\n" +
                        "\n" +
                        "    @Version\n" +
                        "    private long version;\n" +
                        "\n" +
                        "    @Column(length = 160)\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    public long getId() {\n" +
                        "        return id;\n" +
                        "    }\n" +
                        "\n" +
                        "    public long getVersion() {\n" +
                        "        return version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setVersion(final long version) {\n" +
                        "        this.version = version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(final String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
        assertEquals("", files.get("application/src/main/resources/"));
        assertEquals("", files.get("application/src/main/resources/META-INF/"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"\n" +
                        "              http://java.sun.com/xml/ns/persistence\n" +
                        "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                        "             version=\"2.0\">\n" +
                        "  <persistence-unit name=\"jpa\">\n" +
                        "    <jta-data-source>jpaDataSource</jta-data-source>\n" +
                        "    <properties>\n" +
                        "      <!-- auto create tables -->\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "\n" +
                        "      <!-- avoid to list classes but also avoids JPA to scan by itself reusing main container scanning -->\n" +
                        "      <property name=\"openejb.jpa.auto-scan\" value=\"true\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>\n",
                files.get("application/src/main/resources/META-INF/persistence.xml"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.openjpa</groupId>\n" +
                "      <artifactId>openjpa</artifactId>\n" +
                "      <version>2.4.1</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.openjpa</groupId>\n" +
                "        <artifactId>openjpa-maven-plugin</artifactId>\n" +
                "        <version>2.4.1</version>\n" +
                "        <executions>\n" +
                "          <execution>\n" +
                "            <id>openjpa-enhance</id>\n" +
                "            <phase>process-classes</phase>\n" +
                "            <goals>\n" +
                "              <goal>enhance</goal>\n" +
                "            </goals>\n" +
                "          </execution>\n" +
                "        </executions>\n" +
                "        <configuration>\n" +
                "          <includes>com/application/jpa/*.class</includes>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
    }

    @Test
    public void openjpaGradle() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Gradle");
                    setFacets(singletonList("OpenJPA"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(16, files.size());
        // just testing additional ones compared to simple JAXRS
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jpa/"));
        assertEquals(
                "package com.application.jpa;\n" +
                        "\n" +
                        "import javax.persistence.Column;\n" +
                        "import javax.persistence.Entity;\n" +
                        "import javax.persistence.GeneratedValue;\n" +
                        "import javax.persistence.Id;\n" +
                        "import javax.persistence.NamedQueries;\n" +
                        "import javax.persistence.NamedQuery;\n" +
                        "import javax.persistence.Table;\n" +
                        "import javax.persistence.Version;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@NamedQueries({\n" +
                        "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                        "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                        "})\n" +
                        "@Table(name = \"hello\")\n" +
                        "public class HelloEntity {\n" +
                        "    @Id\n" +
                        "    @GeneratedValue\n" +
                        "    private long id;\n" +
                        "\n" +
                        "    @Version\n" +
                        "    private long version;\n" +
                        "\n" +
                        "    @Column(length = 160)\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    public long getId() {\n" +
                        "        return id;\n" +
                        "    }\n" +
                        "\n" +
                        "    public long getVersion() {\n" +
                        "        return version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setVersion(final long version) {\n" +
                        "        this.version = version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(final String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
        assertEquals("", files.get("application/src/main/resources/"));
        assertEquals("", files.get("application/src/main/resources/META-INF/"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"\n" +
                        "              http://java.sun.com/xml/ns/persistence\n" +
                        "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                        "             version=\"2.0\">\n" +
                        "  <persistence-unit name=\"jpa\">\n" +
                        "    <jta-data-source>jpaDataSource</jta-data-source>\n" +
                        "    <properties>\n" +
                        "      <!-- auto create tables -->\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "\n" +
                        "      <!-- avoid to list classes but also avoids JPA to scan by itself reusing main container scanning -->\n" +
                        "      <property name=\"openejb.jpa.auto-scan\" value=\"true\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>\n",
                files.get("application/src/main/resources/META-INF/persistence.xml"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("buildscript {\n" +
                "  repositories {\n" +
                "    maven {\n" +
                "      url \"https://plugins.gradle.org/m2/\"\n" +
                "    }\n" +
                "  }\n" +
                "  dependencies {\n" +
                "    classpath \"com.netflix.nebula:gradle-extra-configurations-plugin:3.0.3\"\n" +
                "    classpath \"org.apache.openjpa:openjpa:2.4.1\"\n" +
                "    classpath \"at.schmutterer.oss.gradle:gradle-openjpa:0.2.0\"\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "apply plugin: 'nebula.provided-base'\n" +
                "apply plugin: 'java'\n" +
                "apply plugin: 'war'\n" +
                "apply plugin: 'openjpa'\n" +
                "\n" +
                "\n" +
                "repositories {\n" +
                "  mavenLocal()\n" +
                "  mavenCentral()\n" +
                "}\n" +
                "\n" +
                "group = 'com.application'\n" +
                "description = 'An application generated by the Factory'\n" +
                "\n" +
                "\n" +
                "war {\n" +
                "  baseName = 'application'\n" +
                "  version = '0.0.1-SNAPSHOT'\n" +
                "}\n" +
                "\n" +
                "test {\n" +
                "  testLogging.showStandardStreams = true\n" +
                "}\n" +
                "\n" +
                "openjpa {\n" +
                "  files = fileTree(sourceSets.main.output.classesDir).matching {\n" +
                "    include 'com/application/jpa/**'\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "sourceCompatibility = 1.8\n" +
                "targetCompatibility = 1.8\n" +
                "\n" +
                "repositories {\n" +
                "  mavenCentral()\n" +
                "}\n" +
                "\n" +
                "dependencies {\n" +
                "  provided group: 'org.apache.tomee', name: 'javaee-api', version: '7.0'\n" +
                "  provided group: 'org.apache.openjpa', name: 'openjpa', version: '2.4.1'\n" +
                "}\n", files.get("application/build.gradle"));
    }

    @Test
    public void openjpaAppComposerGradle() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Gradle");
                    setFacets(asList("OpenJPA", "ApplicationComposer"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(22, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== ApplicationComposer\n" +
                "\n" +
                "ApplicationComposer is a TomEE feature allowing to:\n" +
                "\n" +
                "- test with JUnit or TestNG any EE application in embedded mode building programmatically the EE model instead of relying on scanning\n" +
                "- deploy a standalone application using ApplicationComposers helper (very useful for application without frontend like batch)\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jpa/ApplicationComposerHelloEntityTest.java\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/test/"));
        assertEquals("", files.get("application/src/test/java/"));
        assertEquals("", files.get("application/src/test/java/com/"));
        assertEquals("", files.get("application/src/test/java/com/application/"));
        assertEquals("", files.get("application/src/test/java/com/application/jpa/"));
        assertEquals("package com.application.jpa;\n" +
                "\n" +
                "import org.apache.openejb.api.configuration.PersistenceUnitDefinition;\n" +
                "import org.apache.openejb.junit.ApplicationComposer;\n" +
                "import org.apache.openejb.testing.CdiExtensions;\n" +
                "import org.apache.openejb.testing.Classes;\n" +
                "import org.apache.openejb.testing.SimpleLog;\n" +
                "import org.junit.After;\n" +
                "import org.junit.Before;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.annotation.Resource;\n" +
                "import javax.persistence.EntityManager;\n" +
                "import javax.persistence.PersistenceContext;\n" +
                "import javax.transaction.UserTransaction;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@SimpleLog\n" +
                "@CdiExtensions(/* skip extensions since not needed in this test */)\n" +
                "@Classes(cdi = true, value = HelloEntity.class /* to find by scanning the entity */)\n" +
                "@PersistenceUnitDefinition // activates JPA\n" +
                "@RunWith(ApplicationComposer.class)\n" +
                "public class ApplicationComposerHelloEntityTest {\n" +
                "    @PersistenceContext\n" +
                "    private EntityManager entityManager;\n" +
                "\n" +
                "    @Resource\n" +
                "    private UserTransaction userTransaction;\n" +
                "\n" +
                "    private long id;\n" +
                "\n" +
                "    @Before\n" +
                "    public void before() throws Exception {\n" +
                "        userTransaction.begin();\n" +
                "        final HelloEntity helloEntity = new HelloEntity();\n" +
                "        helloEntity.setName(\"test\");\n" +
                "        entityManager.persist(helloEntity);\n" +
                "        entityManager.flush();\n" +
                "        id = helloEntity.getId();\n" +
                "        userTransaction.commit();\n" +
                "    }\n" +
                "\n" +
                "    @After\n" +
                "    public void after() throws Exception {\n" +
                "        userTransaction.begin();\n" +
                "        entityManager.remove(entityManager.getReference(HelloEntity.class, id));\n" +
                "        userTransaction.commit();\n" +
                "    }\n" +
                "\n" +
                "    @Test\n" +
                "    public void sayHelloToTheDefaultUser() {\n" +
                "        assertEquals(\n" +
                "                1,\n" +
                "                entityManager.createNamedQuery(\"HelloEntity.findByName\")\n" +
                "                        .setParameter(\"name\", \"test\")\n" +
                "                        .getResultList().size());\n" +
                "    }\n" +
                "}\n", files.get("application/src/test/java/com/application/jpa/ApplicationComposerHelloEntityTest.java"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jpa/"));
        assertEquals(
                "package com.application.jpa;\n" +
                        "\n" +
                        "import javax.persistence.Column;\n" +
                        "import javax.persistence.Entity;\n" +
                        "import javax.persistence.GeneratedValue;\n" +
                        "import javax.persistence.Id;\n" +
                        "import javax.persistence.NamedQueries;\n" +
                        "import javax.persistence.NamedQuery;\n" +
                        "import javax.persistence.Table;\n" +
                        "import javax.persistence.Version;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@NamedQueries({\n" +
                        "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                        "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                        "})\n" +
                        "@Table(name = \"hello\")\n" +
                        "public class HelloEntity {\n" +
                        "    @Id\n" +
                        "    @GeneratedValue\n" +
                        "    private long id;\n" +
                        "\n" +
                        "    @Version\n" +
                        "    private long version;\n" +
                        "\n" +
                        "    @Column(length = 160)\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    public long getId() {\n" +
                        "        return id;\n" +
                        "    }\n" +
                        "\n" +
                        "    public long getVersion() {\n" +
                        "        return version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setVersion(final long version) {\n" +
                        "        this.version = version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(final String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
        assertEquals("", files.get("application/src/main/resources/"));
        assertEquals("", files.get("application/src/main/resources/META-INF/"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"\n" +
                        "              http://java.sun.com/xml/ns/persistence\n" +
                        "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                        "             version=\"2.0\">\n" +
                        "  <persistence-unit name=\"jpa\">\n" +
                        "    <jta-data-source>jpaDataSource</jta-data-source>\n" +
                        "    <properties>\n" +
                        "      <!-- auto create tables -->\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "\n" +
                        "      <!-- avoid to list classes but also avoids JPA to scan by itself reusing main container scanning -->\n" +
                        "      <property name=\"openejb.jpa.auto-scan\" value=\"true\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>\n",
                files.get("application/src/main/resources/META-INF/persistence.xml"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("buildscript {\n" +
                "  repositories {\n" +
                "    maven {\n" +
                "      url \"https://plugins.gradle.org/m2/\"\n" +
                "    }\n" +
                "  }\n" +
                "  dependencies {\n" +
                "    classpath \"com.netflix.nebula:gradle-extra-configurations-plugin:3.0.3\"\n" +
                "    classpath \"org.apache.openjpa:openjpa:2.4.1\"\n" +
                "    classpath \"at.schmutterer.oss.gradle:gradle-openjpa:0.2.0\"\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "apply plugin: 'nebula.provided-base'\n" +
                "apply plugin: 'java'\n" +
                "apply plugin: 'war'\n" +
                "apply plugin: 'openjpa'\n" +
                "\n" +
                "\n" +
                "repositories {\n" +
                "  mavenLocal()\n" +
                "  mavenCentral()\n" +
                "}\n" +
                "\n" +
                "group = 'com.application'\n" +
                "description = 'An application generated by the Factory'\n" +
                "\n" +
                "configurations { \n" +
                "    javaagentOpenJPA\n" +
                "} \n" +
                "\n" +
                "war {\n" +
                "  baseName = 'application'\n" +
                "  version = '0.0.1-SNAPSHOT'\n" +
                "}\n" +
                "\n" +
                "test {\n" +
                "  testLogging.showStandardStreams = true\n" +
                "}\n" +
                "\n" +
                "openjpa {\n" +
                "  files = fileTree(sourceSets.main.output.classesDir).matching {\n" +
                "    include 'com/application/jpa/**'\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "test {\n" +
                "  testLogging.showStandardStreams = true\n" +
                "}\n" +
                "\n" +
                "test.doFirst {\n" +
                "  jvmArgs \"-javaagent:${configurations.javaagentOpenJPA.singleFile}\"\n" +
                "}\n" +
                "\n" +
                "sourceCompatibility = 1.8\n" +
                "targetCompatibility = 1.8\n" +
                "\n" +
                "repositories {\n" +
                "  mavenCentral()\n" +
                "}\n" +
                "\n" +
                "dependencies {\n" +
                "  provided group: 'org.apache.tomee', name: 'javaee-api', version: '7.0'\n" +
                "  provided group: 'org.apache.openjpa', name: 'openjpa', version: '2.4.1'\n" +
                "  testCompile group: 'junit', name: 'junit', version: '4.12'\n" +
                "  testCompile group: 'org.apache.tomee', name: 'openejb-core', version: '7.0.0-M3'\n" +
                "  javaagentOpenJPA group: 'org.apache.tomee', name: 'openejb-javaagent', version: '7.0.0-M3'\n" +
                "}\n", files.get("application/build.gradle"));
    }

    @Test
    public void openjpaAppComposerMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("OpenJPA", "ApplicationComposer"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(22, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== ApplicationComposer\n" +
                "\n" +
                "ApplicationComposer is a TomEE feature allowing to:\n" +
                "\n" +
                "- test with JUnit or TestNG any EE application in embedded mode building programmatically the EE model instead of relying on scanning\n" +
                "- deploy a standalone application using ApplicationComposers helper (very useful for application without frontend like batch)\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jpa/ApplicationComposerHelloEntityTest.java\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/test/"));
        assertEquals("", files.get("application/src/test/java/"));
        assertEquals("", files.get("application/src/test/java/com/"));
        assertEquals("", files.get("application/src/test/java/com/application/"));
        assertEquals("", files.get("application/src/test/java/com/application/jpa/"));
        assertEquals("package com.application.jpa;\n" +
                "\n" +
                "import org.apache.openejb.api.configuration.PersistenceUnitDefinition;\n" +
                "import org.apache.openejb.junit.ApplicationComposer;\n" +
                "import org.apache.openejb.testing.CdiExtensions;\n" +
                "import org.apache.openejb.testing.Classes;\n" +
                "import org.apache.openejb.testing.SimpleLog;\n" +
                "import org.junit.After;\n" +
                "import org.junit.Before;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.annotation.Resource;\n" +
                "import javax.persistence.EntityManager;\n" +
                "import javax.persistence.PersistenceContext;\n" +
                "import javax.transaction.UserTransaction;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@SimpleLog\n" +
                "@CdiExtensions(/* skip extensions since not needed in this test */)\n" +
                "@Classes(cdi = true, value = HelloEntity.class /* to find by scanning the entity */)\n" +
                "@PersistenceUnitDefinition // activates JPA\n" +
                "@RunWith(ApplicationComposer.class)\n" +
                "public class ApplicationComposerHelloEntityTest {\n" +
                "    @PersistenceContext\n" +
                "    private EntityManager entityManager;\n" +
                "\n" +
                "    @Resource\n" +
                "    private UserTransaction userTransaction;\n" +
                "\n" +
                "    private long id;\n" +
                "\n" +
                "    @Before\n" +
                "    public void before() throws Exception {\n" +
                "        userTransaction.begin();\n" +
                "        final HelloEntity helloEntity = new HelloEntity();\n" +
                "        helloEntity.setName(\"test\");\n" +
                "        entityManager.persist(helloEntity);\n" +
                "        entityManager.flush();\n" +
                "        id = helloEntity.getId();\n" +
                "        userTransaction.commit();\n" +
                "    }\n" +
                "\n" +
                "    @After\n" +
                "    public void after() throws Exception {\n" +
                "        userTransaction.begin();\n" +
                "        entityManager.remove(entityManager.getReference(HelloEntity.class, id));\n" +
                "        userTransaction.commit();\n" +
                "    }\n" +
                "\n" +
                "    @Test\n" +
                "    public void sayHelloToTheDefaultUser() {\n" +
                "        assertEquals(\n" +
                "                1,\n" +
                "                entityManager.createNamedQuery(\"HelloEntity.findByName\")\n" +
                "                        .setParameter(\"name\", \"test\")\n" +
                "                        .getResultList().size());\n" +
                "    }\n" +
                "}\n", files.get("application/src/test/java/com/application/jpa/ApplicationComposerHelloEntityTest.java"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jpa/"));
        assertEquals(
                "package com.application.jpa;\n" +
                        "\n" +
                        "import javax.persistence.Column;\n" +
                        "import javax.persistence.Entity;\n" +
                        "import javax.persistence.GeneratedValue;\n" +
                        "import javax.persistence.Id;\n" +
                        "import javax.persistence.NamedQueries;\n" +
                        "import javax.persistence.NamedQuery;\n" +
                        "import javax.persistence.Table;\n" +
                        "import javax.persistence.Version;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@NamedQueries({\n" +
                        "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                        "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                        "})\n" +
                        "@Table(name = \"hello\")\n" +
                        "public class HelloEntity {\n" +
                        "    @Id\n" +
                        "    @GeneratedValue\n" +
                        "    private long id;\n" +
                        "\n" +
                        "    @Version\n" +
                        "    private long version;\n" +
                        "\n" +
                        "    @Column(length = 160)\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    public long getId() {\n" +
                        "        return id;\n" +
                        "    }\n" +
                        "\n" +
                        "    public long getVersion() {\n" +
                        "        return version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setVersion(final long version) {\n" +
                        "        this.version = version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(final String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
        assertEquals("", files.get("application/src/main/resources/"));
        assertEquals("", files.get("application/src/main/resources/META-INF/"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"\n" +
                        "              http://java.sun.com/xml/ns/persistence\n" +
                        "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                        "             version=\"2.0\">\n" +
                        "  <persistence-unit name=\"jpa\">\n" +
                        "    <jta-data-source>jpaDataSource</jta-data-source>\n" +
                        "    <properties>\n" +
                        "      <!-- auto create tables -->\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "\n" +
                        "      <!-- avoid to list classes but also avoids JPA to scan by itself reusing main container scanning -->\n" +
                        "      <property name=\"openejb.jpa.auto-scan\" value=\"true\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>\n",
                files.get("application/src/main/resources/META-INF/persistence.xml"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.openjpa</groupId>\n" +
                "      <artifactId>openjpa</artifactId>\n" +
                "      <version>2.4.1</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>junit</groupId>\n" +
                "      <artifactId>junit</artifactId>\n" +
                "      <version>4.12</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>openejb-core</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.openjpa</groupId>\n" +
                "        <artifactId>openjpa-maven-plugin</artifactId>\n" +
                "        <version>2.4.1</version>\n" +
                "        <executions>\n" +
                "          <execution>\n" +
                "            <id>openjpa-enhance</id>\n" +
                "            <phase>process-classes</phase>\n" +
                "            <goals>\n" +
                "              <goal>enhance</goal>\n" +
                "            </goals>\n" +
                "          </execution>\n" +
                "        </executions>\n" +
                "        <configuration>\n" +
                "          <includes>com/application/jpa/*.class</includes>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "          <argLine>\"-javaagent:${settings.localRepository}/org/apache/tomee/openejb-javaagent/7.0.0-M3/openejb-javaagent-7.0.0-M3.jar\"</argLine>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
    }

    @Test
    public void openjpaArquillianMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("OpenJPA", "Arquillian"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(24, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== Arquillian\n" +
                "\n" +
                "Arquillian is the framework created by JBoss to standardize the testing accross containers. \n" +
                "Of course it concerns EE container but it has a lot of extensions to integrate smoothly  \n" +
                "with Selenium (Graphene), Spock, AngularJS, .... It allows to do in server and or client side tests or even \n" +
                "mixing both.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jpa/ArquillianHelloEntityTest.java\n" +
                "- src/test/resources/arquillian.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/test/"));
        assertEquals("", files.get("application/src/test/resources/"));
        assertEquals("<?xml version=\"1.0\"?>\n" +
                "<arquillian>\n" +
                "  <container qualifier=\"tomee\" default=\"true\">\n" +
                "    <configuration>\n" +
                "      <property name=\"httpsPort\">-1</property>\n" +
                "      <property name=\"httpPort\">-1</property>\n" +
                "      <property name=\"stopPort\">-1</property>\n" +
                "      <property name=\"ajpPort\">-1</property>\n" +
                "      <property name=\"cleanOnStartUp\">true</property>\n" +
                "      <property name=\"simpleLog\">true</property>\n" +
                "      <property name=\"dir\">target/tomee</property>\n" +
                "      <property name=\"appWorkingDir\">target/arquillian-workdir</property>\n" +
                "      <property name=\"properties\">\n" +
                "        # you can define a resource there if needed\n" +
                "      </property>\n" +
                "    </configuration>\n" +
                "  </container>\n" +
                "</arquillian>", files.get("application/src/test/resources/arquillian.xml"));
        assertEquals("", files.get("application/src/test/java/"));
        assertEquals("", files.get("application/src/test/java/com/"));
        assertEquals("", files.get("application/src/test/java/com/application/"));
        assertEquals("", files.get("application/src/test/java/com/application/jpa/"));
        assertEquals("package com.application.jpa;\n" +
                "\n" +
                "import org.jboss.arquillian.container.test.api.Deployment;\n" +
                "import org.jboss.arquillian.junit.Arquillian;\n" +
                "import org.jboss.shrinkwrap.api.Archive;\n" +
                "import org.jboss.shrinkwrap.api.ShrinkWrap;\n" +
                "import org.jboss.shrinkwrap.api.asset.EmptyAsset;\n" +
                "import org.jboss.shrinkwrap.api.spec.WebArchive;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.annotation.Resource;\n" +
                "import javax.persistence.EntityManager;\n" +
                "import javax.persistence.PersistenceContext;\n" +
                "import javax.transaction.UserTransaction;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@RunWith(Arquillian.class)\n" +
                "public class ArquillianHelloEntityTest {\n" +
                "    @Deployment\n" +
                "    public static Archive<?> createDeploymentPackage() {\n" +
                "        return ShrinkWrap.create(WebArchive.class, \"app.war\")\n" +
                "                .addPackage(HelloEntity.class.getPackage())\n" +
                "                .addAsManifestResource(\"META-INF/persistence.xml\", \"persistence.xml\")\n" +
                "                .addAsWebInfResource(EmptyAsset.INSTANCE, \"beans.xml\");\n" +
                "    }\n" +
                "\n" +
                "    @PersistenceContext\n" +
                "    private EntityManager entityManager;\n" +
                "\n" +
                "    @Resource\n" +
                "    private UserTransaction userTransaction;\n" +
                "\n" +
                "    @Test\n" +
                "    public void findByName() throws Exception { // Note: you can use arquillian-persistence for an advanced JPA integration\n" +
                "        // create some data\n" +
                "        userTransaction.begin();\n" +
                "        final HelloEntity helloEntity = new HelloEntity();\n" +
                "        helloEntity.setName(\"test\");\n" +
                "        entityManager.persist(helloEntity);\n" +
                "        entityManager.flush();\n" +
                "        final long id = helloEntity.getId();\n" +
                "        userTransaction.commit();\n" +
                "\n" +
                "        try { // do the test\n" +
                "            assertEquals(\n" +
                "                    1,\n" +
                "                    entityManager.createNamedQuery(\"HelloEntity.findByName\")\n" +
                "                            .setParameter(\"name\", \"test\")\n" +
                "                            .getResultList().size());\n" +
                "        } finally {\n" +
                "\n" +
                "            // cleanup data\n" +
                "            userTransaction.begin();\n" +
                "            entityManager.remove(entityManager.getReference(HelloEntity.class, id));\n" +
                "            userTransaction.commit();\n" +
                "        }\n" +
                "    }\n" +
                "}\n", files.get("application/src/test/java/com/application/jpa/ArquillianHelloEntityTest.java"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/java/"));
        assertEquals("", files.get("application/src/main/java/com/"));
        assertEquals("", files.get("application/src/main/java/com/application/"));
        assertEquals("", files.get("application/src/main/java/com/application/jpa/"));
        assertEquals(
                "package com.application.jpa;\n" +
                        "\n" +
                        "import javax.persistence.Column;\n" +
                        "import javax.persistence.Entity;\n" +
                        "import javax.persistence.GeneratedValue;\n" +
                        "import javax.persistence.Id;\n" +
                        "import javax.persistence.NamedQueries;\n" +
                        "import javax.persistence.NamedQuery;\n" +
                        "import javax.persistence.Table;\n" +
                        "import javax.persistence.Version;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@NamedQueries({\n" +
                        "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                        "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                        "})\n" +
                        "@Table(name = \"hello\")\n" +
                        "public class HelloEntity {\n" +
                        "    @Id\n" +
                        "    @GeneratedValue\n" +
                        "    private long id;\n" +
                        "\n" +
                        "    @Version\n" +
                        "    private long version;\n" +
                        "\n" +
                        "    @Column(length = 160)\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    public long getId() {\n" +
                        "        return id;\n" +
                        "    }\n" +
                        "\n" +
                        "    public long getVersion() {\n" +
                        "        return version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setVersion(final long version) {\n" +
                        "        this.version = version;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(final String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
        assertEquals("", files.get("application/src/main/resources/"));
        assertEquals("", files.get("application/src/main/resources/META-INF/"));
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"\n" +
                        "              http://java.sun.com/xml/ns/persistence\n" +
                        "              http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"\n" +
                        "             version=\"2.0\">\n" +
                        "  <persistence-unit name=\"jpa\">\n" +
                        "    <jta-data-source>jpaDataSource</jta-data-source>\n" +
                        "    <properties>\n" +
                        "      <!-- auto create tables -->\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "\n" +
                        "      <!-- avoid to list classes but also avoids JPA to scan by itself reusing main container scanning -->\n" +
                        "      <property name=\"openejb.jpa.auto-scan\" value=\"true\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>\n",
                files.get("application/src/main/resources/META-INF/persistence.xml"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.openjpa</groupId>\n" +
                "      <artifactId>openjpa</artifactId>\n" +
                "      <version>2.4.1</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>junit</groupId>\n" +
                "      <artifactId>junit</artifactId>\n" +
                "      <version>4.12</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.cxf</groupId>\n" +
                "      <artifactId>cxf-rt-rs-client</artifactId>\n" +
                "      <version>3.1.5</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.johnzon</groupId>\n" +
                "      <artifactId>johnzon-jaxrs</artifactId>\n" +
                "      <version>0.9.3-incubating</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>apache-tomee</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <classifier>webprofile</classifier>\n" +
                "      <scope>test</scope>\n" +
                "      <type>zip</type>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>arquillian-tomee-remote</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.jboss.arquillian.junit</groupId>\n" +
                "      <artifactId>arquillian-junit-container</artifactId>\n" +
                "      <version>1.1.11.Final</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.openjpa</groupId>\n" +
                "        <artifactId>openjpa-maven-plugin</artifactId>\n" +
                "        <version>2.4.1</version>\n" +
                "        <executions>\n" +
                "          <execution>\n" +
                "            <id>openjpa-enhance</id>\n" +
                "            <phase>process-classes</phase>\n" +
                "            <goals>\n" +
                "              <goal>enhance</goal>\n" +
                "            </goals>\n" +
                "          </execution>\n" +
                "        </executions>\n" +
                "        <configuration>\n" +
                "          <includes>com/application/jpa/*.class</includes>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
    }

    @Test
    public void lombokJAXRS() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("Arquillian", "JAX-RS", "Lombok"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(23, files.size());
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== JAX-RS\n" +
                "\n" +
                "JAX-RS is the big boom of JavaEE 6. It allows to create a smooth frontend naturally integrated with Javascript \n" +
                "application when combined with JSON.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jaxrs/ApplicationConfig.java\n" +
                "- src/main/java/com/application/jaxrs/HelloResource.java\n" +
                "- src/main/java/com/application/jaxrs/Hello.java\n" +
                "\n" +
                "\n" +
                "== Libraries\n" +
                "\n" +
                "=== Lombok\n" +
                "\n" +
                "Lombok provides an annotation processor allowing to generate at compile time \n" +
                "most boring java patterns like getter/setter/equals/hashcode/toString/... (@Data for instance).\n" +
                "More on https://projectlombok.org/.\n" +
                "\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== Arquillian\n" +
                "\n" +
                "Arquillian is the framework created by JBoss to standardize the testing accross containers. \n" +
                "Of course it concerns EE container but it has a lot of extensions to integrate smoothly  \n" +
                "with Selenium (Graphene), Spock, AngularJS, .... It allows to do in server and or client side tests or even \n" +
                "mixing both.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jaxrs/ArquillianHelloResourceTest.java\n" +
                "- src/test/resources/arquillian.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.projectlombok</groupId>\n" +
                "      <artifactId>lombok</artifactId>\n" +
                "      <version>1.16.8</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>junit</groupId>\n" +
                "      <artifactId>junit</artifactId>\n" +
                "      <version>4.12</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.cxf</groupId>\n" +
                "      <artifactId>cxf-rt-rs-client</artifactId>\n" +
                "      <version>3.1.5</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.johnzon</groupId>\n" +
                "      <artifactId>johnzon-jaxrs</artifactId>\n" +
                "      <version>0.9.3-incubating</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>apache-tomee</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <classifier>webprofile</classifier>\n" +
                "      <scope>test</scope>\n" +
                "      <type>zip</type>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>arquillian-tomee-remote</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.jboss.arquillian.junit</groupId>\n" +
                "      <artifactId>arquillian-junit-container</artifactId>\n" +
                "      <version>1.1.11.Final</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "import lombok.Data;\n" +
                "\n" +
                "@Data\n" +
                "public class Hello {\n" +
                "    private String name;\n" +
                "}\n", files.get("application/src/main/java/com/application/jaxrs/Hello.java"));
    }

    @Test
    public void lombokOpenJPA() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("Lombok", "OpenJPA"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(16, files.size());
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== OpenJPA\n" +
                "\n" +
                "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jpa/HelloEntity.java\n" +
                "- src/main/resources/META-INF/persistence.xml\n" +
                "\n" +
                "\n" +
                "== Libraries\n" +
                "\n" +
                "=== Lombok\n" +
                "\n" +
                "Lombok provides an annotation processor allowing to generate at compile time \n" +
                "most boring java patterns like getter/setter/equals/hashcode/toString/... (@Data for instance).\n" +
                "More on https://projectlombok.org/.\n" +
                "\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.openjpa</groupId>\n" +
                "      <artifactId>openjpa</artifactId>\n" +
                "      <version>2.4.1</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.projectlombok</groupId>\n" +
                "      <artifactId>lombok</artifactId>\n" +
                "      <version>1.16.8</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.openjpa</groupId>\n" +
                "        <artifactId>openjpa-maven-plugin</artifactId>\n" +
                "        <version>2.4.1</version>\n" +
                "        <executions>\n" +
                "          <execution>\n" +
                "            <id>openjpa-enhance</id>\n" +
                "            <phase>process-classes</phase>\n" +
                "            <goals>\n" +
                "              <goal>enhance</goal>\n" +
                "            </goals>\n" +
                "          </execution>\n" +
                "        </executions>\n" +
                "        <configuration>\n" +
                "          <includes>com/application/jpa/*.class</includes>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
        assertEquals("package com.application.jpa;\n" +
                "\n" +
                "import lombok.AccessLevel;\n" +
                "import lombok.Getter;\n" +
                "import lombok.Setter;\n" +
                "\n" +
                "import javax.persistence.Column;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.GeneratedValue;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.NamedQueries;\n" +
                "import javax.persistence.NamedQuery;\n" +
                "import javax.persistence.Table;\n" +
                "import javax.persistence.Version;\n" +
                "\n" +
                "@Entity\n" +
                "@NamedQueries({\n" +
                "        @NamedQuery(name = \"HelloEntity.findAll\", query = \"select e from HelloEntity e order by u.name\"),\n" +
                "        @NamedQuery(name = \"HelloEntity.findByName\", query = \"select e from HelloEntity e where e.name = :name\")\n" +
                "})\n" +
                "@Table(name = \"hello\")\n" +
                "@Getter @Setter\n" +
                "public class HelloEntity {\n" +
                "    @Id\n" +
                "    @GeneratedValue\n" +
                "    @Setter(AccessLevel.NONE)\n" +
                "    private long id;\n" +
                "\n" +
                "    @Version\n" +
                "    private long version;\n" +
                "\n" +
                "    @Column(length = 160)\n" +
                "    private String name;\n" +
                "}\n", files.get("application/src/main/java/com/application/jpa/HelloEntity.java"));
    }

    @Test
    public void arquillianJAXRS() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("Arquillian", "JAX-RS"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(23, files.size());
        // just testing additional/changed ones compared to simple JAXRS
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Core\n" +
                "\n" +
                "=== JAX-RS\n" +
                "\n" +
                "JAX-RS is the big boom of JavaEE 6. It allows to create a smooth frontend naturally integrated with Javascript \n" +
                "application when combined with JSON.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/jaxrs/ApplicationConfig.java\n" +
                "- src/main/java/com/application/jaxrs/HelloResource.java\n" +
                "- src/main/java/com/application/jaxrs/Hello.java\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== Arquillian\n" +
                "\n" +
                "Arquillian is the framework created by JBoss to standardize the testing accross containers. \n" +
                "Of course it concerns EE container but it has a lot of extensions to integrate smoothly  \n" +
                "with Selenium (Graphene), Spock, AngularJS, .... It allows to do in server and or client side tests or even \n" +
                "mixing both.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/jaxrs/ArquillianHelloResourceTest.java\n" +
                "- src/test/resources/arquillian.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>junit</groupId>\n" +
                "      <artifactId>junit</artifactId>\n" +
                "      <version>4.12</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.cxf</groupId>\n" +
                "      <artifactId>cxf-rt-rs-client</artifactId>\n" +
                "      <version>3.1.5</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.johnzon</groupId>\n" +
                "      <artifactId>johnzon-jaxrs</artifactId>\n" +
                "      <version>0.9.3-incubating</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>apache-tomee</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <classifier>webprofile</classifier>\n" +
                "      <scope>test</scope>\n" +
                "      <type>zip</type>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>arquillian-tomee-remote</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.jboss.arquillian.junit</groupId>\n" +
                "      <artifactId>arquillian-junit-container</artifactId>\n" +
                "      <version>1.1.11.Final</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/test/"));
        assertEquals("", files.get("application/src/test/resources/"));
        assertEquals("<?xml version=\"1.0\"?>\n" +
                "<arquillian>\n" +
                "  <container qualifier=\"tomee\" default=\"true\">\n" +
                "    <configuration>\n" +
                "      <property name=\"httpsPort\">-1</property>\n" +
                "      <property name=\"httpPort\">-1</property>\n" +
                "      <property name=\"stopPort\">-1</property>\n" +
                "      <property name=\"ajpPort\">-1</property>\n" +
                "      <property name=\"cleanOnStartUp\">true</property>\n" +
                "      <property name=\"simpleLog\">true</property>\n" +
                "      <property name=\"dir\">target/tomee</property>\n" +
                "      <property name=\"appWorkingDir\">target/arquillian-workdir</property>\n" +
                "      <property name=\"properties\">\n" +
                "        # you can define a resource there if needed\n" +
                "      </property>\n" +
                "    </configuration>\n" +
                "  </container>\n" +
                "</arquillian>", files.get("application/src/test/resources/arquillian.xml"));
        assertEquals("", files.get("application/src/test/java/"));
        assertEquals("", files.get("application/src/test/java/com/"));
        assertEquals("", files.get("application/src/test/java/com/application/"));
        assertEquals("", files.get("application/src/test/java/com/application/jaxrs/"));
        assertEquals("package com.application.jaxrs;\n" +
                "\n" +
                "import javax.ws.rs.client.ClientBuilder;\n" +
                "import javax.ws.rs.core.MediaType;\n" +
                "import java.net.URL;\n" +
                "\n" +
                "import org.apache.johnzon.jaxrs.JohnzonProvider;\n" +
                "import org.jboss.arquillian.container.test.api.Deployment;\n" +
                "import org.jboss.arquillian.junit.Arquillian;\n" +
                "import org.jboss.arquillian.test.api.ArquillianResource;\n" +
                "import org.jboss.shrinkwrap.api.Archive;\n" +
                "import org.jboss.shrinkwrap.api.ShrinkWrap;\n" +
                "import org.jboss.shrinkwrap.api.asset.EmptyAsset;\n" +
                "import org.jboss.shrinkwrap.api.spec.WebArchive;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.ws.rs.client.ClientBuilder;\n" +
                "import javax.ws.rs.core.MediaType;\n" +
                "import java.net.URL;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@RunWith(Arquillian.class)\n" +
                "public class ArquillianHelloResourceTest {\n" +
                "    @Deployment(testable = false) // testable = false implies it is a client test so no server injection\n" +
                "    public static Archive<?> createDeploymentPackage() {\n" +
                "        return ShrinkWrap.create(WebArchive.class, \"app.war\")\n" +
                "                .addPackage(Hello.class.getPackage())\n" +
                "                .addAsWebInfResource(EmptyAsset.INSTANCE, \"beans.xml\");\n" +
                "    }\n" +
                "\n" +
                "    @ArquillianResource\n" +
                "    private URL url;\n" +
                "\n" +
                "    @Test\n" +
                "    public void sayHelloToTheDefaultUser() {\n" +
                "        assertEquals(\n" +
                "                \"test\",\n" +
                "                ClientBuilder.newBuilder().build()\n" +
                "                        .register(new JohnzonProvider<>()) // we run in a remote javaee so we need to do it ourself\n" +
                "                        .target(url.toExternalForm()).path(\"api/hello\")\n" +
                "                        .request(MediaType.APPLICATION_JSON_TYPE)\n" +
                "                        .get(Hello.class)\n" +
                "                        .getName());\n" +
                "    }\n" +
                "}\n" +
                "\n", files.get("application/src/test/java/com/application/jaxrs/ArquillianHelloResourceTest.java"));
    }

    @Test
    public void generateDefaultGradle() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Gradle");
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(8, files.size());
        assertEquals("", files.get("application/"));
        assertEquals("= A Factory generated Application\n\n", files.get("application/README.adoc"));
        assertEquals("", files.get("application/src/"));
        assertEquals("", files.get("application/src/main/"));
        assertEquals("", files.get("application/src/main/webapp/"));
        assertEquals("", files.get("application/src/main/webapp/WEB-INF/"));
        assertEquals(
                "<?xml version=\"1.0\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n",
                files.get("application/src/main/webapp/WEB-INF/beans.xml"));
        assertEquals(
                "buildscript {\n" +
                        "  repositories {\n" +
                        "    maven {\n" +
                        "      url \"https://plugins.gradle.org/m2/\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "  dependencies {\n" +
                        "    classpath \"com.netflix.nebula:gradle-extra-configurations-plugin:3.0.3\"\n" +
                        "  }\n" +
                        "}\n" +
                        "\n" +
                        "apply plugin: 'nebula.provided-base'\n" +
                        "apply plugin: 'java'\n" +
                        "apply plugin: 'war'\n" +
                        "\n" +
                        "\n" +
                        "repositories {\n" +
                        "  mavenLocal()\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "group = 'com.application'\n" +
                        "description = 'An application generated by the Factory'\n" +
                        "\n" +
                        "\n" +
                        "war {\n" +
                        "  baseName = 'application'\n" +
                        "  version = '0.0.1-SNAPSHOT'\n" +
                        "}\n" +
                        "\n" +
                        "sourceCompatibility = 1.8\n" +
                        "targetCompatibility = 1.8\n" +
                        "\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  provided group: 'org.apache.tomee', name: 'javaee-api', version: '7.0'\n" +
                        "}\n", files.get("application/build.gradle"));
    }

    @Test
    public void deltaspikeConfigurationMaven() throws IOException {
        final Map<String, String> files = new HashMap<>();
        try (final ZipInputStream stream = new ZipInputStream(blog.target().path("factory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/zip")
                .post(Entity.entity(new ProjectModel() {{
                    setBuildType("Maven");
                    setFacets(asList("Deltaspike Configuration", "ApplicationComposer", "Arquillian"));
                }}, MediaType.APPLICATION_JSON_TYPE), InputStream.class))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                files.put(entry.getName(), new String(io.read(stream), StandardCharsets.UTF_8));
            }
        }
        assertEquals(22, files.size());
        assertEquals("package com.application.deltaspike;\n" +
                "\n" +
                "import org.apache.deltaspike.core.api.config.ConfigProperty;\n" +
                "import org.apache.deltaspike.core.impl.config.DefaultConfigPropertyProducer;\n" +
                "import org.jboss.arquillian.container.test.api.Deployment;\n" +
                "import org.jboss.arquillian.junit.Arquillian;\n" +
                "import org.jboss.shrinkwrap.api.Archive;\n" +
                "import org.jboss.shrinkwrap.api.ShrinkWrap;\n" +
                "import org.jboss.shrinkwrap.api.asset.EmptyAsset;\n" +
                "import org.jboss.shrinkwrap.api.spec.WebArchive;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.inject.Inject;\n" +
                "\n" +
                "import static org.apache.ziplock.JarLocation.jarLocation;\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@RunWith(Arquillian.class)\n" +
                "public class ArquillianConfigurationTest {\n" +
                "    @Deployment\n" +
                "    public static Archive<?> createDeploymentPackage() {\n" +
                "        return ShrinkWrap.create(WebArchive.class, \"configuration.war\")\n" +
                "                .addClasses(ApplicationConfiguration.class)\n" +
                "                .addAsLibraries(\n" +
                "                        // add deltaspike library, using ziplock which locates in current classpath jar from a class\n" +
                "                        jarLocation(ConfigProperty.class),\n" +
                "                        jarLocation(DefaultConfigPropertyProducer.class)\n" +
                "                )\n" +
                "                .addAsWebInfResource(EmptyAsset.INSTANCE, \"beans.xml\");\n" +
                "    }\n" +
                "\n" +
                "    @Inject\n" +
                "    private ApplicationConfiguration configuration;\n" +
                "\n" +
                "    @Test\n" +
                "    public void checkApiVersion() {\n" +
                "        assertEquals(\"1.0\", configuration.getApiVersion());\n" +
                "    }\n" +
                "}\n" +
                "\n", files.get("application/src/test/java/com/application/deltaspike/ArquillianConfigurationTest.java"));
        assertEquals("= A Factory generated Application\n" +
                "\n" +
                "== Libraries\n" +
                "\n" +
                "=== Deltaspike Configuration\n" +
                "\n" +
                "DeltaSpike provides a set of JavaEE CDI extensions.\n" +
                "Configuration one allows to use a smooth CDI API to get its configuration \n" +
                "injected keeping an extensible and advanced value reading system matching all \n" +
                "enterprise configuration choices.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/main/java/com/application/deltaspike/ApplicationConfiguration.java\n" +
                "\n" +
                "\n" +
                "== Test\n" +
                "\n" +
                "=== ApplicationComposer\n" +
                "\n" +
                "ApplicationComposer is a TomEE feature allowing to:\n" +
                "\n" +
                "- test with JUnit or TestNG any EE application in embedded mode building programmatically the EE model instead of relying on scanning\n" +
                "- deploy a standalone application using ApplicationComposers helper (very useful for application without frontend like batch)\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/deltaspike/ApplicationComposerConfigurationTest.java\n" +
                "\n" +
                "\n" +
                "=== Arquillian\n" +
                "\n" +
                "Arquillian is the framework created by JBoss to standardize the testing accross containers. \n" +
                "Of course it concerns EE container but it has a lot of extensions to integrate smoothly  \n" +
                "with Selenium (Graphene), Spock, AngularJS, .... It allows to do in server and or client side tests or even \n" +
                "mixing both.\n" +
                "\n" +
                "==== Files generated by this facet\n" +
                "\n" +
                "- src/test/java/com/application/deltaspike/ArquillianConfigurationTest.java\n" +
                "- src/test/resources/arquillian.xml\n" +
                "\n" +
                "\n", files.get("application/README.adoc"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"\n" +
                "          http://maven.apache.org/POM/4.0.0\n" +
                "          http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>com.application</groupId>\n" +
                "  <artifactId>application</artifactId>\n" +
                "  <version>0.0.1-SNAPSHOT</version>\n" +
                "  <packaging>war</packaging>\n" +
                "\n" +
                "  <name>A Factory generated Application</name>\n" +
                "  <description>An application generated by the Factory</description>\n" +
                "\n" +
                "  <properties>\n" +
                "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "  </properties>\n" +
                "\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>javaee-api</artifactId>\n" +
                "      <version>7.0</version>\n" +
                "      <scope>provided</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.deltaspike.core</groupId>\n" +
                "      <artifactId>deltaspike-core-api</artifactId>\n" +
                "      <version>1.5.4</version>\n" +
                "      <scope>compile</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.deltaspike.core</groupId>\n" +
                "      <artifactId>deltaspike-core-impl</artifactId>\n" +
                "      <version>1.5.4</version>\n" +
                "      <scope>runtime</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>junit</groupId>\n" +
                "      <artifactId>junit</artifactId>\n" +
                "      <version>4.12</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.cxf</groupId>\n" +
                "      <artifactId>cxf-rt-rs-client</artifactId>\n" +
                "      <version>3.1.5</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.johnzon</groupId>\n" +
                "      <artifactId>johnzon-jaxrs</artifactId>\n" +
                "      <version>0.9.3-incubating</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>apache-tomee</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <classifier>webprofile</classifier>\n" +
                "      <scope>test</scope>\n" +
                "      <type>zip</type>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>arquillian-tomee-remote</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>openejb-core</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.apache.tomee</groupId>\n" +
                "      <artifactId>ziplock</artifactId>\n" +
                "      <version>7.0.0-M3</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.jboss.arquillian.junit</groupId>\n" +
                "      <artifactId>arquillian-junit-container</artifactId>\n" +
                "      <version>1.1.11.Final</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "\n" +
                "  <build>\n" +
                "    <plugins>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-compiler-plugin</artifactId>\n" +
                "        <version>3.5.1</version>\n" +
                "        <configuration>\n" +
                "          <source>1.8</source>\n" +
                "          <target>1.8</target>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-embedded-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "          <classpathAsWar>true</classpathAsWar>\n" +
                "          <webResourceCached>false</webResourceCached>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.tomee.maven</groupId>\n" +
                "        <artifactId>tomee-maven-plugin</artifactId>\n" +
                "        <version>7.0.0-M3</version>\n" +
                "        <configuration>\n" +
                "          <context>/${project.artifactId}</context>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-war-plugin</artifactId>\n" +
                "        <version>2.6</version>\n" +
                "        <configuration>\n" +
                "          <failOnMissingWebXml>false</failOnMissingWebXml>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "      <plugin>\n" +
                "        <groupId>org.apache.maven.plugins</groupId>\n" +
                "        <artifactId>maven-surefire-plugin</artifactId>\n" +
                "        <version>2.19</version>\n" +
                "        <configuration>\n" +
                "          <trimStackTrace>false</trimStackTrace>\n" +
                "          <runOrder>alphabetical</runOrder>\n" +
                "        </configuration>\n" +
                "      </plugin>\n" +
                "    </plugins>\n" +
                "  </build>\n" +
                "</project>\n", files.get("application/pom.xml"));
        assertEquals("package com.application.deltaspike;\n" +
                "\n" +
                "import javax.enterprise.context.ApplicationScoped;\n" +
                "import javax.inject.Inject;\n" +
                "\n" +
                "import org.apache.deltaspike.core.api.config.ConfigProperty;\n" +
                "\n" +
                "@ApplicationScoped\n" +
                "public class ApplicationConfiguration {\n" +
                "    @Inject\n" +
                "    @ConfigProperty(name = \"application.api.version\", defaultValue  = \"1.0\")\n" +
                "    private String apiVersion;\n" +
                "\n" +
                "    public String getApiVersion() {\n" +
                "        return apiVersion;\n" +
                "    }\n" +
                "}", files.get("application/src/main/java/com/application/deltaspike/ApplicationConfiguration.java"));
        assertEquals("package com.application.deltaspike;\n" +
                "\n" +
                "import org.apache.openejb.junit.ApplicationComposer;\n" +
                "import org.apache.openejb.testing.Classes;\n" +
                "import org.apache.openejb.testing.Jars;\n" +
                "import org.apache.openejb.testing.SimpleLog;\n" +
                "import org.junit.Test;\n" +
                "import org.junit.runner.RunWith;\n" +
                "\n" +
                "import javax.inject.Inject;\n" +
                "\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "\n" +
                "@SimpleLog\n" +
                "@Jars(\"deltaspike-\")\n" +
                "@Classes(cdi = true, value = ApplicationConfiguration.class)\n" +
                "@RunWith(ApplicationComposer.class)\n" +
                "public class ApplicationComposerConfigurationTest {\n" +
                "    @Inject\n" +
                "    private ApplicationConfiguration configuration;\n" +
                "\n" +
                "    @Test\n" +
                "    public void checkApiVersion() {\n" +
                "        assertEquals(\"1.0\", configuration.getApiVersion());\n" +
                "    }\n" +
                "}\n", files.get("application/src/test/java/com/application/deltaspike/ApplicationComposerConfigurationTest.java"));
    }
}

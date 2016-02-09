# maven-wait-http-plugin

Maven plugin that waits until a connection is available. In the case of an http connection, waits for a 200 response.

## Parameters

| Parameter   | Default   | Usage |
|-------------|-----------|-------|
| protocol    | http      | Protocol of connection. If http, waits for 200 response, otherwise just waits for connection |
| host        | localhost ||
| port        | 8080      ||
| file        | /         | Path to fetch |
| timeout     | 30000     | Time in ms to wait between trys |
| maxcount    | 0         | Maximum number of time to try connecting before failing |
| skip        | false     ||
| read        | false     | Read the response instead of just opening connection |
| initialWait | 0         | Time in ms to wait before initial try connecting |

## Usage
```
<build>
  <plugins>
    <plugin>
      <groupId>org.kew.maven.plugins</groupId>
      <artifactId>wait-http-maven-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
      <executions>
        <execution>
          <id>test</id>
          <phase>pre-integration-test</phase>
          <goals>
            <goal>wait-http</goal>
          </goals>
          <configuration>
            <protocol>http</protocol>
            <host>localhost</host>
            <port>1080</port>
            <file>/</file>
            <maxcount>20</maxcount>
            <timeout>10000</timeout>
            <initialWait>10000</initialWait>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
